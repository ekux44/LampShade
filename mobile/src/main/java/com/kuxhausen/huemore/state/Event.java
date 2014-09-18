package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

public class Event implements Comparable<Event> {

  private BulbState state;
  /**
   * 0 indexed *
   */
  public Integer channel;
  /**
   * in units of 1/10 of a second
   */
  private long time;

  @Override
  public int compareTo(Event another) {
    return ((Long) time).compareTo(another.time);
  }

  public Event() {
  }

  public Event(BulbState state, Integer channel, long time) {
    this.state = state;
    this.channel = channel;
    this.time = time;
  }

  @Override
  public Event clone() {
    Gson gson = new Gson();
    return gson.fromJson(gson.toJson(this), Event.class);
  }

  public Event(BulbState state, int channel) {
    this(state, channel, 0l);
  }

  public void setMilliTime(long milliseconds) {
    time = milliseconds / 100l;
  }

  public long getMilliTime() {
    return time * 100l;
  }

  /**
   * in units of 1/10 of a second
   */
  public int getLegacyTime() {
    return ((Long) time).intValue();
  }

  public BulbState getBulbState() {
    return state;
  }
}
