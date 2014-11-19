package com.kuxhausen.huemore.alarm;

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
}
