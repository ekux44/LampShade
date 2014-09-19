package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

public class Event implements Comparable<Event> {

  private BulbState mState;
  /**
   * 0 indexed *
   */
  private int mChannel;
  /**
   * in units of 1/10 of a second
   */
  private long mDeciseconds;

  @Override
  public int compareTo(Event another) {
    return ((Long) mDeciseconds).compareTo(another.mDeciseconds);
  }

  public Event(BulbState state, int channel, long milliseconds) {
    if (state == null) {
      throw new IllegalArgumentException();
    }

    mState = state;
    mChannel = channel;
    this.mDeciseconds = milliseconds / 100l;
  }

  @Override
  public Event clone() {
    Gson gson = new Gson();
    return gson.fromJson(gson.toJson(this), Event.class);
  }

  public long getMilliTime() {
    return mDeciseconds * 100l;
  }

  /**
   * in units of 1/10 of a second
   */
  public int getLegacyTime() {
    return ((Long) mDeciseconds).intValue();
  }

  public BulbState getBulbState() {
    return mState;
  }

  public int getChannel() {
    return mChannel;
  }
}
