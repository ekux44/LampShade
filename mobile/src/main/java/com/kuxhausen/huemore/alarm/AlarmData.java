package com.kuxhausen.huemore.alarm;

import android.content.ContentValues;

public class AlarmData {

  private long mId;
  private String mMoodName;
  private String mGroupName;
  private Integer mBrightness;
  private boolean mIsEnabled;
  private DaysOfWeek mRepeatDays;

  //in
  private Long mNextAlarm;

  public AlarmData(long databaseId) {
    mId = databaseId;
    mRepeatDays = new DaysOfWeek();
  }

  public AlarmData(ContentValues cv){
    //todo do stuff
  }

  public AlarmData(long id, String mood, String group, Integer brightness, boolean enabled, DaysOfWeek days, long nextAlarm){
    mId = id;
    mMoodName = mood;
    mGroupName = group;
    mBrightness = brightness;
    mIsEnabled = enabled;
    mRepeatDays = days;
    mNextAlarm = mNextAlarm;
  }

  public long getId(){
    return mId;
  }

  public void setMoodName(String name) {
    mMoodName = name;
  }

  public String getMoodName() {
    return mMoodName;
  }

  public void setGroupName(String name) {
    mGroupName = name;
  }

  public String getGroupName() {
    return mGroupName;
  }

  public void setBrightness(Integer brightness) {
    mBrightness = brightness;
  }

  public Integer getBrightness() {
    return mBrightness;
  }

  public void setEnabled(boolean enabled) {
    mIsEnabled = enabled;
  }

  public boolean isEnabled() {
    return mIsEnabled;
  }

  public void setRepeatDays(DaysOfWeek days) {
    if (days == null) {
      throw new IllegalArgumentException();
    }
    mRepeatDays = days;
  }

  public DaysOfWeek getRepeatDays() {
    return mRepeatDays;
  }

  public long get

}
