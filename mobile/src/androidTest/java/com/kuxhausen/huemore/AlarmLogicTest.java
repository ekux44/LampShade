package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.alarm.AlarmLogic;
import com.kuxhausen.huemore.alarm.DaysOfWeek;

import java.util.Calendar;

public class AlarmLogicTest extends AndroidTestCase {

  private Calendar twoDaysPrior, yesterday, now, oneMinForward, oneHourForward, oneDayForward,
      oneDayOneMinForward, twoDaysForward, sixDaysForward, sevenDaysForward, eightDaysForward,
      thirteenDaysForward;

  protected void setUp() throws Exception {
    super.setUp();

    twoDaysPrior = Calendar.getInstance();
    twoDaysPrior.clear();
    twoDaysPrior.set(2014, Calendar.JULY, 24, 13, 44);

    yesterday = Calendar.getInstance();
    yesterday.clear();
    yesterday.set(2014, Calendar.JULY, 25, 13, 44);

    now = Calendar.getInstance();
    now.clear();
    now.set(2014, Calendar.JULY, 26, 13, 44); // this is a Saturday

    oneMinForward = Calendar.getInstance();
    oneMinForward.clear();
    oneMinForward.set(2014, Calendar.JULY, 26, 13, 45);

    oneHourForward = Calendar.getInstance();
    oneHourForward.clear();
    oneHourForward.set(2014, Calendar.JULY, 26, 14, 44);

    oneDayForward = Calendar.getInstance();
    oneDayForward.clear();
    oneDayForward.set(2014, Calendar.JULY, 27, 13, 44);

    oneDayOneMinForward = Calendar.getInstance();
    oneDayOneMinForward.clear();
    oneDayOneMinForward.set(2014, Calendar.JULY, 27, 13, 45);

    twoDaysForward = Calendar.getInstance();
    twoDaysForward.clear();
    twoDaysForward.set(2014, Calendar.JULY, 28, 13, 44);

    sixDaysForward = Calendar.getInstance();
    sixDaysForward.clear();
    sixDaysForward.set(2014, Calendar.AUGUST, 1, 13, 44);

    sevenDaysForward = Calendar.getInstance();
    sevenDaysForward.clear();
    sevenDaysForward.set(2014, Calendar.AUGUST, 2, 13, 44);

    eightDaysForward = Calendar.getInstance();
    eightDaysForward.clear();
    eightDaysForward.set(2014, Calendar.AUGUST, 3, 13, 44);

    thirteenDaysForward = Calendar.getInstance();
    thirteenDaysForward.clear();
    thirteenDaysForward.set(2014, Calendar.AUGUST, 8, 13, 44);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComputeNextAlarmTime0() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(false);
    ad1.setHour(oneDayForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneDayForward.get(Calendar.MINUTE));

    assertNull(AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));
  }

  public void testComputeNextAlarmTime1() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(oneDayForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneDayForward.get(Calendar.MINUTE));

    assertEquals((Long) oneDayForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime2() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(oneMinForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneMinForward.get(Calendar.MINUTE));

    assertEquals((Long) oneDayOneMinForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime3() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(oneHourForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneHourForward.get(Calendar.MINUTE));

    assertEquals((Long) oneHourForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime4() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(yesterday.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(yesterday.get(Calendar.MINUTE));

    assertEquals((Long) oneDayForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));
  }

  public void testComputeNextAlarmTime1Repeating() {

    DaysOfWeek fridays = new DaysOfWeek();
    fridays.setDay(Calendar.FRIDAY, true);

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(yesterday.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(yesterday.get(Calendar.MINUTE));
    ad1.setRepeatDays(fridays);

    assertEquals((Long) sixDaysForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

    assertEquals((Long) sixDaysForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, oneDayOneMinForward.getTimeInMillis()));

    assertEquals((Long) yesterday.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, twoDaysPrior.getTimeInMillis()));

    assertEquals((Long) thirteenDaysForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, sixDaysForward.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime2Repeating() {

    DaysOfWeek weekends = new DaysOfWeek();
    weekends.setDay(Calendar.SATURDAY, true);
    weekends.setDay(Calendar.SUNDAY, true);

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setHour(yesterday.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(yesterday.get(Calendar.MINUTE));
    ad1.setRepeatDays(weekends);

    assertEquals((Long) now.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, yesterday.getTimeInMillis()));

    assertEquals((Long) oneDayForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

    assertEquals((Long) sevenDaysForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, oneDayOneMinForward.getTimeInMillis()));

    assertEquals((Long) eightDaysForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, sevenDaysForward.getTimeInMillis()));
  }
}
