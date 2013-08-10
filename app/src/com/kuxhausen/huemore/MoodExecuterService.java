package com.kuxhausen.huemore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Event;
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

	MoodExecuterService me = this;
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
			bulbS = decodedValues.first;
			Mood m = decodedValues.second;
			for(Event e : m.events)
				queue.add(e);
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
	private Integer[] bulbS;
	int numSkips = 0;
	int time;
	
	PriorityQueue<Event> queue = new PriorityQueue<Event>();
	
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
				if(numSkips>0){
					numSkips--;
				} else if(queue.peek()==null){
					me.stopSelf();
				}
				else if(queue.peek().time<=time){
					//remove all events occuring at the same time
					//combine any events effecting same channel
					//execute all lists
					//skip num cycles according to num events executed
					ArrayList<Event> eList = new ArrayList<Event>();
					eList.add(queue.poll());
					while(queue.peek()!=null && queue.peek().compareTo(eList.get(0)) == 0){
						Event e = queue.poll();
						eList.add(e);
					}
					
					for(Event e : eList)
						NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), me, null, bulbS, e.state);
					
					numSkips += eList.size();
				
				}
			}

			@Override
			public void onFinish() {
				
			}
		};
		countDownTimer.start();

	}	
	
}
