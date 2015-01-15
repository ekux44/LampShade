package com.kuxhausen.huemore.timing;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateFormat;

import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.DeprecatedAlarmState;

import java.util.Calendar;

public class DatabaseAlarm {

  private DeprecatedAlarmState aState;
  private Context c;
  private int id;
  Gson gson = new Gson();


  public DatabaseAlarm(Context context, Uri uri) {
    c = context;
    String[] columns = {Definitions.DeprecatedAlarmColumns.STATE, BaseColumns._ID};
    Cursor cursor = context.getContentResolver().query(uri, columns, null, null, null);

    cursor.moveToPosition(0);
    aState = gson.fromJson(cursor.getString(0), DeprecatedAlarmState.class);
    id = cursor.getInt(1);
  }

  public DatabaseAlarm(Context context, DeprecatedAlarmState as, int db_ID) {
    c = context;
    aState = as;
    id = db_ID;
  }

  public DeprecatedAlarmState getAlarmState() {
    return aState;
  }

  public int getID() {
    return id;
  }

  public String getTime() {
    /** only hour and minute are valid **/
    long time = 0;
    if (aState.isRepeating()) {
      for (int i = 0; i < 7; i++) {
        if (aState.getRepeatingDays()[i]) {
          time = aState.getRepeatingTimes()[i];
        }
      }
    } else {
      time = aState.getTime();
    }

    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(time);

    return DateFormat.getTimeFormat(c).format(cal.getTime());
  }

  public String getSecondaryDescription() {
    String result = aState.group + " \u2192 " + aState.mood;

    if (aState.isRepeating()) {
      result += "   " + NewAlarmDialogFragment.repeatsToString(c, aState.getRepeatingDays());
    }
    return result;
  }

  public synchronized void toggle() {
    boolean wasScheduled = aState.isScheduled();

    if (wasScheduled) {
      AlarmReciever.cancelAlarm(c, aState);
    } else {
      AlarmReciever.createAlarms(c, this);
    }

    aState.setScheduledForFuture(!wasScheduled);

    saveToDB();
  }

  public void delete() {
    if (this.aState.isScheduled()) {
      this.toggle();
    }

    String moodSelect2 = BaseColumns._ID + "=?";
    String[] moodArg2 = {"" + this.getID()};
    c.getContentResolver().delete(Definitions.DeprecatedAlarmColumns.ALARMS_URI, moodSelect2, moodArg2);
  }

  public void saveToDB() {
    String rowSelect = BaseColumns._ID + "=?";
    String[] rowArg = {"" + id};

    ContentValues mNewValues = new ContentValues();
    mNewValues.put(Definitions.DeprecatedAlarmColumns.STATE, gson.toJson(aState));

    c.getContentResolver().update(Definitions.DeprecatedAlarmColumns.ALARMS_URI, mNewValues, rowSelect, rowArg);

  }
}
