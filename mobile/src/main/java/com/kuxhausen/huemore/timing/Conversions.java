package com.kuxhausen.huemore.timing;

import android.os.SystemClock;

import java.util.Calendar;


public class Conversions {

  public static int moodDailyTimeFromCalendarMillis(Calendar input) {
    Calendar startOfDay = Calendar.getInstance();
    startOfDay.set(Calendar.MILLISECOND, 0);
    startOfDay.set(Calendar.SECOND, 0);
    startOfDay.set(Calendar.MINUTE, 0);
    startOfDay.set(Calendar.HOUR_OF_DAY, 0);

    Calendar inputCopy = Calendar.getInstance();
    inputCopy.setTimeInMillis(input.getTimeInMillis());

    Long offsetWithinTheDayInMilis = inputCopy.getTimeInMillis() - startOfDay.getTimeInMillis();
    return (int) (offsetWithinTheDayInMilis / 100);
  }

  public static Calendar calendarMillisFromMoodDailyTime(int dailyMoodDeciSeconds) {
    Calendar startOfDay = Calendar.getInstance();
    startOfDay.set(Calendar.MILLISECOND, 0);
    startOfDay.set(Calendar.SECOND, 0);
    startOfDay.set(Calendar.MINUTE, 0);
    startOfDay.set(Calendar.HOUR_OF_DAY, 0);
    startOfDay.getTime();
    startOfDay.setTimeInMillis(startOfDay.getTimeInMillis() + (dailyMoodDeciSeconds * 100L));

    return startOfDay;
  }

  public static long miliEventTimeFromMoodDailyTime(int dailyMoodDeciSeconds) {

    Calendar event = calendarMillisFromMoodDailyTime(dailyMoodDeciSeconds);

    Calendar current = Calendar.getInstance();

    Long miliOffsetFromNow = event.getTimeInMillis() - current.getTimeInMillis();

    return SystemClock.elapsedRealtime() + miliOffsetFromNow;
  }

}
