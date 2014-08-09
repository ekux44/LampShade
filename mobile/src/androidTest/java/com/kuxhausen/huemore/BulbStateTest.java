package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.state.BulbState;

public class BulbStateTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPercentBrightness() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getPercentBri());

    bs.setPercentBri(39);
    assertEquals((Integer) 39, bs.getPercentBri());

    bs.setPercentBri(100);
    assertEquals((Integer) 100, bs.getPercentBri());

    bs.setPercentBri(1);
    assertEquals((Integer) 1, bs.getPercentBri());

    bs.setPercentBri(200);
    assertEquals((Integer) 100, bs.getPercentBri());

    bs.setPercentBri(0);
    assertEquals((Integer) 1, bs.getPercentBri());

    bs.setPercentBri(null);
    assertEquals(null, bs.getPercentBri());
  }

  public void test256Brightness() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.get255Bri());

    bs.set255Bri(39);
    assertEquals((Integer) 39, bs.get255Bri());

    bs.set255Bri(255);
    assertEquals((Integer) 255, bs.get255Bri());

    bs.set255Bri(1);
    assertEquals((Integer) 1, bs.get255Bri());

    bs.set255Bri(400);
    assertEquals((Integer) 255, bs.get255Bri());

    bs.set255Bri(0);
    assertEquals((Integer) 1, bs.get255Bri());

    bs.set255Bri(null);
    assertEquals(null, bs.get255Bri());
  }
}
