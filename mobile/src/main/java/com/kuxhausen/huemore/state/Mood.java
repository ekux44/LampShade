package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.HashSet;

public class Mood implements Cloneable {

  public Event[] events;
  private int numChannels;
  public Boolean usesTiming;
  /**
   * in units of 1/10 of a second
   */
  public int loopIterationTimeLength;
  /**
   * if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets
   * from mood start time
   */
  public Boolean timeAddressingRepeatPolicy;
  /**
   * max value 126 (127 special cased to infinity) *
   */
  private Integer numLoops;

  public Mood() {
    timeAddressingRepeatPolicy = false;
    usesTiming = false;
    numChannels = 1;
    numLoops = 0;
    events = new Event[0];
  }

  public void setInfiniteLooping(boolean infinite) {
    if (infinite) {
      numLoops = 127;
    }
  }

  public boolean isInfiniteLooping() {
    return (numLoops == 127);
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
    if (events == null) {
      return result;
    }
    HashSet<Integer> times = new HashSet<Integer>();
    for (Event e : events) {
      if (e != null && e.time != null && times.add(e.time)) {
        result++;
      }
    }
    return result;
  }

  public BulbState[][] getEventStatesAsSparseMatrix() {
    int maxCol = getNumChannels();
    int maxRow = getNumTimeslots();

    HashMap<Integer, Integer> timeslotMapping = new HashMap<Integer, Integer>();
    BulbState[][] colorGrid = new BulbState[maxRow][maxCol];
    int curRow = -1;
    for (Event e : events) {
      if (!timeslotMapping.containsKey(e.time)) {
        timeslotMapping.put(e.time, ++curRow);
      }
      colorGrid[timeslotMapping.get(e.time)][e.channel] = e.state;
    }

    return colorGrid;
  }

  public boolean isSimple() {
    if (events == null) {
      return true;
    }
    for (Event e : events) {
      if (e != null && e.time != null && e.time != 0) {
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
}
