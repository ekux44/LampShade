package com.kuxhausen.huemore.timing;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class AlarmReciever extends WakefulBroadcastReceiver {

  Gson gson = new Gson();


  public static void updateAlarmTimes(Context context, DatabaseAlarm dbAlarm) {
    AlarmState as = dbAlarm.getAlarmState();

    if (!as.isRepeating()) {
      Calendar timeAdjustedCal = Calendar.getInstance();
      timeAdjustedCal.setTimeInMillis(as.getTime());
      timeAdjustedCal.setLenient(true);

      while (timeAdjustedCal.before(Calendar.getInstance())) {
        // make sure this hour & minute is in the future
        timeAdjustedCal.add(Calendar.DATE, 1);
      }
      as.setTime(timeAdjustedCal.getTimeInMillis());

    } else {
      Calendar rightNow = Calendar.getInstance();
      long[] scheduledTimes = new long[7];
      for (int i = 0; i < 7; i++) {
        if (as.getRepeatingDays()[i]) {
          Calendar existingTimeCal = Calendar.getInstance();
          existingTimeCal.setTimeInMillis(as.getRepeatingTimes()[i]);

          Calendar copyForDayOfWeek = Calendar.getInstance();
          copyForDayOfWeek.setLenient(true);
          copyForDayOfWeek.set(Calendar.HOUR_OF_DAY, existingTimeCal.get(Calendar.HOUR_OF_DAY));
          copyForDayOfWeek.set(Calendar.MINUTE, existingTimeCal.get(Calendar.MINUTE));
          copyForDayOfWeek.set(Calendar.SECOND, existingTimeCal.get(Calendar.SECOND));

          /**
           * 7+ desired day of week (+1 to convert to java calendar number) - current day of week %7
           **/
          copyForDayOfWeek.add(Calendar.DATE,
              (7 + (1 + i) - rightNow.get(Calendar.DAY_OF_WEEK)) % 7);

          while (copyForDayOfWeek.before(Calendar.getInstance())) {
            // if in past, choose that day next week
            copyForDayOfWeek.add(Calendar.DATE, 7);
          }
          scheduledTimes[i] = copyForDayOfWeek.getTimeInMillis();
        }
      }
      as.setRepeatingTimes(scheduledTimes);
    }
    dbAlarm.saveToDB();
  }

  public static void createAlarms(Context context, DatabaseAlarm dbAlarm) {
    updateAlarmTimes(context, dbAlarm);

    AlarmState as = dbAlarm.getAlarmState();
    Calendar soonestTime = null;

    if (!as.isRepeating()) {
      Calendar timeAdjustedCal = Calendar.getInstance();
      timeAdjustedCal.setTimeInMillis(as.getTime());

      AlarmReciever.scheduleAlarm(context, as, timeAdjustedCal.getTimeInMillis());
      soonestTime = timeAdjustedCal;
    } else {
      for (int i = 0; i < 7; i++) {
        long t = as.getRepeatingTimes()[i];
        if (as.getRepeatingDays()[i]) {
          AlarmReciever.scheduleWeeklyAlarm(context, as, t, i + 1);

          Calendar setTime = Calendar.getInstance();
          setTime.setTimeInMillis(t);
          if (soonestTime == null || setTime.before(soonestTime))
            soonestTime = setTime;
        }
      }
    }

    Toast.makeText(
        context,
        context.getString(R.string.next_scheduled_intro) + " "
            + DateUtils.getRelativeTimeSpanString(soonestTime.getTimeInMillis()),
        Toast.LENGTH_SHORT).show();

    dbAlarm.saveToDB();
  }

  private static void scheduleAlarm(Context context, AlarmState alarmState, Long timeInMillis) {

    PendingIntent pIntent = calculatePendingIntent(context, alarmState, 0);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMillis, pIntent);
  }

  private static void scheduleWeeklyAlarm(Context context, AlarmState alarmState,
      Long timeInMillis, int dayOfWeek) {

    PendingIntent pIntent = calculatePendingIntent(context, alarmState, dayOfWeek);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY * 7,
        pIntent);
  }

  public static void scheduleInternalAlarm(Context context, Long wakeupTimeInElapsedRealtimeMilis) {

    Intent intent = new Intent(context, AlarmReciever.class);
    intent.setAction("com.kuxhausen.huemore." + 8);

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupTimeInElapsedRealtimeMilis,
        pendingIntent);
  }

  public static void cancelAlarm(Context context, AlarmState alarmState) {
    for (int i = 0; i < 8; i++) {
      PendingIntent pIntent = calculatePendingIntent(context, alarmState, i);
      AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmMgr.cancel(pIntent);
    }
    {
      // reverse scheduledForFuture boolean to hit both possibilities
      alarmState.setScheduledForFuture(alarmState.isScheduled());
      for (int i = 0; i < 8; i++) {
        PendingIntent pIntent = calculatePendingIntent(context, alarmState, i);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pIntent);
      }
      alarmState.setScheduledForFuture(alarmState.isScheduled());
    }
  }

  /**
   * day of week Sunday = 1, Saturday = 7, 0=not repeating so we don't care, 8=transient/not user
   * visible
   **/
  private static PendingIntent calculatePendingIntent(Context context, AlarmState alarmState,
      int dayOfWeek) {
    Gson gson = new Gson();
    String aState = gson.toJson(alarmState);

    Intent intent = new Intent(context, AlarmReciever.class);
    intent.setAction("com.kuxhausen.huemore." + dayOfWeek + "." + aState);

    intent.putExtra(InternalArguments.ALARM_DETAILS, aState);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    return pendingIntent;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction() != null
        && intent.getAction().matches("com\\.kuxhausen\\.huemore\\.8\\..*")) {
      Intent trasmitter = new Intent(context, ConnectivityService.class);
      trasmitter.putExtra(InternalArguments.FLAG_AWAKEN_PLAYING_MOODS, true);
      startWakefulService(context, trasmitter);
    } else if (intent.getAction() != null
        && intent.getAction().matches("com\\.kuxhausen\\.huemore\\.\\d\\..*")) {
      AlarmState as =
          gson.fromJson(intent.getExtras().getString(InternalArguments.ALARM_DETAILS),
              AlarmState.class);

      Intent trasmitter = new Intent(context, ConnectivityService.class);
      trasmitter.putExtra(InternalArguments.MAX_BRIGHTNESS, as.brightness);
      trasmitter.putExtra(InternalArguments.MOOD_NAME, as.mood);
      trasmitter.putExtra(InternalArguments.GROUP_NAME, as.group);
      startWakefulService(context, trasmitter);

    }
  }
}
