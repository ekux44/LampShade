package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.alarm.AlarmLogic;

import java.util.Calendar;

public class AlarmLogicTest extends AndroidTestCase {

  private Calendar yesterday, now, oneHourForward, oneDayForward, twoDaysForward;

  protected void setUp() throws Exception {
    super.setUp();

    yesterday = Calendar.getInstance();
    yesterday.clear();
    yesterday.set(2014, Calendar.JULY, 25, 13, 44);

    now = Calendar.getInstance();
    now.clear();
    now.set(2014, Calendar.JULY, 26, 13, 44);

    oneHourForward = Calendar.getInstance();
    oneHourForward.clear();
    oneHourForward.set(2014, Calendar.JULY, 26, 14, 44);

    oneDayForward = Calendar.getInstance();
    oneDayForward.clear();
    oneDayForward.set(2014, Calendar.JULY, 27, 13, 44);

    twoDaysForward = Calendar.getInstance();
    twoDaysForward.clear();
    twoDaysForward.set(2014, Calendar.JULY, 28, 13, 44);

  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testComputeNextAlarmTime1() {

    AlarmData ad1 = new AlarmData(-1);
    ad1.setEnabled(true);
    ad1.setNextTime(oneDayForward.getTimeInMillis());
    ad1.setHour(13);
    ad1.setMinute(44);

    assertEquals((Long) oneDayForward.getTimeInMillis(),
                 AlarmLogic.computeNextAlarmTime(ad1, now.getTimeInMillis()));

  }
}
