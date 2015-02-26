package com.kuxhausen.huemore.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kuxhausen.huemore.BuildConfig;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;

import java.util.Calendar;

public class AlarmLogic {

  /**
   * if enabled and alarm time has passed, replaced with next upcoming alarm time. If not repeating,
   * mark disabled. Saves changes to database
   */
  public static void updateAlarm(Context context, AlarmData data) {
    logAlarm("UpdateAlarm", data);
    if (data.isEnabled()) {

      Calendar mustBeAfter = Calendar.getInstance();
      mustBeAfter.add(Calendar.MINUTE, 1);

      Calendar cal = data.getAlarmTime();
      if (!cal.after(mustBeAfter)) {
        if (data.getRepeatDays().isNoDaysSet()) {
          data.setEnabled(false);
        } else {
          data.setAlarmTime(
              computeNextAlarmTime(data.getHourOfDay(), data.getMinute(), data.getRepeatDays(),
                                   Calendar.getInstance()));
        }
        saveChangesToDB(context, data);
      }
    }
  }

  public static Calendar computeNextAlarmTime(int hourOfDay, int minute, DaysOfWeek repeats,
                                              Calendar currentTime) {
    Calendar mustBeAfter = Calendar.getInstance();
    mustBeAfter.setTimeInMillis(currentTime.getTimeInMillis());
    mustBeAfter.add(Calendar.MINUTE, 1);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(currentTime.getTimeInMillis());
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.getTime(); //force internal recompute

    while (!calendar.after(mustBeAfter)) {
      calendar.add(Calendar.DAY_OF_WEEK, 1);

      //if alarm is only valid on certain days, keep incrementing till one of those days reached
      if (!repeats.isNoDaysSet()) {
        while (!repeats.isDaySet(calendar.get(Calendar.DAY_OF_WEEK))) {
          calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
      }
    }

    return calendar;
  }

  public static AlarmData getAlarm(Context context, long id) {
    AlarmData result = null;

    String
        rowSelect =
        Definitions.AlarmColumns.TABLE_NAME + "." + Definitions.AlarmColumns._ID + "=?";
    String[] rowArg = {"" + id};
    Cursor cursor = context.getContentResolver().query(AlarmColumns.ALARMS_URI,
                                                       AlarmData.QUERY_COLUMNS, rowSelect, rowArg,
                                                       null);

    if (cursor.moveToFirst()) {
      result = new AlarmData(cursor);
    }
    cursor.close();
    return result;
  }

  public static void insertAlarmToDB(Context context, AlarmData data) {
    logAlarm("DBinsert", data);
    long
        baseId =
        Long.parseLong(
            context.getContentResolver()
                .insert(Definitions.AlarmColumns.ALARMS_URI, data.getValues())
                .getLastPathSegment());
    data.setId(baseId);
  }

  public static void saveChangesToDB(Context context, AlarmData data) {
    logAlarm("DBupdate", data);
    String rowSelect = Definitions.AlarmColumns._ID + "=?";
    String[] rowArg = {"" + data.getId()};
    context.getContentResolver()
        .update(Definitions.AlarmColumns.ALARMS_URI, data.getValues(), rowSelect, rowArg);
  }

  public static void deleteAlarmFromDB(Context context, AlarmData data) {
    logAlarm("DBdelete", data);
    String rowSelect = Definitions.AlarmColumns._ID + "=?";
    String[] rowArg = {"" + data.getId()};
    context.getContentResolver().delete(Definitions.AlarmColumns.ALARMS_URI, rowSelect, rowArg);
  }

  public static void updateGloablIntentId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int newGlobalId = prefs.getInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, -1) + 1;
    prefs.edit().putInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, newGlobalId).commit();
  }

  public static int getGlobalId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, -1);
  }

  public static void toggleAlarm(Context context, AlarmData data) {
    logAlarm("Toggled", data);
    if (!data.isEnabled()) {
      data.setEnabled(true);
      data.setAlarmTime(
          computeNextAlarmTime(data.getHourOfDay(), data.getMinute(), data.getRepeatDays(),
                               Calendar.getInstance()));
      saveChangesToDB(context, data);
      AlarmReceiver.registerAlarm(context, data);
    } else {
      AlarmReceiver.unregisterAlarm(context, data);
      data.setEnabled(false);
      saveChangesToDB(context, data);
    }
  }

  public static void logAlarm(String eventMessage, AlarmData alarmData) {
    if (BuildConfig.BUILD_TYPE.equals("debug")) {
      StringBuilder sb = new StringBuilder(eventMessage);
      sb.append(',');
      Calendar time = alarmData.getAlarmTime();
      sb.append(time.get(Calendar.YEAR));
      sb.append(',');
      sb.append(time.get(Calendar.MONTH));
      sb.append(',');
      sb.append(time.get(Calendar.DAY_OF_MONTH));
      sb.append(',');
      sb.append(time.get(Calendar.HOUR_OF_DAY));
      sb.append(',');
      sb.append(time.get(Calendar.MINUTE));
      sb.append(',');
      sb.append(alarmData.getMoodName());
      sb.append(',');
      sb.append(alarmData.getGroupName());
      Log.i("AlarmDebug", sb.toString());
    }
  }
}
