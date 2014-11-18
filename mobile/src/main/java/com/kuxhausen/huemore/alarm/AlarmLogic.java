package com.kuxhausen.huemore.alarm;

import java.util.Calendar;

public class AlarmLogic {


  public static Long computeNextAlarmTime(AlarmData alarmData, long currentUnixTime) {
    if (alarmData.isEnabled()) {
      if (alarmData.getRepeatDays().isNoDaysSet()) {

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentUnixTime);
        now.add(Calendar.MINUTE, 1); // jump ahead 1 minute to disallow alarm going off immediately

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(currentUnixTime);
        alarmTime.set(Calendar.HOUR_OF_DAY, alarmData.getHour());
        alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set
        alarmTime.set(Calendar.MINUTE, alarmData.getMinute());
        alarmTime.getTimeInMillis(); // force internals fields to be recomputed after set

        if (!alarmTime.after(now)) {
          alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        return alarmTime.getTimeInMillis();
      }
    }

    return null;
  }
}
