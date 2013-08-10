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
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.state.api.BulbState;

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
		String encodedTransientMood = intent.getStringExtra(InternalArguments.ENCODED_TRANSIENT_MOOD);
		
		if(encodedMood != null){
			Pair<Integer[], Mood> decodedValues = HueUrlEncoder.decode(encodedMood);
			Integer[] bulbS = decodedValues.first;
			Mood m = decodedValues.second;
			
			ArrayList<Integer>[] channels = new ArrayList[m.numChannels];
			for(int i = 0; i<channels.length; i++)
				channels[i] = new ArrayList<Integer>();
			
			for(int i = 0; i<bulbS.length; i++){
				channels[i%m.numChannels].add(bulbS[i]);
			}
			
			
			for(Event e : m.events){
				e.time += (int)System.nanoTime()/1000000;//divide by 1 million to convert to millis
				for(Integer bNum : channels[e.channel]){
					QueueEvent qe = new QueueEvent(e);
					qe.bulb = bNum;
					queue.add(qe);
				}
			}
		}else if (encodedTransientMood!= null){
			Pair<Integer[], Mood> decodedValues = HueUrlEncoder.decode(encodedTransientMood);
			Integer[] bulbS = decodedValues.first;
			Mood m = decodedValues.second;
			
			ArrayList<Integer>[] channels = new ArrayList[m.numChannels];
			for(int i = 0; i<channels.length; i++)
				channels[i] = new ArrayList<Integer>();
			
			for(int i = 0; i<bulbS.length; i++){
				channels[i%m.numChannels].add(bulbS[i]);
			}
			
			
			for(Event e : m.events){
				for(Integer bNum : channels[e.channel]){
					BulbState toModify = transientStateChanges[bNum-1];
					toModify.merge(e.state);
					if(toModify.toString()!=e.state.toString())
						flagTransientChanges[bNum-1] = true;
				}
			}
			
			
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
	
	
	private BulbState[] transientStateChanges = new BulbState[50];
	{
		for(int i = 0; i<transientStateChanges.length; i++)
			transientStateChanges[i] = new BulbState();
	}
	//TODO use a datastructure that know if it has any set/true bits
	private boolean[] flagTransientChanges = new boolean[50];
	
	private boolean hasTransientChanges(){
		boolean result = false;
		for(boolean b : flagTransientChanges)
			result |= b;
		return result;
	}
	
	private CountDownTimer countDownTimer;
	int numSkips = 0;
	int time;
	
	PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
	
	public void restartCountDownTimer(){
		if(countDownTimer!=null)
			countDownTimer.cancel();
		
		
		Log.e("asdf", "count down timer interval rate = "+(1000/15));
		//runs at the rate to execute 15 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE,
				(1000/15)) {

			@Override
			public void onTick(long millisUntilFinished) {
				if(numSkips>0){
					numSkips--;
				} else if(hasTransientChanges()){
					int numTransientChanges = 0;
					for(int i = 0; i<50; i++)
						if(flagTransientChanges[i]){
							NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), me, null, i+1, transientStateChanges[i]);
							numTransientChanges++;
						}
					numSkips += numTransientChanges;
				}else if(queue.peek()==null){
				//	me.stopSelf();
				}
				else if(queue.peek().time<=time){
					//remove all events occuring at the same time
					//combine any events effecting same channel
					//execute all lists
					//skip num cycles according to num events executed
					ArrayList<QueueEvent> eList = new ArrayList<QueueEvent>();
					eList.add(queue.poll());
					while(queue.peek()!=null && queue.peek().compareTo(eList.get(0)) == 0){
						QueueEvent e = queue.poll();
						eList.add(e);
					}
					
					for(QueueEvent e : eList)
						NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), me, null, e.bulb, e.state);
					
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
