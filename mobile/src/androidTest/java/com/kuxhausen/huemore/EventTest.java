package com.kuxhausen.huemore;

import android.support.test.runner.AndroidJUnit4;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class EventTest {

  @Test
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

  @Test
  public void testEquals() {
    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.setOn(true);

    Event e1 = new Event(bs1, 0, 0);
    Event e2 = new Event(bs1, 1, 0);
    Event e3 = new Event(bs1, 0, 100l);
    Event e4 = new Event(bs2, 0, 0);

    assertEquals(e1, e1);
    assertFalse(e1.equals(e2));
    assertFalse(e1.equals(e3));
    assertEquals(e1, e4);

    Event e5 = new Event(bs1, 2, 5000l);
    Event e6 = new Event(bs1, 2, 5000l);

    assertFalse(e5.equals(e1));
    assertFalse(e5.equals(e2));
    assertFalse(e5.equals(e3));
    assertEquals(e5, e6);
  }
}
