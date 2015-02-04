package com.kuxhausen.huemore.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.persistence.Definitions;

public class ReinitalizerReciever extends BroadcastReceiver {

  /**
   * Schedules alarm on ACTION_BOOT_COMPLETED.  Reschedules alarm on TIME_SET, TIMEZONE_CHANGED
   */
  @Override
  public void onReceive(final Context context, Intent intent) {
    final String action = intent.getAction();

    final PendingResult result = goAsync();
    final PowerManager.WakeLock wl = AlarmWakeLock.createPartialWakeLock(context);
    wl.acquire();

    // We must increment the global id out of the async task to prevent race conditions
    updateGloablIntentId(context);

    AlarmAsyncHandler.post(new Runnable() {
      @Override
      public void run() {

        // Update all the alarm instances on time change event
        AlarmReciever.fixAlarmInstances(context); //TODO
        result.finish();

        wl.release();
      }
    });
  }

  public static void updateGloablIntentId(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    int globalId = prefs.getInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, -1) + 1;
    prefs.edit().putInt(Definitions.PreferenceKeys.ALARM_GLOBAL_ID, globalId).commit();
  }
}
