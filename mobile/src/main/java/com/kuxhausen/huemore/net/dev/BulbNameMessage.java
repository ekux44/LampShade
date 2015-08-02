package com.kuxhausen.huemore.net.dev;

public class BulbNameMessage {

  private long mId;
  private String mName;

  public BulbNameMessage(long bulbId, String newName) {
    mId = bulbId;
    mName = newName;
  }

  public long getId() {
    return mId;
  }

  public String getName() {
    return mName;
  }
}
