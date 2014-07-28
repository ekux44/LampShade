package com.kuxhausen.huemore.state;

public class GroupMoodBrightness {

  public GroupMoodBrightness() {
  }

  public String group;
  public String mood;

  /**
   * may be null in serialization *
   * out of 100
   */
  public Integer brightness;
}
