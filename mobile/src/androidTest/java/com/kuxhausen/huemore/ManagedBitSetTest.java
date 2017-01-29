package com.kuxhausen.huemore;

import android.support.test.runner.AndroidJUnit4;

import com.kuxhausen.huemore.persistence.ManagedBitSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ManagedBitSetTest {

  @Test
  public void testIncrementing() {
    boolean[] set = new boolean[1000];
    for (int i = 0; i < set.length; i++) {
      if (Math.random() < .5) {
        set[i] = true;
      }
    }

    ManagedBitSet input = new ManagedBitSet();
    for (int i = 0; i < set.length; i++) {
      input.incrementingSet(set[i]);
    }

    String intermediate = input.getBase64Encoding();

    ManagedBitSet output = new ManagedBitSet(intermediate);
    for (int i = 0; i < set.length; i++) {
      assertEquals(set[i], output.incrementingGet());
    }
  }

  @Test
  public void testNumber() {

    ManagedBitSet input = new ManagedBitSet();
    input.addNumber(0, 16);
    input.addNumber(1, 1);
    input.addNumber(2, 3);
    input.addNumber(3, 9);
    input.addNumber(1023, 10);

    String intermediate = input.getBase64Encoding();

    ManagedBitSet output = new ManagedBitSet(intermediate);
    assertEquals(0, output.extractNumber(16));
    assertEquals(1, output.extractNumber(1));
    assertEquals(2, output.extractNumber(3));
    assertEquals(3, output.extractNumber(9));
    assertEquals(1023, output.extractNumber(10));

    ManagedBitSet littleEndianOuput = new ManagedBitSet(intermediate);
    littleEndianOuput.useLittleEndianEncoding(true);
    assertEquals(0, littleEndianOuput.extractNumber(16));
    assertEquals(1, littleEndianOuput.extractNumber(1));
    assertEquals(2, littleEndianOuput.extractNumber(3));
    assertEquals(384, littleEndianOuput.extractNumber(9));
    assertEquals(1023, littleEndianOuput.extractNumber(10));
  }
}
