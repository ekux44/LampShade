package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.alarm.AlarmLogic;
import com.kuxhausen.huemore.alarm.DaysOfWeek;

import java.util.Calendar;

public class AlarmLogicTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComputeNextAlarmTime(){

    Calendar yesterday = Calendar.getInstance();
    yesterday.set(2014, Calendar.JULY, 26, 13, 44);

    Calendar now = Calendar.getInstance();
    now.set(2014, Calendar.JULY, 25, 13, 44);

    Calendar oneHourForward = Calendar.getInstance();
    oneHourForward.set(2014, Calendar.JULY, 27, 14, 44);

    Calendar oneDayForward = Calendar.getInstance();
    oneDayForward.set(2014, Calendar.JULY, 27, 13, 44);

    Calendar twoDaysForward = Calendar.getInstance();
    twoDaysForward.set(2014, Calendar.JULY, 27, 13, 44);

    DaysOfWeek noRepeats = new DaysOfWeek();
    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setNextTime(oneDayForward.getTimeInMillis());
    ad1.setHour(14);
    ad1.setMinute(44);

    assertEquals((Long)oneDayForward.getTimeInMillis(), AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));


  }
}
