package com.kuxhausen.huemore.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.persistence.Definitions;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmLogic {


  public static Long computeNextAlarmTime(AlarmData alarmData, long currentUnixTime) {
    if (alarmData.isEnabled()) {

      Calendar now = Calendar.getInstance();
      now.setTimeInMillis(currentUnixTime);
      now.add(Calendar.MINUTE, 1); // jump ahead 1 minute to disallow alarm going off immediately

      if (alarmData.getRepeatDays().isNoDaysSet()) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(currentUnixTime);
        alarmTime.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set
        alarmTime.set(Calendar.MINUTE, alarmData.getMinute());
        alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set

        while (!alarmTime.after(now)) {
          alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        return alarmTime.getTimeInMillis();

      } else {
        ArrayList<Long> repeatingDays = new ArrayList<Long>();

        for (int dayNo = 1; dayNo <= 7; dayNo++) {
          if (alarmData.getRepeatDays().isDaySet(dayNo)) {

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(currentUnixTime);

            alarmTime.set(Calendar.DAY_OF_WEEK, dayNo);
            alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set

            alarmTime.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
            alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set
            alarmTime.set(Calendar.MINUTE, alarmData.getMinute());
            alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set

            //move adjust this alarmTime such that it is the first occurrence of this day of week after now
            {
              while (!alarmTime.before(now)) {
                alarmTime.add(Calendar.WEEK_OF_YEAR, -1);
              }
              while (!alarmTime.after(now)) {
                alarmTime.add(Calendar.WEEK_OF_YEAR, 1);
              }
            }

            repeatingDays.add(alarmTime.getTimeInMillis());
          }
        }
        //now return the earliest valid day
        if (repeatingDays.size() > 0) {
          long result = Long.MAX_VALUE;
          for (long dayTime : repeatingDays) {
            if (dayTime < result) {
              result = dayTime;
            }
          }
          return result;
        }

      }
    }
    return null;
  }

  /**
   * if enabled and alarm time has passed, replaced with next upcoming alarm time. If not repeating,
   * mark disabled. Saves changes to database
   */
  public static void updateAlarm(Context context, AlarmData data) {
    if (data.isEnabled()) {

      Calendar mustBeAfter = Calendar.getInstance();
      mustBeAfter.add(Calendar.MINUTE, 1);

      Calendar cal = data.getAlarmTime();
      if (cal.before(mustBeAfter)) {
        if (data.getRepeatDays().isNoDaysSet()) {
          data.setEnabled(false);
        } else {
          data.setAlarmTime(
              computeNextAlarmTime(data.getHour(), data.getMinute(), data.getRepeatDays()));
        }
        saveChangesToDB(context, data);
      }
    }
  }

  public static Calendar computeNextAlarmTime(int hour, int minute, DaysOfWeek repeats) {
    Calendar mustBeAfter = Calendar.getInstance();
    mustBeAfter.add(Calendar.MINUTE, 1);

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.HOUR, hour);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.getTime(); //force internal recompute

    while (calendar.before(mustBeAfter)) {
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
    Cursor
        cursor =
        context.getContentResolver()
            .query(Definitions.AlarmColumns.ALARMS_URI, AlarmData.QUERY_COLUMNS, null, null, null);

    if (cursor.moveToFirst()) {
      return new AlarmData(cursor);
    } else {
      return null;
    }
  }

  public static void insertAlarmToDB(Context context, AlarmData data) {
    long
        baseId =
        Long.parseLong(
            context.getContentResolver()
                .insert(Definitions.AlarmColumns.ALARMS_URI, data.getValues())
                .getLastPathSegment());
    data.setId(baseId);
  }

  public static void saveChangesToDB(Context context, AlarmData data) {
    String rowSelect = Definitions.AlarmColumns._ID + "=?";
    String[] rowArg = {"" + data.getId()};
    context.getContentResolver()
        .update(Definitions.AlarmColumns.ALARMS_URI, data.getValues(), rowSelect, rowArg);
  }

  public static void deleteAlarmFromDB(Context context, AlarmData data) {
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
    if (!data.isEnabled()) {
      data.setEnabled(true);
      data.setAlarmTime(
          computeNextAlarmTime(data.getHour(), data.getMinute(), data.getRepeatDays()));
      saveChangesToDB(context, data);
      AlarmReceiver.registerAlarm(context, data);
    } else {
      AlarmReceiver.unregisterAlarm(context, data);
      data.setEnabled(false);
      saveChangesToDB(context, data);
    }
  }
}
