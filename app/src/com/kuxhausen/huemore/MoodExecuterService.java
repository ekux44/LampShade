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

public class MoodExecuterService extends Service {

	private RequestQueue volleyRQ;
	
	public MoodExecuterService() {
	}

	@Override
	public void onCreate(){
		super.onCreate();
		volleyRQ = Volley.newRequestQueue(this);
		
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
		restartCountDownTimer();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
		super.onDestroy();
	}
	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}
	
	
	private CountDownTimer countDownTimer;
	
	public void restartCountDownTimer(){
		if(countDownTimer!=null)
			countDownTimer.cancel();
		
		//runs at the rate to execute 20 op/sec
		countDownTimer = new CountDownTimer(5000,
				5000) {

			@Override
			public void onTick(long millisUntilFinished) {
				
			}

			@Override
			public void onFinish() {
				stopSelf();
			}
		};
		countDownTimer.start();

	}
	
	private String groupS;	
	private Integer[] bulbS;
	private String mood;
	
	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;
		
		Mood m = Utils.getMoodFromDatabase(mood, this);
		
		this.getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
//		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, moodS);
		
		// TODO clean up after development
		
		Intent intent = new Intent(this, MoodExecuterService.class);
        startService(intent);
		
		
	}
}
