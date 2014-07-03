package com.kuxhausen.huemore.state;

public class GroupMoodBrightness {

  public GroupMoodBrightness() {}

  public String group;
  public String mood;

  /** may be null in serialization **/
  public Integer brightness;
}
