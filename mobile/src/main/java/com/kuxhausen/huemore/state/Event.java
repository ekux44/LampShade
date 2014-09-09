package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

public class Event implements Comparable<Event> {

  public BulbState state;
  /**
   * 0 indexed *
   */
  public Integer channel;
  /**
   * in units of 1/10 of a second
   */
  private Long time;

  @Override
  public int compareTo(Event another) {
    return time.compareTo(another.time);
  }

  public Event() {
  }

  public Event(BulbState state, Integer channel, Long time) {
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
    this.state = state;
    this.channel = channel;
    this.time = null;
  }

  public void setMilliTime(long milliseconds) {
    time = milliseconds / 100l;
  }

  public long getMilliTime() {
    if (time == null) {
      throw new IllegalStateException();
    }
    return time * 100l;
  }

  /**
   * in units of 1/10 of a second
   */
  public Integer getLegacyTime() {
    if (time == null) {
      return null;
    } else {
      return time.intValue();
    }
  }

  public void setLegacyTime(Integer t) {
    if (t == null) {
      time = null;
    } else {
      time = (long) t;
    }
  }
}
