package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.PriorityQueue;
import java.util.Stack;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.timing.AlarmReciever;
import com.kuxhausen.huemore.timing.AlarmState;
import com.kuxhausen.huemore.timing.Conversions;

import alt.android.os.CountDownTimer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;

public class MoodPlayer{

	private Context mContext;
	private DeviceManager mDeviceManager;
	private ArrayList<OnActiveMoodsChangedListener> moodsChangedListeners = new ArrayList<OnActiveMoodsChangedListener>();
	
	public MoodPlayer(Context c, DeviceManager m){	
		mContext = c;
		mDeviceManager = m;
		
		me = (MoodExecuterService)c;
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
		volleyRQ = Volley.newRequestQueue(mContext);
		restartCountDownTimer();
	}
	
	public void playMood(Group g, Mood m){
		
		
		//update notifications
	}
	public void cancelMood(Group g){
		
		
		//update notifications
	}
	
	public void addOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l){
		moodsChangedListeners.add(l);
	}
	public void removeOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l){
		moodsChangedListeners.remove(l);
	}
	private void notifyMoodsChanged(){
		for(OnActiveMoodsChangedListener l : moodsChangedListeners)
			l.onActiveMoodsChanged();
	}
	
	
	public void onDestroy() {
		if(countDownTimer!=null)
			countDownTimer.cancel();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
	}
	
//TODO clean up/remove everything below this line
	private MoodExecuterService me;
	
	
	private SharedPreferences mSettings;
	private RequestQueue volleyRQ;
	private static CountDownTimer countDownTimer;
	Long moodLoopIterationEndNanoTime = 0L;
	private final static int TRANSMITS_PER_SECOND = 12;
	private final static int MAX_STOP_SELF_COUNDOWN = TRANSMITS_PER_SECOND*3;
	private static int countDownToStopSelf = MAX_STOP_SELF_COUNDOWN;
	private static boolean suspendingTillNextEvent = false;
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
	
	boolean groupIsColorLooping=false;
	boolean groupIsAlerting=false;
	PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
	int transientIndex = 0;
	
	
	public String getGroupName(){
		return groupName;
	}
	public String getMoodName(){
		return moodName;
	}
	public Integer getMaxBrightness(){
		return maxBrightness;
	}
	
	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}
	
	public void startMood(Mood m, String moodName){
		mood = m;
		this.moodName = moodName;
		queue.clear();
		loadMoodIntoQueue();
		restartCountDownTimer();
		notifyMoodsChanged();
	}
	public void stopMood(){
		mood = null;
		queue.clear();
		notifyMoodsChanged();
	}
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
			me.onBrightnessChanged();
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
				
				me.onBrightnessChanged();
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
	
	private boolean hasTransientChanges() {
		if(bulbKnown==null)
			return false;		
		boolean result = false;
		for (KnownState ks : bulbKnown)
			result |= (ks==KnownState.ToSend);
		return result;
	}

	
	private void loadMoodIntoQueue() {
		if(group==null)
			return;
		
		//clear out any cached upcoming resume mood
		Editor edit = mSettings.edit();
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
					notifyMoodsChanged();
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
