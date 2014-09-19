package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.persistence.Utils;

public class UtilsTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testDeciMilliConversions() {
    int[] decis = {1, 0, 123456, -5, Integer.MAX_VALUE};
    long[] millis = {100l, 0l, 12345600l, -500l, (Integer.MAX_VALUE * 100l)};

    for (int i = 0; i < decis.length; i++) {
      assertEquals(Utils.fromDeciSeconds(decis[i]), millis[i]);
      assertEquals(decis[i], Utils.toDeciSeconds(millis[i]));
    }
  }
}
