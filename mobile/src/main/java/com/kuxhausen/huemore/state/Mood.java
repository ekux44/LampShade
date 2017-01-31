package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Mood implements Cloneable {

  public final static long NUMBER_OF_MILLISECONDS_IN_DAY = 86400000;

  private Event[] mEvents;
  private int mNumChannels;
  private boolean mUsesTiming;
  /**
   * in units of 1/10 of a second
   */
  private int mLoopIterationTimeLength;
  private TimingPolicy mTimingPolicy;

  public enum TimingPolicy {
    BASIC, // play the mood relative to start time, no looping
    LOOPING, // play the mood relative to start time, looping
    DAILY, // play the mood relative to start of day, looping
  }

  private Mood() {
    // for Gson
  }

  private Mood(Event[] events, int loopIterationTimeLength, int numChannels,
               TimingPolicy timingPolicy, boolean usesTiming) {
    this.mEvents = events;
    this.mLoopIterationTimeLength = loopIterationTimeLength;
    this.mNumChannels = numChannels;
    this.mTimingPolicy = timingPolicy;
    this.mUsesTiming = usesTiming;
  }

  public int getNumChannels() {
    return Math.max(mNumChannels, 1);
  }

  @Override
  public Mood clone() {
    Gson gson = new Gson();
    return gson.fromJson(gson.toJson(this), Mood.class);
  }

  public int getNumTimeslots() {
    int result = 0;
    if (mEvents == null) {
      return result;
    }
    HashSet<Integer> times = new HashSet<Integer>();
    for (Event e : mEvents) {
      if (e != null && times.add(e.getLegacyTime())) {
        result++;
      }
    }
    return result;
  }

  public Event[] getEvents() {
    return mEvents;
  }

  public BulbState[][] getEventStatesAsSparseMatrix() {
    int maxCol = getNumChannels();
    int maxRow = getNumTimeslots();

    HashMap<Integer, Integer> timeslotMapping = new HashMap<Integer, Integer>();
    BulbState[][] colorGrid = new BulbState[maxRow][maxCol];
    int curRow = -1;
    for (Event e : mEvents) {
      if (!timeslotMapping.containsKey(e.getLegacyTime())) {
        timeslotMapping.put(e.getLegacyTime(), ++curRow);
      }
      colorGrid[timeslotMapping.get(e.getLegacyTime())][e.getChannel()] = e.getBulbState();
    }

    return colorGrid;
  }

  public boolean isSimple() {
    if (mEvents == null) {
      return true;
    }
    for (Event e : mEvents) {
      if (e != null && e.getLegacyTime() != 0) {
        return false;
      }
    }
    return true;
  }


  public long getLoopMilliTime() {
    if (mTimingPolicy == TimingPolicy.DAILY) {
      return NUMBER_OF_MILLISECONDS_IN_DAY;
    } else {
      return mLoopIterationTimeLength * 100l;
    }
  }

  public TimingPolicy getTimingPolicy() {
    return mTimingPolicy;
  }

  public boolean getUsesTiming() {
    return mUsesTiming;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Mood)) {
      throw new IllegalArgumentException();
    }

    return (Arrays.equals(this.getEvents(), ((Mood) obj).getEvents())
            && this.getNumChannels() == ((Mood) obj).getNumChannels()
            && this.getTimingPolicy() == ((Mood) obj).getTimingPolicy()
            && this.getUsesTiming() == ((Mood) obj).getUsesTiming()
            && this.getLoopMilliTime() == ((Mood) obj).getLoopMilliTime());
  }

  public static class Builder {

    private Event[] mEvents;
    private int mLoopIterationTimeLength;
    private Integer mNumChannels;
    private TimingPolicy mTimingPolicy;
    private boolean mUsesTiming;

    public Builder() {
    }

    public Builder(BulbState bulbState) {
      mNumChannels = 1;
      mEvents = new Event[]{new Event(bulbState, 0, 0l)};
    }

    public Builder setEvents(Event[] events) {
      if (events == null || events.length < 1 || events[0] == null) {
        throw new IllegalArgumentException();
      }
      mEvents = events;
      return this;
    }

    public Builder setNumChannels(int num) {
      mNumChannels = num;
      return this;
    }

    public Builder setLoopMilliTime(long milliseconds) {
      mLoopIterationTimeLength = (int) (milliseconds / 100l);
      return this;
    }

    public Builder setTimingPolicy(TimingPolicy timingPolicy) {
      mTimingPolicy = timingPolicy;
      return this;
    }

    public Builder setUsesTiming(boolean usesTiming) {
      mUsesTiming = usesTiming;
      return this;
    }

    public Mood build() {
      // Lazy init default parameters
      if (mEvents == null) {
        throw new IllegalStateException();
      }
      if (mTimingPolicy == null) {
        mTimingPolicy = TimingPolicy.BASIC;
      }
      if (mNumChannels == null) {
        mNumChannels = 0;
        for (Event e : mEvents) {
          if (e.getChannel() >= mNumChannels) {
            mNumChannels = e.getChannel() + 1; // channels are zero-indexed
          }
        }
      }

      return new Mood(mEvents, mLoopIterationTimeLength, mNumChannels, mTimingPolicy, mUsesTiming);
    }
  }
}
