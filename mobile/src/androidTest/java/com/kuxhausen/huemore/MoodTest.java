package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

public class MoodTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testConstructor() {
    Mood m = new Mood();
    assertFalse(m.getTimeAddressingRepeatPolicy());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(0, m.getNumTimeslots());
    assertEquals(0, m.getEventStatesAsSparseMatrix().length);
  }

  /*
  Tests the Off mood
   */
  public void functionality1(){
    BulbState bs1 = new BulbState();
    bs1.setOn(false);

    Event e1 = new Event(bs1, 0, 0);

    Mood m = new Mood();
    m.events = new Event[]{e1};

    assertFalse(m.getTimeAddressingRepeatPolicy());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(1, m.getNumTimeslots());
    assertEquals(1, m.getEventStatesAsSparseMatrix().length);
  }
}
