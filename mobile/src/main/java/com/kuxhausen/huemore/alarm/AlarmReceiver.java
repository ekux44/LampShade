package com.kuxhausen.huemore.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if (intent.getAction().equals(InternalArguments.ALARM_INTENT_ACTION)) {
      int globalId = getGlobalId(context);
      int intentId = intent.getIntExtra(PreferenceKeys.ALARM_GLOBAL_ID, -1);

      if (globalId == intentId) {
        int alarmId = intent.getIntExtra(InternalArguments.ALARM_ID, -1);

        AlarmData alarm = getAlarm(context, alarmId);
        if (alarm != null) {

          updateAlarm(context, alarm);
          //schedule next alarm if repeating
          if (alarm.isEnabled()) {
            registerAlarm(context, alarm);
          }

          //now start playing this alarm
          Intent transmitter = new Intent(context, ConnectivityService.class);
          transmitter.putExtra(InternalArguments.MAX_BRIGHTNESS, alarm.getBrightness());
          transmitter.putExtra(InternalArguments.MOOD_NAME, alarm.getMoodName());
          transmitter.putExtra(InternalArguments.GROUP_NAME, alarm.getGroupName());
          startWakefulService(context, transmitter);
        }
      }
    }
  }

  public static AlarmData getAlarm(Context context, int id) {
    Cursor
        cursor =
        context.getContentResolver()
            .query(AlarmColumns.ALARMS_URI, AlarmData.QUERY_COLUMNS, null, null, null);

    if (cursor.moveToFirst()) {
      return new AlarmData(cursor);
    } else {
      return null;
    }
  }

  private static PendingIntent generatePendingIntent(Context context, AlarmData data) {
    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.setAction(InternalArguments.ALARM_INTENT_ACTION);
    intent.setData(Uri.parse("content://" + data.getId()));
    intent.putExtra(InternalArguments.ALARM_ID, data.getId());
    intent.putExtra(PreferenceKeys.ALARM_GLOBAL_ID, getGlobalId(context));

    return PendingIntent.getBroadcast(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  public static void registerAlarm(Context context, AlarmData data) {
    PendingIntent pending = generatePendingIntent(context, data);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.set(AlarmManager.RTC_WAKEUP, data.getAlarmTime().getTimeInMillis(), pending);

  }

  public static void upregisterAlarm(Context context, AlarmData data) {
    PendingIntent pending = generatePendingIntent(context, data);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.cancel(pending);
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

        String rowSelect = AlarmColumns._ID + "=?";
        String[] rowArg = {"" + data.getId()};
        context.getContentResolver()
            .update(AlarmColumns.ALARMS_URI, data.getValues(), rowSelect, rowArg);

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

  public static void updateGloablIntentId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int newGlobalId = prefs.getInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, -1) + 1;
    prefs.edit().putInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, newGlobalId).commit();
  }

  public static int getGlobalId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getInt(PreferenceKeys.ALARM_GLOBAL_ID, -1);
  }
}
