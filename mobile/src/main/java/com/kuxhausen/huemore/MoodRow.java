package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.content.Context;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Mood;

public class MoodRow {
  Mood mValue;
  long id;
  String mName, mLowercaseName;
  int mPriority;

  public MoodRow(String name, long dbid, Mood mood, String lowercaseName, int priority) {
    id = dbid;
    mLowercaseName = lowercaseName;
    mName = name;
    mValue = mood;
    mPriority = priority;
  }

  public boolean isStared(){
    if(mPriority>1)
      return true;
    return false;
  }

  public void starChanged(Context c, boolean isStared){
    if(isStared)
      mPriority = 2;
    else
      mPriority = 1;

    String rowSelect = DatabaseDefinitions.MoodColumns._ID + "=?";
    String[] rowArg = {"" + id};

    ContentValues mNewValues = new ContentValues();
    mNewValues.put(DatabaseDefinitions.MoodColumns.COL_MOOD_PRIORITY, mPriority);
    c.getContentResolver().update(DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues, rowSelect, rowArg);
  }
}
