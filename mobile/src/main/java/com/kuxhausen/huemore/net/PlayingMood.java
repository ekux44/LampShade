package com.kuxhausen.huemore.net;

import android.util.Pair;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * used to store activity data about an ongoing mood and format the data for consumption by
 * visualizations/notifications
 */
public class PlayingMood {


  private Mood mMood;
  private String mMoodName;
  private Group mGroup;
  /**
   * In elapsed realtime milliseconds
   */
  private long mStartTime;
  /**
   * In elapsed realtime milliseconds
   */
  private long mLastTickedTime;

  /**
   * @param startTime    in elapsed realtime milliseconds (may be negative)
   * @param dayStartTime in elapsed realtime milliseconds (may be negative)
   */
  public PlayingMood(Mood m, String moodName, Group g, long startTime, long dayStartTime,
                     Long internalProgress) {
    if (m == null || g == null) {
      throw new IllegalArgumentException();
    }

    mMood = m;
    if (moodName != null) {
      mMoodName = moodName;
    } else {
      mMoodName = "?";
    }
    mGroup = g;
    if (m.getTimeAddressingRepeatPolicy()) {
      mStartTime = dayStartTime;

      long[] lastTickedTimePerChannel = new long[m.getNumChannels()];
      Arrays.fill(lastTickedTimePerChannel, dayStartTime - 1);

      for (int numCycles = -1; numCycles < 1; numCycles++) {
        for (Event e : m.getEvents()) {

          long
              adjustedEventTime =
              e.getMilliTime() + dayStartTime + (numCycles * m.getLoopMilliTime());

          if (adjustedEventTime < lastTickedTimePerChannel[e.getChannel()]) {
            lastTickedTimePerChannel[e.getChannel()] = adjustedEventTime;
          }
        }
      }

      mLastTickedTime = dayStartTime - 1;
      for (long i : lastTickedTimePerChannel) {
        mLastTickedTime = Math.min(mLastTickedTime - 1, i);
      }
    } else {
      mStartTime = startTime;
      mLastTickedTime = startTime - 1;
    }

    if (internalProgress != null) {
      mLastTickedTime = internalProgress;
    }
  }

  public long getStartTime() {
    return mStartTime;
  }

  public long getInternalProgress() {
    return mLastTickedTime;
  }

  private List<Long> getChannelBulbIds(int channelNum) {
    ArrayList<Long> channel = new ArrayList<Long>();

    List<Long> bulbBaseIds = mGroup.getNetworkBulbDatabaseIds();
    for (int i = 0; i < bulbBaseIds.size(); i++) {
      if (i % mMood.getNumChannels() == channelNum) {
        channel.add(bulbBaseIds.get(i));
      }
    }

    return channel;
  }

  public boolean hasFutureEvents() {
    if (mMood.getEvents().length == 0) {
      return false;
    }
    if (mMood.getTimeAddressingRepeatPolicy()) {
      return true;
    }
    if (mMood.isInfiniteLooping()) {
      return true;
    }
    if ((mMood.getEvents()[mMood.getEvents().length - 1].getMilliTime() + mStartTime)
        > mLastTickedTime) {
      return true;
    }
    return false;
  }

  /**
   * @return anticipated tme of the next event, calculated in elapsed realtime milliseconds
   */
  public long getNextEventInCurrentMillis() {
    if (!hasFutureEvents()) {
      throw new IllegalStateException();
    }

    if (mMood.isInfiniteLooping()) {
      long
          cycleStart =
          mStartTime + ((mLastTickedTime - mStartTime) / mMood.getLoopMilliTime())
                       * mMood.getLoopMilliTime();
      for (int numCycles = 0; numCycles < 2; numCycles++) {
        for (Event e : mMood.getEvents()) {
          if (e.getMilliTime() + cycleStart + (numCycles * mMood.getLoopMilliTime())
              > mLastTickedTime) {
            return e.getMilliTime() + cycleStart + (numCycles * mMood.getLoopMilliTime());
          }
        }
      }
    } else {
      for (Event e : mMood.getEvents()) {
        if (e.getMilliTime() + mStartTime > mLastTickedTime) {
          return e.getMilliTime() + mStartTime;
        }
      }
    }

    throw new IllegalStateException();
  }

  /**
   * @param sinceTime   in elapsed realtime milliseconds
   * @param throughTime in elapsed realtime milliseconds
   */
  public List<Pair<List<Long>, BulbState>> getEventsSinceThrough(long sinceTime, long throughTime) {

    List<Pair<List<Long>, BulbState>> result = new ArrayList<Pair<List<Long>, BulbState>>();

    if (mMood.getTimeAddressingRepeatPolicy()) {
      int
          priorLoops =
          (int) Math.floor(((double) (sinceTime - mStartTime)) / mMood.getLoopMilliTime());

      for (int numCycles = priorLoops;
           mStartTime + (numCycles * mMood.getLoopMilliTime()) <= throughTime;
           numCycles++) {
        for (Event e : mMood.getEvents()) {
          if (sinceTime < (e.getMilliTime() + mStartTime + (numCycles * mMood.getLoopMilliTime()))
              && (e.getMilliTime() + mStartTime + (numCycles * mMood.getLoopMilliTime()))
                 <= throughTime) {
            result.add(new Pair<List<Long>, BulbState>(getChannelBulbIds(e.getChannel()),
                                                       e.getBulbState()));
          }
        }
      }

    } else if (mMood.isInfiniteLooping()) {
      int priorLoops = (int) Math.max(0, (sinceTime - mStartTime) / mMood.getLoopMilliTime());

      for (int numCycles = priorLoops;
           mStartTime + (numCycles * mMood.getLoopMilliTime()) <= throughTime;
           numCycles++) {
        for (Event e : mMood.getEvents()) {
          if (sinceTime < (e.getMilliTime() + mStartTime + (numCycles * mMood.getLoopMilliTime()))
              && (e.getMilliTime() + mStartTime + (numCycles * mMood.getLoopMilliTime()))
                 <= throughTime) {
            result.add(new Pair<List<Long>, BulbState>(getChannelBulbIds(e.getChannel()),
                                                       e.getBulbState()));
          }
        }
      }

    } else {
      for (Event e : mMood.getEvents()) {
        if (sinceTime < (e.getMilliTime() + mStartTime)
            && (e.getMilliTime() + mStartTime) <= throughTime) {
          result.add(
              new Pair<List<Long>, BulbState>(getChannelBulbIds(e.getChannel()), e.getBulbState()));
        }
      }
    }
    return result;
  }

  /**
   * @param throughTime in elapsed realtime milliseconds
   */
  public List<Pair<List<Long>, BulbState>> tick(long throughTime) {
    if (throughTime < mLastTickedTime) {
      throw new IllegalArgumentException(throughTime+",'"+mLastTickedTime);
    }

    long sinceT = mLastTickedTime;
    long throughT = throughTime;

    mLastTickedTime = throughTime;

    return getEventsSinceThrough(sinceT, throughT);
  }

  public String getMoodName() {
    return mMoodName;
  }

  public String getGroupName() {
    return mGroup.getName();
  }

  public String toString() {
    return getGroupName() + " \u2190 " + getMoodName();
  }

  public Group getGroup() {
    return mGroup;
  }

  public Mood getMood() {
    return mMood;
  }

}
