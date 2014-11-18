package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.alarm.AlarmLogic;

import java.util.Calendar;

public class AlarmLogicTest extends AndroidTestCase {

  private Calendar yesterday, now, oneMinForward, oneHourForward, oneDayForward,
      oneDayOneMinForward, twoDaysForward;

  protected void setUp() throws Exception {
    super.setUp();

    yesterday = Calendar.getInstance();
    yesterday.clear();
    yesterday.set(2014, Calendar.JULY, 25, 13, 44);

    now = Calendar.getInstance();
    now.clear();
    now.set(2014, Calendar.JULY, 26, 13, 44);

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

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComputeNextAlarmTime0() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(false);
    ad1.setNextTime(oneDayForward.getTimeInMillis());
    ad1.setHour(oneDayForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneDayForward.get(Calendar.MINUTE));

    assertNull(AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));
  }

  public void testComputeNextAlarmTime1() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setNextTime(oneDayForward.getTimeInMillis());
    ad1.setHour(oneDayForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneDayForward.get(Calendar.MINUTE));

    assertEquals((Long) oneDayForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime2() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setNextTime(oneMinForward.getTimeInMillis());
    ad1.setHour(oneMinForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneMinForward.get(Calendar.MINUTE));

    assertEquals((Long) oneDayOneMinForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }

  public void testComputeNextAlarmTime3() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setNextTime(oneHourForward.getTimeInMillis());
    ad1.setHour(oneHourForward.get(Calendar.HOUR_OF_DAY));
    ad1.setMinute(oneHourForward.get(Calendar.MINUTE));

    assertEquals((Long) oneHourForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }
}
