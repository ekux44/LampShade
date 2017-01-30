package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

import android.support.annotation.IntRange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Mood implements Cloneable {

  public final static long NUMBER_OF_MILLISECONDS_IN_DAY = 86400000;
  public final static int NUM_LOOPS_INFINITE = 127;

  private Event[] mEvents;
  private int mNumChannels;
  private boolean mUsesTiming;
  /**
   * in units of 1/10 of a second
   */
  private int mLoopIterationTimeLength;
  /**
   * if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets
   * from mood start time
   */
  private boolean mRelativeToMidnight;
  /**
   * Number of times to loop back after playing the events. 127 means infinite.
   */
  @IntRange(from = 0, to = NUM_LOOPS_INFINITE)
  private int mNumLoops;

  private Mood() {
    // for Gson
  }

  private Mood(Event[] events, int loopIterationTimeLength, int numChannels, Integer numLoops,
               Boolean timeAddressingRepeatPolicy, boolean usesTiming) {
    this.mEvents = events;
    this.mLoopIterationTimeLength = loopIterationTimeLength;
    this.mNumChannels = numChannels;
    this.mNumLoops = numLoops;
    this.mRelativeToMidnight = timeAddressingRepeatPolicy;
    this.mUsesTiming = usesTiming;
  }

  public boolean isInfiniteLooping() {
    if (isRelativeToMidnight()) {
      return true;
    } else {
      return (mNumLoops == NUM_LOOPS_INFINITE);
    }
  }

  public int getNumLoops() {
    return mNumLoops;
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

  public boolean isRelativeToMidnight() {
    return mRelativeToMidnight;
  }

  public long getLoopMilliTime() {
    if (isRelativeToMidnight()) {
      return NUMBER_OF_MILLISECONDS_IN_DAY;
    } else {
      return mLoopIterationTimeLength * 100l;
    }
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
            && this.isInfiniteLooping() == ((Mood) obj).isInfiniteLooping()
            && this.getUsesTiming() == ((Mood) obj).getUsesTiming()
            && this.getLoopMilliTime() == ((Mood) obj).getLoopMilliTime()
            && this.isRelativeToMidnight() == ((Mood) obj)
        .isRelativeToMidnight());
  }

  public static class Builder {

    private Event[] mEvents;
    private int mLoopIterationTimeLength;
    private Integer mNumChannels;
    private int mNumLoops;
    private boolean mRelativeToMidnight;
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

    public Builder setRelativeToMidnight(boolean relativeToMidnight) {
      mRelativeToMidnight = relativeToMidnight;
      return this;
    }

    public Builder setInfiniteLooping(boolean infinite) {
      if (infinite) {
        mNumLoops = NUM_LOOPS_INFINITE;
      } else {
        mNumLoops = 0;
      }
      return this;
    }

    public Builder setLoopMilliTime(long milliseconds) {
      mLoopIterationTimeLength = (int) (milliseconds / 100l);
      return this;
    }

    public Builder setNumLoops(@IntRange(from = 0, to = NUM_LOOPS_INFINITE) int numLoops) {
      if (numLoops < 0 || numLoops > NUM_LOOPS_INFINITE) {
        throw new IllegalArgumentException();
      }
      mNumLoops = numLoops;
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
      if (mNumChannels == null) {
        mNumChannels = 0;
        for (Event e : mEvents) {
          if (e.getChannel() >= mNumChannels) {
            mNumChannels = e.getChannel() + 1; // channels are zero-indexed
          }
        }
      }

      return new Mood(mEvents, mLoopIterationTimeLength, mNumChannels, mNumLoops,
                      mRelativeToMidnight, mUsesTiming);
    }
  }
}
