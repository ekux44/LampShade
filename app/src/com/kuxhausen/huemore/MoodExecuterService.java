package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Mood;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

public class MoodExecuterService extends Service {

	private RequestQueue volleyRQ;
	
	public MoodExecuterService() {
	}

	@Override
	public void onCreate(){
		super.onCreate();
		volleyRQ = Volley.newRequestQueue(this);
		restartCountDownTimer();
		
		Notification notification = new Notification(R.drawable.huemore, "placeholder text",
		        System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "other placeholder",
		        "3rd placeholder", pendingIntent);
		startForeground(-1, notification);
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		//throw new UnsupportedOperationException("Not yet implemented");
		return null;
	}
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId){
		
		String encodedMood = intent.getStringExtra(InternalArguments.ENCODED_MOOD);
		if(encodedMood != null){
			Pair<Integer[], Mood> decodedValues = HueUrlEncoder.decode(encodedMood);
			previewStates = decodedValues.second;
			bulbS = decodedValues.first;
			hasChanged = true;
			
		}
		restartCountDownTimer();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		countDownTimer.cancel();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
		super.onDestroy();
	}
	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}
	
	
	private CountDownTimer countDownTimer;
	private boolean hasChanged = false;
	private Mood previewStates;
	private Integer[] bulbS;
	
	public void restartCountDownTimer(){
		if(countDownTimer!=null)
			countDownTimer.cancel();
		
		int numBulbs = 1;
		if(bulbS!=null)
			numBulbs = bulbS.length;
		
		Log.e("asdf", "count down timer interval rate = "+50*numBulbs);
		//runs at the rate to execute 20 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE,
				50*(numBulbs)) {

			@Override
			public void onTick(long millisUntilFinished) {
				if(hasChanged){
					testMood(previewStates);
					hasChanged = false;	
				}
			}

			@Override
			public void onFinish() {
				// try one last time
				if(hasChanged){
					testMood(previewStates);
					hasChanged = false;
				}
			}
		};
		countDownTimer.start();

	}	
	
	public void testMood(Mood m) {
		this.getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, null, bulbS, m);
	}
}
