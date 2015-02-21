package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.test.AndroidTestCase;

import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;

import java.util.Calendar;

public class AlarmDataTest extends AndroidTestCase {

  private Calendar twoDaysPrior, yesterday, now, oneMinForward, oneHourForward, oneDayForward,
      oneMonthForward, oneYearForward;

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

    oneMonthForward = Calendar.getInstance();
    oneMonthForward.clear();
    oneMonthForward.set(2014, Calendar.AUGUST, 26, 13, 44);

    oneYearForward = Calendar.getInstance();
    oneYearForward.clear();
    oneYearForward.set(2015, Calendar.JULY, 26, 13, 44);

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }


  public void testSetAlarm() {
    AlarmData alarm = new AlarmData();

    alarm.setAlarmTime(now);
    assertEquals(alarm.getAlarmTime(), now);

    alarm.setAlarmTime(yesterday);
    assertEquals(alarm.getAlarmTime(), yesterday);

    alarm.setAlarmTime(oneMinForward);
    assertEquals(alarm.getAlarmTime(), oneMinForward);

    alarm.setAlarmTime(oneHourForward);
    assertEquals(alarm.getAlarmTime(), oneHourForward);

    alarm.setAlarmTime(oneDayForward);
    assertEquals(alarm.getAlarmTime(), oneDayForward);

    alarm.setAlarmTime(oneMonthForward);
    assertEquals(alarm.getAlarmTime(), oneMonthForward);

    alarm.setAlarmTime(oneYearForward);
    assertEquals(alarm.getAlarmTime(), oneYearForward);
  }

  public void testGetValues() {
    AlarmData alarm = new AlarmData();

    alarm.setAlarmTime(now);
    ContentValues nowValues = alarm.getValues();

    assertEquals(nowValues.get(AlarmColumns.COL_YEAR), now.get(Calendar.YEAR));
    assertEquals(nowValues.get(AlarmColumns.COL_MONTH), now.get(Calendar.MONTH));
    assertEquals(nowValues.get(AlarmColumns.COL_DAY), now.get(Calendar.DAY_OF_MONTH));
    assertEquals(nowValues.get(AlarmColumns.COL_HOUR), now.get(Calendar.HOUR_OF_DAY));
    assertEquals(nowValues.get(AlarmColumns.COL_MINUTE), now.get(Calendar.MINUTE));
  }

}
