package com.kuxhausen.huemore.net;

import android.os.SystemClock;
import android.util.Log;

import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.QueueEvent;
import com.kuxhausen.huemore.timing.Conversions;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * used to store activity data about an ongoing mood and format the data for consumption by
 * visualizations/notefications
 */
public class PlayingMood {

  // if the next even is happening in less than 1/2 seconds, stay awake for it
  private final static long IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC = 1000l;

  /**
   * should never be null *
   */
  private Mood mood;
  private String moodName;
  private Group group;

  private MoodPlayer mChangedListener;
  private DeviceManager mDeviceManager;

  private BrightnessManager mBrightnessManager;

  private PriorityQueue<QueueEvent> queue = new PriorityQueue<QueueEvent>();
  private Long moodLoopIterationEndMiliTime = 0L;

  public PlayingMood(MoodPlayer mp, DeviceManager dm, Group g, Mood m, String mName,
                     Integer maxBri, Long timeStartedInRealtimeElapsedMilis) {
    mChangedListener = mp;
    mDeviceManager = dm;
    group = g;
    mood = m;
    moodName = mName;

    mBrightnessManager = dm.obtainBrightnessManager(g);

    if (timeStartedInRealtimeElapsedMilis != null) {
      if (mood.isInfiniteLooping()) {
        // if entire loops could have occurred since the timeStarm started, fast forward to the
        // currently ongoing loop
        while (timeStartedInRealtimeElapsedMilis < (SystemClock.elapsedRealtime() + (
            mood.loopIterationTimeLength * 100))) {
          timeStartedInRealtimeElapsedMilis += (mood.loopIterationTimeLength * 100);
        }
      }
    }

    loadMoodIntoQueue(timeStartedInRealtimeElapsedMilis);
  }

  public String getMoodName() {
    if (moodName != null) {
      return moodName;
    }
    return "?";
  }

  public String getGroupName() {
    if (group != null) {
      return group.getName();
    }
    return "?";
  }

  public String toString() {
    return getGroupName() + " \u2190 " + getMoodName();
  }

  // if null, parameter ignored
  private void loadMoodIntoQueue(Long timeLoopStartedInRealtimeElapsedMilis) {
    Log.d("mood", "loadMoodWithOffset" + ((timeLoopStartedInRealtimeElapsedMilis != null)
                                          ? timeLoopStartedInRealtimeElapsedMilis : "null"));

    ArrayList<Long>[] channels = new ArrayList[mood.getNumChannels()];
    for (int i = 0; i < channels.length; i++) {
      channels[i] = new ArrayList<Long>();
    }

    ArrayList<Long> bulbBaseIds = group.getNetworkBulbDatabaseIds();
    for (int i = 0; i < bulbBaseIds.size(); i++) {
      channels[i % mood.getNumChannels()].add(bulbBaseIds.get(i));
    }

    if (mood.timeAddressingRepeatPolicy) {
      Stack<QueueEvent> pendingEvents = new Stack<QueueEvent>();

      long earliestEventStillApplicable = Long.MIN_VALUE;

      for (int i = mood.events.length - 1; i >= 0; i--) {
        Event e = mood.events[i];
        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;

          qe.miliTime = Conversions.miliEventTimeFromMoodDailyTime(e.time);
          if (qe.miliTime > SystemClock.elapsedRealtime()) {
            pendingEvents.add(qe);
          } else if (qe.miliTime >= earliestEventStillApplicable) {
            earliestEventStillApplicable = qe.miliTime;
            qe.miliTime = SystemClock.elapsedRealtime();
            pendingEvents.add(qe);
          }
        }
      }

      if (earliestEventStillApplicable == Long.MIN_VALUE && mood.events.length > 0) {
        // haven't found a previous state to start with, time to roll over and add last evening
        // event
        Event e = mood.events[mood.events.length - 1];
        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;
          qe.miliTime = SystemClock.elapsedRealtime();
          pendingEvents.add(qe);
        }
      }

      while (!pendingEvents.empty()) {
        queue.add(pendingEvents.pop());
      }
    } else {
      for (Event e : mood.events) {

        for (Long bNum : channels[e.channel]) {
          QueueEvent qe = new QueueEvent(e);
          qe.bulbBaseId = bNum;

          // if no preset mood start time, use present time
          if (timeLoopStartedInRealtimeElapsedMilis == null) {
            timeLoopStartedInRealtimeElapsedMilis = SystemClock.elapsedRealtime();
          }
          qe.miliTime = timeLoopStartedInRealtimeElapsedMilis + (e.time * 100l);

          Log.d("mood", "qe event offset" + (qe.miliTime - SystemClock.elapsedRealtime()));

          // if event in future or present (+/- 100ms, add to queue
          if (qe.miliTime + 100 > SystemClock.elapsedRealtime()) {
            queue.add(qe);
          }
        }
      }
    }
    moodLoopIterationEndMiliTime =
        SystemClock.elapsedRealtime() + (mood.loopIterationTimeLength * 100l);
  }

  /**
   * @return false if done playing
   */
  public boolean onTick() {
    if (queue.peek() != null && queue.peek().miliTime <= SystemClock.elapsedRealtime()) {
      while (queue.peek() != null && queue.peek().miliTime <= SystemClock.elapsedRealtime()) {
        QueueEvent e = queue.poll();
        if (mDeviceManager.getNetworkBulb(e.bulbBaseId) != null) {
          mBrightnessManager.setState(mDeviceManager.getNetworkBulb(e.bulbBaseId), e.event.state);
        }
      }
    } else if (queue.peek() == null && mood.isInfiniteLooping()
               && SystemClock.elapsedRealtime() > moodLoopIterationEndMiliTime) {
      loadMoodIntoQueue(null);
    } else if (queue.peek() == null && !mood.isInfiniteLooping()) {
      return false;
    }
    return true;
  }

  public boolean hasImminentPendingWork() {
    //TODO fix
    return true;/*
    if(!queue.isEmpty()){
      Log.d("mood",queue.peek().miliTime + "");
      Log.d("mood",(queue.peek().miliTime - SystemClock.elapsedRealtime())+"");
    }

    // IF queue has imminent events or queue about to be reloaded
    if (!queue.isEmpty() && (queue.peek().miliTime - SystemClock.elapsedRealtime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC){
      return true;
    } else if(mood.isInfiniteLooping() && (moodLoopIterationEndMiliTime - SystemClock.elapsedRealtime()) < IMMIMENT_EVENT_WAKE_THRESHOLD_IN_MILISEC){
      return true;
    }
    return false;*/
  }

  public Group getGroup() {
    return group;
  }

  public Mood getMood() {
    return mood;
  }

  /**
   * @return next event time is millis referenced in SystemClock.elapsedRealtime()
   */
  public long getNextEventTime() {
    if (!queue.isEmpty()) {
      return queue.peek().miliTime;
    } else if (mood.isInfiniteLooping()) {
      return moodLoopIterationEndMiliTime;
    }
    return Long.MAX_VALUE;
  }
}
