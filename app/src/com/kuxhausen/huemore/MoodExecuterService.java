package com.kuxhausen.huemore;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.kuxhausen.huemore.automation.FireReceiver;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.DeviceManager.OnStateChangedListener;
import com.kuxhausen.huemore.net.MoodPlayer;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;

public class MoodExecuterService extends Service implements OnActiveMoodsChangedListener, OnStateChangedListener{

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
	
	private boolean mBound;
	private WakeLock mWakelock;
	private DeviceManager mDeviceManager;
	private MoodPlayer mMoodPlayer;

	@Override
	public void onCreate() {
		super.onCreate();
		
		//acquire wakelock needed till everything initialized
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		mWakelock.acquire();
		
		//Initialize DeviceManager and Mood Player
		mDeviceManager = new DeviceManager(this);
		mDeviceManager.registerStateListener(this);
		mMoodPlayer = new MoodPlayer(this,mDeviceManager);
		mMoodPlayer.addOnActiveMoodsChangedListener(this);
		
	}
	
	@Override
	/** 
	 * Called after onCreate when service attaching to Activity(s)
	 */
	public IBinder onBind(Intent intent) {
		mBound = true;
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
	    super.onUnbind(intent);
		mBound = false;
	    return true; // ensures onRebind is called
	}

	@Override
	public void onRebind(Intent intent) {
	    super.onRebind(intent);
		mBound = true;
	}
	
	@Override
	/** 
	 * Called after onCreate when service (re)started independently
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//remove any possible launched wakelocks
			AlarmReciever.completeWakefulIntent(intent);
			FireReceiver.completeWakefulIntent(intent);

			
			String encodedMood = intent.getStringExtra(InternalArguments.ENCODED_MOOD);
			String groupName = intent.getStringExtra(InternalArguments.GROUP_NAME);
			String moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
			Integer maxBri = intent.getIntExtra(InternalArguments.MAX_BRIGHTNESS, -1);
			if(maxBri==-1)
				maxBri = null;
			
			if (encodedMood != null) {
				try{
					Pair<Integer[], Pair<Mood, Integer>> moodPairs = HueUrlEncoder.decode(encodedMood);
					
					if(moodPairs.second.first!=null){
						Group g = Group.loadFromLegacyData(moodPairs.first,groupName,this);
					
						
						moodName = (moodName == null) ? "Unknown Mood" : moodName;
						mMoodPlayer.playMood(g, moodPairs.second.first, moodName, moodPairs.second.second);
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
			} else if(moodName!=null && groupName!=null){
				Group g = Group.loadFromDatabase(groupName, this);
				Mood m = Utils.getMoodFromDatabase(moodName, this);
				
				mMoodPlayer.playMood(g, m, moodName, maxBri);
			}
		}
		calculateWakeNeeds();
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * don't call till after onCreate and near the end of onStartCommand so device doesn't sleep before launching mood events queued
	 */
	public void calculateWakeNeeds(){
		boolean shouldStayAwake = false;
		
		if(mMoodPlayer.hasImminentPendingWork())
			shouldStayAwake = true;
		
		Log.e("ccc","shoudlStayAwakeMood "+shouldStayAwake);
		
		
		for(Connection c : mDeviceManager.getConnections()){
			if(c.hasPendingWork())
				shouldStayAwake = true;
		}
		
		
		Log.e("ccc","shoudlStayAwakeM&D "+shouldStayAwake);
		
		if(shouldStayAwake){
			if(mWakelock == null){
				//acquire wakelock till done doing work
				PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
				mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
				mWakelock.acquire();
			}
		} else {
			if(!mBound){
				//not bound, so service may die after releasing wakelock
				//save ongoing moods
				mMoodPlayer.saveOngoingAndScheduleResores();
			}
			
			if(mWakelock!=null){
				mWakelock.release();
				mWakelock = null;
			}
			//if unbound, sleep 
		}
	}
	
	public MoodPlayer getMoodPlayer(){
		return mMoodPlayer;
	}
	public DeviceManager getDeviceManager(){
		return mDeviceManager;
	}
	
	@Override
	public void onActiveMoodsChanged(){
		createNotification();
		calculateWakeNeeds();
	}
	@Override
	public void onStateChanged() {
		calculateWakeNeeds();
	}
	
	public void createNotification() {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainFragment.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
	
	
	@Override
	public void onDestroy() {
		mMoodPlayer.onDestroy();
		mDeviceManager.onDestroy();
		if(mWakelock!=null)
			mWakelock.release();
		super.onDestroy();
	}
}
