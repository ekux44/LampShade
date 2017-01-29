package com.kuxhausen.huemore.net.dev;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MessageAccumulatorTest {

  @Test
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
