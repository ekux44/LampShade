package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.timing.Conversions;

/** used to store activity data about an ongoing mood and format the data for consumption by visualizations/notefications **/
public class PlayingMood {
	
	// if the next even is happening in less than 5 seconds, stay awake for it
	private final static long IMMIMENT_EVENT_WAKE_THRESHOLD_IN_NANOSEC = 5000000000l;
	
	private Mood mood;
	private String moodName;
	private Group group;	
	
	public Integer initialMaxBri; // careful this might go stale after playing starts
	
	private MoodPlayer mChangedListener;
	private DeviceManager mDeviceManager;
	
	private PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
	private Long moodLoopIterationEndNanoTime = 0L;
		
	public PlayingMood(MoodPlayer mp, DeviceManager dm, Group g, Mood m, String mName, Integer maxBri){
		mChangedListener = mp;
		mDeviceManager = dm;
		group = g;
		mood = m;
		moodName = mName;
		
		initialMaxBri = maxBri;
		
		loadMoodIntoQueue();
	}
	
	public String getMoodName(){
		if(moodName!=null)
			return moodName;
		return "?";
	}
	public String getGroupName(){
		if(group!=null)
			return group.getName();
		return "?";
	}
	
	public String toString(){
		return getGroupName()+" \u2190 "+getMoodName();
	}
	
	private void loadMoodIntoQueue() {
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
	
	public void onTick(){
		if (queue.peek()!=null && queue.peek().nanoTime <= System.nanoTime()) {
			while(queue.peek()!=null && queue.peek().nanoTime <= System.nanoTime())
			{
				QueueEvent e = queue.poll();
				mDeviceManager.getNetworkBulb(e.bulbBaseId).setState(e.event.state);
			}
		} else if (queue.peek() == null && mood != null && mood.isInfiniteLooping() && System.nanoTime()>moodLoopIterationEndNanoTime) {
			loadMoodIntoQueue();
		} else if (queue.peek() == null && mood != null && !mood.isInfiniteLooping()){
			mChangedListener.cancelMood(group);
		}
	}
	
	public boolean hasImminentPendingWork() {
		//IF queue has imminent events or queue about to be reloaded
		if((!queue.isEmpty() && (queue.peek().nanoTime - System.nanoTime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_NANOSEC)
			|| (queue.peek() == null && mood != null && mood.isInfiniteLooping() && System.nanoTime()>moodLoopIterationEndNanoTime))
			return true;
		return false;
	}
	
	public Group getGroup(){
		return group;
	}
}
