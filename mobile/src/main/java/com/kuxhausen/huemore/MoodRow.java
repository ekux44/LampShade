package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.content.Context;

import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.state.Mood;

public class MoodRow {

  private Mood mValue;
  private long id;
  private String mName, mLowercaseName;
  private int mPriority;

  public MoodRow(String name, long dbid, Mood mood, String lowercaseName, int priority) {
    id = dbid;
    mLowercaseName = lowercaseName;
    mName = name;
    mValue = mood;
    mPriority = priority;
  }

  public boolean isStared() {
    if (mPriority == MoodColumns.STARRED_PRIORITY) {
      return true;
    }
    return false;
  }

  public void starChanged(Context c, boolean isStared) {
    if (isStared) {
      mPriority = MoodColumns.STARRED_PRIORITY;
    } else {
      mPriority = MoodColumns.UNSTARRED_PRIORITY;
    }

    String rowSelect = MoodColumns._ID + "=?";
    String[] rowArg = {"" + id};

    ContentValues mNewValues = new ContentValues();
    mNewValues.put(MoodColumns.COL_MOOD_PRIORITY, mPriority);
    c.getContentResolver()
        .update(MoodColumns.MOODS_URI, mNewValues, rowSelect, rowArg);
  }

  public String getName() {
    return mName;
  }

  public Mood getMood() {
    return mValue;
  }
}
