package com.kuxhausen.huemore.state;

import com.google.gson.Gson;

import com.kuxhausen.huemore.persistence.Utils;

public class Event implements Comparable<Event> {

  private BulbState mState;
  /**
   * 0 indexed *
   */
  private int mChannel;
  /**
   * in units of 1/10 of a second
   */
  private int mDeciseconds;

  @Override
  public int compareTo(Event another) {
    return ((Integer) mDeciseconds).compareTo(another.mDeciseconds);
  }

  public Event(BulbState state, int channel, long milliseconds) {
    if (state == null) {
      throw new IllegalArgumentException();
    }

    mState = state;
    mChannel = channel;
    this.mDeciseconds = Utils.toDeciSeconds(milliseconds);
  }

  @Override
  public Event clone() {
    Gson gson = new Gson();
    return gson.fromJson(gson.toJson(this), Event.class);
  }

  public long getMilliTime() {
    return Utils.fromDeciSeconds(mDeciseconds);
  }

  /**
   * in units of 1/10 of a second
   */
  public int getLegacyTime() {
    return (mDeciseconds);
  }

  public BulbState getBulbState() {
    return mState;
  }

  public int getChannel() {
    return mChannel;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Event)) {
      throw new IllegalArgumentException();
    }

    return (this.getBulbState().equals(((Event) obj).getBulbState())
            && (this.getChannel() == ((Event) obj).getChannel())
            && (this.getMilliTime() == ((Event) obj).getMilliTime()));
  }
}
