package com.kuxhausen.huemore;

import android.support.test.runner.AndroidJUnit4;

import com.kuxhausen.huemore.persistence.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UtilsTest {

  @Test
  public void testDeciMilliConversions() {
    int[] decis = {1, 0, 123456, -5, Integer.MAX_VALUE};
    long[] millis = {100l, 0l, 12345600l, -500l, (Integer.MAX_VALUE * 100l)};

    for (int i = 0; i < decis.length; i++) {
      assertEquals(Utils.fromDeciSeconds(decis[i]), millis[i]);
      assertEquals(decis[i], Utils.toDeciSeconds(millis[i]));
    }
  }
}
