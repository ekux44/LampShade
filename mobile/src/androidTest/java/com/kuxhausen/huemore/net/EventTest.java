package com.kuxhausen.huemore.net;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;

public class EventTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testConstructor() {
    try {
      Event e1 = new Event(null, 0, 0l);
      fail();
    } catch (IllegalArgumentException e) {
    }

    Event e2 = new Event(new BulbState(), 0, 0l);
    assertEquals(new BulbState(), e2.getBulbState());
    assertEquals(0, e2.getChannel());
    assertEquals(0l, e2.getMilliTime());
    assertEquals(0l, e2.getLegacyTime());

    BulbState state3 = new BulbState();
    state3.setOn(true);

    Event e3 = new Event(state3, 25, 300l);
    assertEquals(state3, e3.getBulbState());
    assertEquals(25, e3.getChannel());
    assertEquals(300l, e3.getMilliTime());
    assertEquals(3l, e3.getLegacyTime());

  }
}
