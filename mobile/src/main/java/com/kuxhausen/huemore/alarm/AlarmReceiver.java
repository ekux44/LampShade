package com.kuxhausen.huemore.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

public class AlarmReceiver extends WakefulBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if (intent.getAction().equals(InternalArguments.ALARM_INTENT_ACTION)) {
      int globalId = AlarmLogic.getGlobalId(context);
      int intentId = intent.getIntExtra(PreferenceKeys.ALARM_GLOBAL_ID, -1);

      if (globalId == intentId) {
        int alarmId = intent.getIntExtra(InternalArguments.ALARM_ID, -1);

        AlarmData alarm = AlarmLogic.getAlarm(context, alarmId);
        if (alarm != null) {

          AlarmLogic.updateAlarm(context, alarm);
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

  private static PendingIntent generatePendingIntent(Context context, AlarmData data) {
    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.setAction(InternalArguments.ALARM_INTENT_ACTION);
    intent.setData(Uri.parse("content://" + data.getId()));
    intent.putExtra(InternalArguments.ALARM_ID, data.getId());
    intent.putExtra(PreferenceKeys.ALARM_GLOBAL_ID, AlarmLogic.getGlobalId(context));

    return PendingIntent.getBroadcast(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  public static void registerAlarm(Context context, AlarmData data) {
    AlarmLogic.logAlarm("RegisterAlarm", data);

    PendingIntent pending = generatePendingIntent(context, data);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      alarmMgr.set(AlarmManager.RTC_WAKEUP, data.getAlarmTime().getTimeInMillis(), pending);
    } else {
      alarmMgr.setExact(AlarmManager.RTC_WAKEUP, data.getAlarmTime().getTimeInMillis(), pending);
    }
    Toast.makeText(context, context.getString(R.string.next_scheduled_intro) + " " + DateUtils
        .getRelativeTimeSpanString(data.getAlarmTime().getTimeInMillis()), Toast.LENGTH_SHORT)
        .show();

  }

  public static void unregisterAlarm(Context context, AlarmData data) {
    AlarmLogic.logAlarm("UnregisterAlarm", data);

    PendingIntent pending = generatePendingIntent(context, data);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.cancel(pending);
  }


}
