package com.kuxhausen.huemore.net.dev;

import android.test.AndroidTestCase;

public class MessageAccumulatorTest extends AndroidTestCase {

  public void testAdd() {
    MessageAccumulator accumulator = new MessageAccumulator();
    accumulator.add(1);
    accumulator.add(2);
    accumulator.add(3);
    accumulator.add(5);
    accumulator.add(6);
    accumulator.add(6);
    accumulator.add(4);
    assertEquals(accumulator.toString(), "1-3,5-6,6,4");
  }
}
