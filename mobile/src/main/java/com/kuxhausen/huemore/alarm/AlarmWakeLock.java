package com.kuxhausen.huemore.alarm;

import android.content.Context;
import android.os.PowerManager;

import com.kuxhausen.huemore.persistence.Definitions;

/**
 * Helper class to hold alarm wakelocks
 */
public class AlarmWakeLock {

  private static PowerManager.WakeLock sCpuWakeLock;

  public static PowerManager.WakeLock createPartialWakeLock(Context context) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                          Definitions.InternalArguments.ALARM_HANDLER_THREAD);
  }

  public static void acquireCpuWakeLock(Context context) {
    if (sCpuWakeLock != null) {
      return;
    }
    sCpuWakeLock = createPartialWakeLock(context);
    sCpuWakeLock.acquire();
  }

  public static void acquireScreenCpuWakeLock(Context context) {
    if (sCpuWakeLock != null) {
      return;
    }
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    sCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                  | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                  | PowerManager.ON_AFTER_RELEASE,
                                  Definitions.InternalArguments.ALARM_HANDLER_THREAD);
    sCpuWakeLock.acquire();
  }

  public static void releaseCpuLock() {
    if (sCpuWakeLock != null) {
      sCpuWakeLock.release();
      sCpuWakeLock = null;
    }
  }
}
