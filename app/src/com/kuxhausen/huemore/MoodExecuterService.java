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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.util.Pair;

public class MoodExecuterService extends Service {

	MoodExecuterService me = this;
	private RequestQueue volleyRQ;
	int notificationId = 1337;
	
	public MoodExecuterService() {
	}

	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MoodExecuterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MoodExecuterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		volleyRQ = Volley.newRequestQueue(this);
		restartCountDownTimer();
		
		//TODO make forground like music player
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.huemore)
		        .setContentTitle(this.getResources().getString(R.string.app_name))
		        .setContentText("");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		//NotificationManager mNotificationManager =
		//    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		//mNotificationManager.notify(notificationId, mBuilder.build());
		this.startForeground(notificationId, mBuilder.build());
		
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
		String moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
		moodName = (moodName==null) ? "" : moodName;
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.huemore)
		        .setContentTitle(this.getResources().getString(R.string.app_name))
		        .setContentText(moodName);
		//NotificationManager mNotificationManager =
		//	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
		//	mNotificationManager.notify(notificationId, mBuilder.build());
			this.startForeground(notificationId, mBuilder.build());
		
			
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
					String moodName = me.getResources().getString(R.string.app_name);
					NotificationCompat.Builder mBuilder =
					        new NotificationCompat.Builder(me)
					        .setSmallIcon(R.drawable.huemore)
					        .setContentTitle(me.getResources().getString(R.string.app_name))
					        .setContentText("");
					//NotificationManager mNotificationManager =
					//	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// mId allows you to update the notification later on.
					//	mNotificationManager.notify(notificationId, mBuilder.build());
					me.startForeground(notificationId, mBuilder.build());
					
					
					me.stopSelf();
				}
				else if(queue.peek().time<=System.nanoTime()/1000000){
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
