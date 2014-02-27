package com.kuxhausen.huemore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.Stack;

import alt.android.os.CountDownTimer;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.automation.FireReceiver;
import com.kuxhausen.huemore.network.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.network.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.timing.AlarmReciever;
import com.kuxhausen.huemore.timing.AlarmState;
import com.kuxhausen.huemore.timing.Conversions;

public class MoodExecuterService extends Service implements ConnectionMonitor, OnBulbAttributesReturnedListener{

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		MoodExecuterService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return MoodExecuterService.this;
		}
	}
	MoodExecuterService me = this;
	private RequestQueue volleyRQ;

	int notificationId = 1337;

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	private static CountDownTimer countDownTimer;
	Long moodLoopIterationEndNanoTime = 0L;
	WakeLock wakelock;
	private boolean hasHubConnection = false;
	private final static int TRANSMITS_PER_SECOND = 12;
	private final static int MAX_STOP_SELF_COUNDOWN = TRANSMITS_PER_SECOND*3;
	private static int countDownToStopSelf = MAX_STOP_SELF_COUNDOWN;
	private static boolean suspendingTillNextEvent = false;
	public ArrayList<OnConnectionStatusChangedListener> connectionListeners = new ArrayList<OnConnectionStatusChangedListener>();
	
	public MoodExecuterService() {
	}
	
	PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
	int transientIndex = 0;
	
	public enum KnownState {Unknown, ToSend, Getting, Synched};	
	public Integer maxBrightness;
	public int[] group;
	public int[] bulbBri;
	public int[] bulbRelBri;
	public KnownState[] bulbKnown;
	public Mood mood;
	public String groupName;
	public String moodName;
	private static int MAX_REL_BRI = 255;
	public ArrayList<OnBrightnessChangedListener> brightnessListeners = new ArrayList<OnBrightnessChangedListener>();
	boolean groupIsColorLooping=false;
	boolean groupIsAlerting=false;
	
	public synchronized void onGroupSelected(int[] bulbs, Integer optionalBri, String groupName){
		groupIsAlerting = false;
		groupIsColorLooping = false;
		group = bulbs;
		maxBrightness = null;
		bulbBri = new int[group.length];
		bulbRelBri = new int[group.length];
		bulbKnown = new KnownState[group.length];
		for(int i = 0; i < bulbRelBri.length; i++){
			bulbRelBri[i] = MAX_REL_BRI;
			bulbKnown[i] = KnownState.Unknown;
		}
		
		this.groupName = groupName;
		
		if(optionalBri==null){
			for(int i = 0; i< group.length; i++){
				bulbKnown[i] = KnownState.Getting;
				NetworkMethods.PreformGetBulbAttributes(me, me, group[i]);
			}
		} else {
			maxBrightness = optionalBri;
			for(int i = 0; i< group.length; i++)
				bulbKnown[i] = KnownState.ToSend;
			onBrightnessChanged();
		}
	}
	/** doesn't notify listeners **/
	public synchronized void setBrightness(int brightness){
		if(countDownTimer==null)
			restartCountDownTimer();
		
		maxBrightness = brightness;
		if(group!=null){
			for(int i = 0; i< group.length; i++){
				bulbBri[i] = (maxBrightness * bulbRelBri[i])/MAX_REL_BRI; 
				bulbKnown[i] = KnownState.ToSend;
			}
		}
	}

	public interface OnBrightnessChangedListener {
		public void onBrightnessChanged(int brightness);
	}
	
	/** announce brightness to any listeners **/
	public void onBrightnessChanged(){
		for(OnBrightnessChangedListener l : brightnessListeners){
			l.onBrightnessChanged(maxBrightness);
		}
	}
	public void registerBrightnessListener(OnBrightnessChangedListener l){
		if(maxBrightness!=null)
			l.onBrightnessChanged(maxBrightness);
		brightnessListeners.add(l);
	}
	public void removeBrightnessListener(OnBrightnessChangedListener l){
		brightnessListeners.remove(l);
	}
	
	public void startMood(Mood m, String moodName){
		mood = m;
		this.moodName = moodName;
		createNotification();
		queue.clear();
		loadMoodIntoQueue();
		restartCountDownTimer();
	}
	public void stopMood(){
		mood = null;
		queue.clear();
	}
	
	@Override
	public void onAttributesReturned(BulbAttributes result, int bulbNumber) {
		//figure out which bulb in group (if that group is still selected)
		int index = calculateBulbPositionInGroup(bulbNumber);
		//if group is still expected this, save 
		if(index>-1 && bulbKnown[index]==KnownState.Getting){
			bulbKnown[index] = KnownState.Synched;
			bulbBri[index] = result.state.bri;
			
			//if all expected get brightnesses have returned, compute maxbri and notify listeners
			boolean anyOutstandingGets = false;
			for(KnownState ks : bulbKnown)
				anyOutstandingGets |= (ks == KnownState.Getting);
			if(!anyOutstandingGets){
				//todo calc more intelligent bri when mood known
				int briSum = 0;
				for(int bri : bulbBri)
					briSum +=bri;
				maxBrightness = briSum/group.length;
				
				for(int i = 0; i< group.length; i++){
					bulbBri[i]= maxBrightness;
					bulbRelBri[i] = MAX_REL_BRI;
				}
				
				onBrightnessChanged();
			}	
		}	
	}
	/** finds bulb index within group[] **/
	private int calculateBulbPositionInGroup(int bulbNumber){
		int index = -1;
		for(int j = 0; j< group.length; j++){
			if(group[j]==bulbNumber)
				index = j;
		}
		return index;
	}
	
	@Override
	public void setHubConnectionState(boolean connected){
		if(hasHubConnection!=connected){
			hasHubConnection = connected;
			for(OnConnectionStatusChangedListener l : connectionListeners)
				l.onConnectionStatusChanged(connected);	
		}
		if(!connected){
			//TODO rate limit
			NetworkMethods.PreformGetBulbList(this, null);
		}
	}
	public boolean hasHubConnection(){
		return hasHubConnection;
	}
	
	
	public void createNotification() {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String secondaryText = ((groupName!=null&&moodName!=null)?groupName:"") + ((moodName!=null)?(" \u2192 " +moodName):"");
		
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.lampshade_notification)
				.setContentTitle(
						this.getResources().getString(R.string.app_name))
				.setContentText(secondaryText)
				.setContentIntent(resultPendingIntent);
		this.startForeground(notificationId, mBuilder.build());

	}

	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}

	private boolean hasTransientChanges() {
		if(bulbKnown==null)
			return false;		
		boolean result = false;
		for (KnownState ks : bulbKnown)
			result |= (ks==KnownState.ToSend);
		return result;
	}

	private void loadMoodIntoQueue() {
		
		//clear out any cached upcoming resume mood
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = settings.edit();
		edit.putString(PreferenceKeys.CACHED_EXECUTING_ENCODED_MOOD,"");
		edit.commit();
		
		ArrayList<Integer>[] channels = new ArrayList[mood.getNumChannels()];
		for (int i = 0; i < channels.length; i++)
			channels[i] = new ArrayList<Integer>();

		for (int i = 0; i < group.length; i++) {
			channels[i % mood.getNumChannels()].add(group[i]);
		}

		if(mood.timeAddressingRepeatPolicy){
			Stack<QueueEvent> pendingEvents = new Stack<QueueEvent>();
			
			long earliestEventStillApplicable = Long.MIN_VALUE;
			
			for (int i= mood.events.length-1; i>=0; i--) {
				Event e = mood.events[i];
				for (Integer bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulb = bNum;
					
					qe.nanoTime = Conversions.nanoEventTimeFromMoodDailyTime(e.time);
					if(qe.nanoTime>System.nanoTime()){
						pendingEvents.add(qe);
					}
					else if(qe.nanoTime>=earliestEventStillApplicable){
						earliestEventStillApplicable = qe.nanoTime;
						qe.nanoTime = System.nanoTime();
						pendingEvents.add(qe);
					}
				}
			}
			
			if(earliestEventStillApplicable == Long.MIN_VALUE && mood.events.length>0){
				//haven't found a previous state to start with, time to roll over and add last evening event
				Event e = mood.events[mood.events.length-1];
				for (Integer bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulb = bNum;
					qe.nanoTime = System.nanoTime();
					pendingEvents.add(qe);
				}
			}
			
			while(!pendingEvents.empty()){
				queue.add(pendingEvents.pop());
			}
		}else{
			for (Event e : mood.events) {
				for (Integer bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulb = bNum;
					
					// 10^8 * e.time
					qe.nanoTime = System.nanoTime()+(e.time*100000000l);
					queue.add(qe);
				}
			}
		}
		moodLoopIterationEndNanoTime = System.nanoTime()+(mood.loopIterationTimeLength*100000000l);
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		volleyRQ = Volley.newRequestQueue(this);
		restartCountDownTimer();
		createNotification();
		
		//acquire wakelock
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		wakelock.acquire();
		
		//start pinging to test connectivity
		NetworkMethods.PreformGetBulbList(this, null);
	}
	@Override
	public void onDestroy() {
		if(countDownTimer!=null)
			countDownTimer.cancel();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
		if(wakelock!=null)
			wakelock.release();
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//remove any possible launched wakelocks
			AlarmReciever.completeWakefulIntent(intent);
			FireReceiver.completeWakefulIntent(intent);

			//if doesn't already have a wakelock, acquire one
			if(this.wakelock==null){
				//acquire wakelock
				PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
				wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
				wakelock.acquire();
			}
			
			String encodedMood = intent
					.getStringExtra(InternalArguments.ENCODED_MOOD);

			try{
				if (encodedMood != null) {
					Pair<Integer[], Pair<Mood, Integer>> moodPairs = HueUrlEncoder.decode(encodedMood);
					moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
					moodName = (moodName == null) ? "Unknown Mood" : moodName;
					
					if(moodPairs.first!=null && moodPairs.first.length>0){
						int[] bulbs = new int[moodPairs.first.length];
						for(int i = 0; i< bulbs.length; i++)
							bulbs[i] = moodPairs.first[i];
						groupName = intent.getStringExtra(InternalArguments.GROUP_NAME);
						onGroupSelected(bulbs, moodPairs.second.second, groupName);
					}
					if(moodPairs.second.first!=null)
						startMood(moodPairs.second.first, moodName);				
				}
			} catch (InvalidEncodingException e) {
				Intent i = new Intent(this,DecodeErrorActivity.class);
				i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, false);
				startActivity(i);
			} catch (FutureEncodingException e) {
				Intent i = new Intent(this,DecodeErrorActivity.class);
				i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, true);
				startActivity(i);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void restartCountDownTimer() {
		
		if (countDownTimer != null)
			countDownTimer.cancel();

		transientIndex = 0;
		countDownToStopSelf = MAX_STOP_SELF_COUNDOWN;
		suspendingTillNextEvent = false;
		// runs at the rate to execute 15 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / TRANSMITS_PER_SECOND)) {

			@Override
			public void onFinish() {
			}

			@Override
			public void onTick(long millisUntilFinished) {
				if (queue.peek()!=null && queue.peek().nanoTime <= System.nanoTime()) {
					QueueEvent e = queue.poll();
					
					//remove effect=none except when meaningful (after spotting an effect=colorloop)
					if(e.event.state.effect!=null && e.event.state.effect.equals("colorloop")){
						groupIsColorLooping = true;
					}else if(!groupIsColorLooping){
						e.event.state.effect = null;
					}
					//remove alert=none except when meaningful (after spotting an alert=colorloop)
					if(e.event.state.alert!=null && (e.event.state.alert.equals("select")||e.event.state.alert.equals("lselect"))){
						groupIsAlerting = true;
					}else if(!groupIsAlerting){
						e.event.state.alert = null;
					}
					
					
					int bulbInGroup = calculateBulbPositionInGroup(e.bulb);
					if(bulbInGroup>-1 && maxBrightness!=null){
						//convert relative brightness into absolute brightness
						if(e.event.state.bri!=null)
							bulbRelBri[bulbInGroup] = e.event.state.bri;
						else
							bulbRelBri[bulbInGroup] = MAX_REL_BRI;
						bulbBri[bulbInGroup] = (bulbRelBri[bulbInGroup] * maxBrightness)/ MAX_REL_BRI;
						e.event.state.bri = bulbBri[bulbInGroup];
						bulbKnown[bulbInGroup] = KnownState.Synched;
					}					
					NetworkMethods.PreformTransmitGroupMood(me, e.bulb, e.event.state);
				} else if (queue.peek() == null && mood != null && mood.isInfiniteLooping() && System.nanoTime()>moodLoopIterationEndNanoTime) {
					loadMoodIntoQueue();
				} else if (hasTransientChanges()) {
					boolean sentSomething = false;
					while (!sentSomething) {
						if(bulbKnown[transientIndex] == KnownState.ToSend){
							BulbState bs = new BulbState();
							bulbBri[transientIndex] = (bulbRelBri[transientIndex] * maxBrightness)/ MAX_REL_BRI;
							bs.bri = bulbBri[transientIndex];
							
							NetworkMethods.PreformTransmitGroupMood(me, group[transientIndex], bs);
							bulbKnown[transientIndex] = KnownState.Synched;
							sentSomething = true;
						}
						transientIndex = (transientIndex + 1) % group.length;
					}
				} else if (suspendingTillNextEvent){
					//TODO shut down loop also
					if(countDownToStopSelf<=0){
						if (wakelock!=null){
							wakelock.release();
							wakelock = null;
						}
						countDownTimer = null;
						this.cancel();
					}						
					else
						countDownToStopSelf--;
				}else if(queue.peek()!=null && (queue.peek().nanoTime + (5000* 1000000L)) > System.nanoTime() && mood.timeAddressingRepeatPolicy==true){
					Integer[] bulbs = new Integer[group.length];
					for(int i = 0; i< bulbs.length; i++)
						bulbs[i] = group[i];
					
					String encodedMood = HueUrlEncoder.encode(mood, bulbs, maxBrightness);
					
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(me);
					Editor edit = settings.edit();
					edit.putString(PreferenceKeys.CACHED_EXECUTING_ENCODED_MOOD,encodedMood);
					edit.commit();
									
					//if no daily events for atleast another 5 seconds, schedule mood for future and flag count down to sleep
					AlarmState as = new AlarmState();
					as.mood = moodName;
					as.group = groupName;
					
					//state 1 second before the next event is to occur
					Long time = Calendar.getInstance().getTimeInMillis() + (queue.peek().nanoTime - System.nanoTime())/1000000L -1000L;
					
					AlarmReciever.scheduleInternalAlarm(me, as, time);
					suspendingTillNextEvent = true;
				} else if (queue.peek() == null && (mood ==null || !mood.isInfiniteLooping())){
					moodName = null;
					createNotification();
					if(countDownToStopSelf<=0){
						me.stopSelf();
						countDownTimer = null;
						this.cancel();
					}
					else
						countDownToStopSelf--;
				}
			}
		};
		countDownTimer.start();
	}
}
