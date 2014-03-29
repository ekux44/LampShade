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
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.MoodPlayer;
import com.kuxhausen.huemore.network.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.network.OnConnectionStatusChangedListener;
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

public class MoodExecuterService extends Service implements ConnectionMonitor, OnBulbAttributesReturnedListener, OnActiveMoodsChangedListener{

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		MoodExecuterService getService() {
			// Return this instance of LocalService so clients can call public methods
			return MoodExecuterService.this;
		}
	}
	
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	private final static int notificationId = 1337;
	
	WakeLock wakelock;
	private boolean hasHubConnection = false;
	public ArrayList<OnConnectionStatusChangedListener> connectionListeners = new ArrayList<OnConnectionStatusChangedListener>();
	
	public ArrayList<OnBrightnessChangedListener> brightnessListeners = new ArrayList<OnBrightnessChangedListener>();
	
	private DeviceManager mDeviceManager;
	private MoodPlayer mMoodPlayer;
	
	public synchronized void onGroupSelected(int[] bulbs, Integer optionalBri, String groupName){
		mMoodPlayer.onGroupSelected(bulbs, optionalBri, groupName);
	}
	/** doesn't notify listeners **/
	public synchronized void setBrightness(int brightness){
		mMoodPlayer.setBrightness(brightness);
	}

	public interface OnBrightnessChangedListener {
		public void onBrightnessChanged(int brightness);
	}
	
	/** announce brightness to any listeners **/
	public void onBrightnessChanged(){
		for(OnBrightnessChangedListener l : brightnessListeners){
			l.onBrightnessChanged(mMoodPlayer.getMaxBrightness());
		}
	}
	public void registerBrightnessListener(OnBrightnessChangedListener l){
		if(mMoodPlayer.getMaxBrightness()!=null)
			l.onBrightnessChanged(mMoodPlayer.getMaxBrightness());
		brightnessListeners.add(l);
	}
	
	public void removeBrightnessListener(OnBrightnessChangedListener l){
		brightnessListeners.remove(l);
	}
	
	public void startMood(Mood m, String moodName){
		mMoodPlayer.startMood(m, moodName);
	}
	public void stopMood(){
		mMoodPlayer.stopMood();
	}
	
	@Override
	public void onAttributesReturned(BulbAttributes result, int bulbNumber) {
		mMoodPlayer.onAttributesReturned(result, bulbNumber);
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
	
	@Override
	public void onActiveMoodsChanged(){
		createNotification();
	}
	
	public void createNotification() {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String secondaryText = ((mMoodPlayer.getGroupName()!=null&&mMoodPlayer.getMoodName()!=null)?mMoodPlayer.getGroupName():"") + ((mMoodPlayer.getMoodName()!=null)?(" \u2192 " +mMoodPlayer.getMoodName()):"");
		
		
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
		return mMoodPlayer.getRequestQueue();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//acquire wakelock
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		wakelock.acquire();
		
		//Initialize DeviceManager and Mood Player
		mDeviceManager = new DeviceManager(this);
		mMoodPlayer = new MoodPlayer(this,mDeviceManager);
		
		//start pinging to test connectivity
		NetworkMethods.PreformGetBulbList(this, null);
	}
	@Override
	public void onDestroy() {
		mMoodPlayer.onDestroy();
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
			
			String encodedMood = intent.getStringExtra(InternalArguments.ENCODED_MOOD);

			try{
				if (encodedMood != null) {
					Pair<Integer[], Pair<Mood, Integer>> moodPairs = HueUrlEncoder.decode(encodedMood);
					
					if(moodPairs.first!=null && moodPairs.first.length>0){
						int[] bulbs = new int[moodPairs.first.length];
						for(int i = 0; i< bulbs.length; i++)
							bulbs[i] = moodPairs.first[i];
						String groupName = intent.getStringExtra(InternalArguments.GROUP_NAME);
						onGroupSelected(bulbs, moodPairs.second.second, groupName);
					}
					if(moodPairs.second.first!=null){
						String moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
						moodName = (moodName == null) ? "Unknown Mood" : moodName;
						startMood(moodPairs.second.first, moodName);
					}
					
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


}
