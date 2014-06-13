package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import alt.android.os.CountDownTimer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.timing.Conversions;

public class MoodPlayer{

	// if the next even is happening in less than 5 seconds, stay awake for it
	private final static long IMMIMENT_EVENT_WAKE_THRESHOLD_IN_NANOSEC = 5000000000l;
	private Context mContext;
	private DeviceManager mDeviceManager;
	private ArrayList<OnActiveMoodsChangedListener> moodsChangedListeners = new ArrayList<OnActiveMoodsChangedListener>();
	
	public MoodPlayer(Context c, DeviceManager m){	
		mContext = c;
		mDeviceManager = m;
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	public void playMood(Group g, Mood m, String mName, Integer maxBri){
		moodName = mName;
		mood = m;
		group = g;
		
		//TODO do something with maxBri
		
		queue.clear();
		loadMoodIntoQueue();
		restartCountDownTimer();
		
		//update notifications
		notifyMoodsChanged();
	}
	public void cancelMood(Group g){
		mood = null;
		queue.clear();
		
		//update notifications
		notifyMoodsChanged();
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
	
	}
	
	public Mood mood;
	private String moodName;
	public String getGroupName(){
		if(group!=null)
			return group.getName();
		return "";
	}
	public String getMoodName(){
		return moodName;
	}
	PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
	Group group;
	private SharedPreferences mSettings;
	Long moodLoopIterationEndNanoTime = 0L;
	private static CountDownTimer countDownTimer;
	private final static int MOODS_TIMES_PER_SECOND = 10;
	
	//rewrite
	private void loadMoodIntoQueue() {
		//clear out any cached upcoming resume mood
		Editor edit = mSettings.edit();
		edit.putString(PreferenceKeys.CACHED_EXECUTING_ENCODED_MOOD,"");
		edit.commit();
		
		ArrayList<Long>[] channels = new ArrayList[mood.getNumChannels()];
		for (int i = 0; i < channels.length; i++)
			channels[i] = new ArrayList<Long>();

		ArrayList<Long> bulbBaseIds = group.getNetworkBulbDatabaseIds();
		for (int i = 0; i < bulbBaseIds.size(); i++) {
			channels[i % mood.getNumChannels()].add(bulbBaseIds.get(i));
		}

		if(mood.timeAddressingRepeatPolicy){
			Stack<QueueEvent> pendingEvents = new Stack<QueueEvent>();
			
			long earliestEventStillApplicable = Long.MIN_VALUE;
			
			for (int i= mood.events.length-1; i>=0; i--) {
				Event e = mood.events[i];
				for (Long bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulbBaseId = bNum;
					
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
				for (Long bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulbBaseId = bNum;
					qe.nanoTime = System.nanoTime();
					pendingEvents.add(qe);
				}
			}
			
			while(!pendingEvents.empty()){
				queue.add(pendingEvents.pop());
			}
		}else{
			for (Event e : mood.events) {
				for (Long bNum : channels[e.channel]) {
					QueueEvent qe = new QueueEvent(e);
					qe.bulbBaseId = bNum;
					
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

		// runs at the rate to execute 10 times per second
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / MOODS_TIMES_PER_SECOND)) {

			@Override
			public void onFinish() {
			}

			@Override
			public void onTick(long millisUntilFinished) {
				if (queue.peek()!=null && queue.peek().nanoTime <= System.nanoTime()) {
					while(queue.peek()!=null && queue.peek().nanoTime <= System.nanoTime())
					{
						QueueEvent e = queue.poll();
						mDeviceManager.getNetworkBulb(e.bulbBaseId).setState(e.event.state);
					}
				} else if (queue.peek() == null && mood != null && mood.isInfiniteLooping() && System.nanoTime()>moodLoopIterationEndNanoTime) {
					loadMoodIntoQueue();
				} else if (queue.peek() == null && mood != null && !mood.isInfiniteLooping()){
					moodName = null;
					notifyMoodsChanged();
				}
			}
		};
		countDownTimer.start();
	}

	public boolean hasImminentPendingWork() {
		//IF queue has imminent events or queue about to be reloaded
		if((!queue.isEmpty() && (queue.peek().nanoTime - System.nanoTime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_NANOSEC)
			|| (queue.peek() == null && mood != null && mood.isInfiniteLooping() && System.nanoTime()>moodLoopIterationEndNanoTime))
			return true;
		return false;
	}

	/**
	 * to save power, service is shutting down so ongoing moods should be schedule to be restarted in time for their next events
	 */
	public void saveOngoingAndScheduleResores() {
		// TODO Auto-generated method stub
	}
}
