package com.kuxhausen.huemore.alarm;

import android.content.ContentValues;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;

public class AlarmData {

  // must be kept in sync with AlarmData constructor
  public final static String[] QUERY_COLUMNS = {
      AlarmColumns._ID,
      AlarmColumns.COL_GROUP_NAME,
      AlarmColumns.COL_MOOD_ID,
      Definitions.MoodColumns.COL_MOOD_NAME,
      AlarmColumns.COL_BRIGHTNESS,
      AlarmColumns.COL_IS_ENABLED,
      AlarmColumns.COL_REPEAT_DAYS,
      AlarmColumns.COL_HOUR,
      AlarmColumns.COL_MINUTE,
      AlarmColumns.COL_NEXT_TIME
  };

  private long mId;
  private String mGroupName;
  private long mMoodId;
  private String mMoodName; // this is only to be read by the UI, and never saved to database
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

    setGroupName(cursor.getString(1));

    setMood(cursor.getLong(2), cursor.getString(3));

    if (!cursor.isNull(4)) {
      setBrightness(cursor.getInt(4));
    }

    setEnabled(cursor.getInt(5) != 0);

    setRepeatDays(new DaysOfWeek((byte) cursor.getInt(6)));

    setHour(cursor.getInt(7));

    setMinute(cursor.getInt(8));

    if (!cursor.isNull(9)) {
      setNextTime(cursor.getLong(9));
    }
  }

  public ContentValues getValues() {
    ContentValues cv = new ContentValues();
    cv.put(AlarmColumns.COL_GROUP_NAME, getGroupName());
    cv.put(AlarmColumns.COL_MOOD_ID, mMoodId);
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

  public long getmMoodId() {
    return mMoodId;
  }

  public String getMoodName() {
    return mMoodName;
  }

  public void setMood(long id, String name) {
    mMoodId = id;
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

  public Long getNextTime() {
    return mNextTime;
  }

  public void setNextTime(long nextTime) {
    mNextTime = nextTime;
  }
}
