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
import android.util.Pair;

import com.android.volley.RequestQueue;
import com.kuxhausen.huemore.automation.FireReceiver;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.MoodPlayer;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;

public class MoodExecuterService extends Service implements OnActiveMoodsChangedListener{

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
	
	private WakeLock mWakelock;
	private DeviceManager mDeviceManager;
	private MoodPlayer mMoodPlayer;
	
	public MoodPlayer getMoodPlayer(){
		return mMoodPlayer;
	}
	public DeviceManager getDeviceManager(){
		return mDeviceManager;
	}
	
	@Override
	public void onActiveMoodsChanged(){
		createNotification();
	}
	
	public void createNotification() {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
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
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		//acquire wakelock
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		mWakelock.acquire();
		
		//Initialize DeviceManager and Mood Player
		mDeviceManager = new DeviceManager(this);
		mMoodPlayer = new MoodPlayer(this,mDeviceManager);
		
	}
	@Override
	public void onDestroy() {
		mMoodPlayer.onDestroy();
		mDeviceManager.onDestroy();
		if(mWakelock!=null)
			mWakelock.release();
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			//remove any possible launched wakelocks
			AlarmReciever.completeWakefulIntent(intent);
			FireReceiver.completeWakefulIntent(intent);

			//if doesn't already have a wakelock, acquire one
			if(this.mWakelock==null){
				//acquire wakelock
				PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
				mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
				mWakelock.acquire();
			}
			
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
		return super.onStartCommand(intent, flags, startId);
	}
}
