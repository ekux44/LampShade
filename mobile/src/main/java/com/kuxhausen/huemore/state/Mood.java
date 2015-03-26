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

  public Mood() {
    timeAddressingRepeatPolicy = false;
    numChannels = 1;
    numLoops = 0;
    mEvents = new Event[0];
  }

  public Mood(BulbState simple) {
    timeAddressingRepeatPolicy = false;
    numChannels = 1;
    numLoops = 0;
    mEvents = new Event[]{new Event(simple, 0, 0l)};
  }

  public void setInfiniteLooping(boolean infinite) {
    if (infinite) {
      numLoops = 127;
    } else {
      numLoops = 0;
    }
  }

  public boolean isInfiniteLooping() {
    if (getTimeAddressingRepeatPolicy()) {
      return true;
    } else {
      return (numLoops == 127);
    }
  }

  public void setNumLoops(int num) {
    numLoops = Math.max(0, Math.min(127, num));
  }

  public int getNumLoops() {
    return numLoops;
  }

  public int getNumChannels() {
    return Math.max(numChannels, 1);
  }

  public void setNumChannels(int num) {
    numChannels = num;
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

  public void setEvents(Event[] events) {
    if (events == null) {
      throw new IllegalArgumentException();
    }
    mEvents = events;
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

  public void setTimeAddressingRepeatPolicy(boolean dailyMode) {
    timeAddressingRepeatPolicy = dailyMode;
  }

  public long getLoopMilliTime() {
    if (getTimeAddressingRepeatPolicy()) {
      return NUMBER_OF_MILLISECONDS_IN_DAY;
    } else {
      return loopIterationTimeLength * 100l;
    }
  }

  public void setLoopMilliTime(long milliseconds) {
    loopIterationTimeLength = (int) (milliseconds / 100l);
  }

  public void setUsesTiming(boolean usesTiming) {
    mUsesTiming = usesTiming;
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
}
