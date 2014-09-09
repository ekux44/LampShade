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
  public Integer time;

  @Override
  public int compareTo(Event another) {
    return time.compareTo(another.time);
  }

  public Event() {
  }

  public Event(BulbState state, Integer channel, Integer time) {
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

  public void setMilliTime(int milliseconds) {
    time = (int) (milliseconds / 100l);
  }

  public long getMilliTime() {
    if (time == null) {
      throw new IllegalStateException();
    }
    return time * 100l;
  }
}
