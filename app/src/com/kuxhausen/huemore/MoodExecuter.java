package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.CountDownTimer;
import android.os.IBinder;

public class MoodExecuter extends Service {

	private RequestQueue volleyRQ;
	
	public MoodExecuter() {
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
		
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { mood };
		Cursor cursor = getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use the
																// default
																// content
																// URI
																// for the
																// provider.
				moodColumns, // Return the note ID and title for each note.
				MoodColumns.MOOD + "=?", // selection clause
				mWereClause, // election clause args
				null // Use the default sort order.
				);

		ArrayList<String> moodStates = new ArrayList<String>();
		while (cursor.moveToNext()) {
			moodStates.add(cursor.getString(0));
		}
		String[] moodS = moodStates.toArray(new String[moodStates.size()]);
		
		
		this.getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
//		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, moodS);
		
		// TODO clean up after development
		
		Intent intent = new Intent(this, MoodExecuter.class);
        startService(intent);
		
		
	}
}
