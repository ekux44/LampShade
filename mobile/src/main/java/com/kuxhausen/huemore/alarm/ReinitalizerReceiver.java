package com.kuxhausen.huemore.alarm;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.PowerManager;

import com.kuxhausen.huemore.persistence.Definitions;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ReinitalizerReceiver extends BroadcastReceiver {

  /**
   * Schedules alarm on ACTION_BOOT_COMPLETED.  Reschedules alarm on TIME_SET, TIMEZONE_CHANGED,
   * MY_PACKAGE_REPLACED
   */
  @Override
  public void onReceive(final Context context, Intent intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

      final String action = intent.getAction();

      final PendingResult result = goAsync();
      final PowerManager.WakeLock wl = AlarmWakeLock.createPartialWakeLock(context);
      wl.acquire();

      // We must increment the global id out of the async task to prevent race conditions
      AlarmLogic.updateGloablIntentId(context);

      AlarmAsyncHandler.post(new Runnable() {
        @Override
        public void run() {

          // Update all the alarm instances on time change event
          updateAndScheduleAllAlarms(context);
          result.finish();

          wl.release();
        }
      });
    }
  }

  private static void updateAndScheduleAllAlarms(Context context) {
    Cursor
        cursor =
        context.getContentResolver()
            .query(Definitions.AlarmColumns.ALARMS_URI, AlarmData.QUERY_COLUMNS, null, null, null);

    cursor.moveToPosition(-1);
    while (cursor.moveToNext()) {
      AlarmData row = new AlarmData(cursor);

      AlarmLogic.updateAlarm(context, row);

      if (row.isEnabled()) {
        AlarmReceiver.registerAlarm(context, row);
      }
    }
  }
}
