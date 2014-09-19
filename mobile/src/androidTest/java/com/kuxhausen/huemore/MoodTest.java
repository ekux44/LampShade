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
  public void functionality1() {
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
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
  Tests the Deep Sea mood
   */
  public void functionality2() {
    BulbState bs1 = new BulbState();
    bs1.setXY(new float[]{.3f, .4f});

    BulbState bs2 = new BulbState();
    bs2.setXY(new float[]{.1f, .6f});

    Event e1 = new Event(bs1, 0, 0);
    Event e2 = new Event(bs2, 1, 0);
    Event e3 = new Event(bs1, 2, 0);

    Mood m = new Mood();
    m.events = new Event[]{e1, e2, e3};

    assertFalse(m.getTimeAddressingRepeatPolicy());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(3, m.getNumChannels());
    assertEquals(1, m.getNumTimeslots());
    assertEquals(1, m.getEventStatesAsSparseMatrix().length);
    assertEquals(3, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
    Tests a sunset mood
   */
  public void functionality3() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setOn(false);

    Event e1 = new Event(bs1, 1, 0);
    Event e2 = new Event(bs2, 1, 5000l);

    Mood m = new Mood();
    m.events = new Event[]{e1, e2};
    m.setNumChannels(2);

    assertFalse(m.getTimeAddressingRepeatPolicy());
    assertFalse(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(2, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
    Tests a cycling mood
   */
  public void functionality4() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setMiredCT(200);

    Event e1 = new Event(bs1, 1, 0);
    Event e2 = new Event(bs2, 1, 1000l);

    Mood m = new Mood();
    m.events = new Event[]{e1, e2};
    m.setNumChannels(2);
    m.setInfiniteLooping(true);
    m.setLoopMilliTime(2000l);

    assertFalse(m.getTimeAddressingRepeatPolicy());
    assertTrue(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(2, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
    Tests a daily mood
   */
  public void functionality5() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setMiredCT(200);

    Event e1 = new Event(bs1, 0, 123456l);
    Event e2 = new Event(bs2, 0, 2468024l);

    Mood m = new Mood();
    m.events = new Event[]{e1, e2};
    m.setNumChannels(2);
    m.setTimeAddressingRepeatPolicy(true);

    assertTrue(m.getTimeAddressingRepeatPolicy());
    assertTrue(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }
}
