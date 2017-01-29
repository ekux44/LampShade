package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Mood implements Cloneable {

  public final static long NUMBER_OF_MILLISECONDS_IN_DAY = 86400000;

  private Event[] mEvents;
  private int numChannels;
  private boolean mUsesTiming;
  /**
   * in units of 1/10 of a second
   */
  private int loopIterationTimeLength;
  /**
   * if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets
   * from mood start time
   */
  private Boolean timeAddressingRepeatPolicy;
  /**
   * max value 126 (127 special cased to infinity) *
   */
  private Integer numLoops;

  private Mood() {
    // for Gson
  }

  private Mood(Event[] events, int loopIterationTimeLength, int numChannels, Integer numLoops,
               Boolean timeAddressingRepeatPolicy, boolean usesTiming) {
    this.mEvents = events;
    this.loopIterationTimeLength = loopIterationTimeLength;
    this.numChannels = numChannels;
    this.numLoops = numLoops;
    this.timeAddressingRepeatPolicy = timeAddressingRepeatPolicy;
    this.mUsesTiming = usesTiming;
  }

  public boolean isInfiniteLooping() {
    if (getTimeAddressingRepeatPolicy()) {
      return true;
    } else {
      return (numLoops == 127);
    }
  }

  public int getNumLoops() {
    return numLoops;
  }

  public int getNumChannels() {
    return Math.max(numChannels, 1);
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

  public boolean getTimeAddressingRepeatPolicy() {
    if (timeAddressingRepeatPolicy == null) {
      return false;
    }
    return timeAddressingRepeatPolicy;
  }

  public long getLoopMilliTime() {
    if (getTimeAddressingRepeatPolicy()) {
      return NUMBER_OF_MILLISECONDS_IN_DAY;
    } else {
      return loopIterationTimeLength * 100l;
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
            && this.getTimeAddressingRepeatPolicy() == ((Mood) obj)
        .getTimeAddressingRepeatPolicy());
  }

  public static class Builder {

    private Event[] mEvents;
    private int mLoopIterationTimeLength;
    private int mNumChannels;
    private Integer mNumLoops;
    private Boolean mTimeAddressingRepeatPolicy;
    private boolean mUsesTiming;

    public Builder() {
      mTimeAddressingRepeatPolicy = false;
      mNumChannels = 1;
      mNumLoops = 0;
      mEvents = new Event[0];
    }

    public Builder(BulbState bulbState) {
      mTimeAddressingRepeatPolicy = false;
      mNumChannels = 1;
      mNumLoops = 0;
      mEvents = new Event[]{new Event(bulbState, 0, 0l)};
    }

    public Builder setEvents(Event[] events) {
      if (events == null) {
        throw new IllegalArgumentException();
      }
      mEvents = events;
      return this;
    }

    public Builder setNumChannels(int num) {
      mNumChannels = num;
      return this;
    }

    public Builder setTimeAddressingRepeatPolicy(boolean dailyMode) {
      mTimeAddressingRepeatPolicy = dailyMode;
      return this;
    }

    public Builder setInfiniteLooping(boolean infinite) {
      if (infinite) {
        mNumLoops = 127;
      } else {
        mNumLoops = 0;
      }
      return this;
    }

    public Builder setLoopMilliTime(long milliseconds) {
      mLoopIterationTimeLength = (int) (milliseconds / 100l);
      return this;
    }

    public Builder setNumLoops(int num) {
      mNumLoops = Math.max(0, Math.min(127, num));
      return this;
    }

    public Builder setUsesTiming(boolean usesTiming) {
      mUsesTiming = usesTiming;
      return this;
    }

    public Mood build() {
      return new Mood(mEvents, mLoopIterationTimeLength, mNumChannels, mNumLoops,
                      mTimeAddressingRepeatPolicy, mUsesTiming);
    }
  }
}
