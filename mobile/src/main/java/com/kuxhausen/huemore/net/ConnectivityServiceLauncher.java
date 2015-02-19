package com.kuxhausen.huemore.net;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.kuxhausen.huemore.persistence.Definitions;

public class ConnectivityServiceLauncher extends WakefulBroadcastReceiver {

  public static void scheduleInternalAlarm(Context context,
                                           Long wakeupTimeInElapsedRealtimeMillis) {

    Intent intent = new Intent(context, ConnectivityServiceLauncher.class);
    intent.setAction(Definitions.InternalArguments.CONNECTIVITY_SERVICE_INTENT_ACTION);

    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeupTimeInElapsedRealtimeMillis,
                 pendingIntent);
  }


  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction() != null
        && intent.getAction()
        .equals(Definitions.InternalArguments.CONNECTIVITY_SERVICE_INTENT_ACTION)) {
      Log.d("alarm", "napping mood wakeup");

      Intent transmitter = new Intent(context, ConnectivityService.class);
      startWakefulService(context, transmitter);
    }
  }
}
