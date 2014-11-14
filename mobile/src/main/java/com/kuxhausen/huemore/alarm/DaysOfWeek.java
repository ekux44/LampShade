package com.kuxhausen.huemore.alarm;

import com.kuxhausen.huemore.persistence.ManagedBitSet;

import java.util.BitSet;
import java.util.Calendar;

public final class DaysOfWeek {

  private BitSet mDaysAsBits;

  public DaysOfWeek() {
    mDaysAsBits = new BitSet();
  }

  public DaysOfWeek(byte asByte) {
    mDaysAsBits = ManagedBitSet.toBitSet(new byte[]{asByte});
  }

  /**
   * @param day based on values of Calendar.DAY_OF_WEEK
   */
  public boolean isDaySet(int day) {
    return mDaysAsBits.get(day);
  }

  /**
   * @param day based on values of Calendar.DAY_OF_WEEK
   */
  public void setDay(int day, boolean dayEnabled) {
    mDaysAsBits.set(day, dayEnabled);
  }

  public boolean isNoDaysSet() {
    return mDaysAsBits.isEmpty();
  }

  public boolean isAllDaysSet() {
    int[]
        daysOfWeek =
        {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
         Calendar.FRIDAY, Calendar.SATURDAY};

    for (int day : daysOfWeek) {
      if (!isDaySet(day)) {
        return false;
      }
    }
    return true;
  }

  public byte getValue() {
    return ManagedBitSet.fromBitSet(mDaysAsBits, 8)[0];
  }
}
