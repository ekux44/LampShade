package com.kuxhausen.huemore.timing;

import com.google.gson.Gson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DeprecatedAlarmState;

import java.util.Calendar;

public class AlarmReciever extends WakefulBroadcastReceiver {

  public static void scheduleInternalAlarm(Context context, Long wakeupTimeInElapsedRealtimeMilis) {

    Intent intent = new Intent(context, AlarmReciever.class);
    intent.setAction("com.kuxhausen.huemore." + 8);

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupTimeInElapsedRealtimeMilis,
                 pendingIntent);
  }


  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction() != null
        && intent.getAction().matches("com\\.kuxhausen\\.huemore\\.8*")) {
      Log.d("alarm", "napping mood wakeup");

      Intent trasmitter = new Intent(context, ConnectivityService.class);
      startWakefulService(context, trasmitter);
    }
  }
}
