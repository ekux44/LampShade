package com.kuxhausen.huemore.alarm;

import android.content.ContentValues;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;

public class AlarmData {

  // must be kept in sync with AlarmData constructor
  public final static String[] QUERY_COLUMNS = {
      AlarmColumns._ID,
      AlarmColumns.COL_GROUP_NAME,
      AlarmColumns.COL_MOOD_NAME,
      AlarmColumns.COL_BRIGHTNESS,
      AlarmColumns.COL_IS_ENABLED,
      AlarmColumns.COL_REPEAT_DAYS,
      AlarmColumns.COL_HOUR,
      AlarmColumns.COL_MINUTE,
      AlarmColumns.COL_NEXT_TIME
  };

  private long mId;

  private String mMoodName;
  private String mGroupName;
  private Integer mBrightness;
  private boolean mIsEnabled;
  private DaysOfWeek mRepeatDays;
  private int mHour; //using 24 hour time
  private int mMinute;
  private Long mNextTime;

  public AlarmData(long databaseId) {
    mId = databaseId;
    mRepeatDays = new DaysOfWeek();
  }

  /**
   * @param cursor already moved to the relevant row, ordered according to AlarmData.QUERY_COLUMNS
   */
  public AlarmData(Cursor cursor) {
    mId = cursor.getLong(0);

    setMoodName(cursor.getString(1));

    setGroupName(cursor.getString(2));

    if (!cursor.isNull(3)) {
      setBrightness(cursor.getInt(3));
    }

    setEnabled(cursor.getInt(4) != 0);

    setRepeatDays(new DaysOfWeek((byte) cursor.getInt(5)));

    setHour(cursor.getInt(6));

    setMinute(cursor.getInt(7));

    setNextTime(cursor.getLong(8));
  }

  public ContentValues getValues() {
    ContentValues cv = new ContentValues();
    cv.put(AlarmColumns.COL_GROUP_NAME, getGroupName());
    cv.put(AlarmColumns.COL_MOOD_NAME, getMoodName());
    cv.put(AlarmColumns.COL_BRIGHTNESS, getBrightness());
    cv.put(AlarmColumns.COL_IS_ENABLED, isEnabled() ? 1 : 0);
    cv.put(AlarmColumns.COL_REPEAT_DAYS, getRepeatDays().getValue());
    cv.put(AlarmColumns.COL_HOUR, getHour());
    cv.put(AlarmColumns.COL_MINUTE, getMinute());
    cv.put(AlarmColumns.COL_NEXT_TIME, getNextTime());

    return cv;
  }

  public long getId() {
    return mId;
  }


  public String getGroupName() {
    return mGroupName;
  }

  public void setGroupName(String name) {
    mGroupName = name;
  }

  public String getMoodName() {
    return mMoodName;
  }

  public void setMoodName(String name) {
    mMoodName = name;
  }

  public Integer getBrightness() {
    return mBrightness;
  }

  public void setBrightness(Integer brightness) {
    mBrightness = brightness;
  }

  public boolean isEnabled() {
    return mIsEnabled;
  }

  public void setEnabled(boolean enabled) {
    mIsEnabled = enabled;
  }

  public DaysOfWeek getRepeatDays() {
    return mRepeatDays;
  }

  public void setRepeatDays(DaysOfWeek days) {
    if (days == null) {
      throw new IllegalArgumentException();
    }
    mRepeatDays = days;
  }

  public int getHour() {
    return mHour;
  }

  public void setHour(int hour) {
    mHour = hour;
  }

  public int getMinute() {
    return mMinute;
  }

  public void setMinute(int minute) {
    mMinute = minute;
  }

  public long getNextTime() {
    return mNextTime;
  }

  public void setNextTime(long nextTime) {
    mNextTime = nextTime;
  }
}
