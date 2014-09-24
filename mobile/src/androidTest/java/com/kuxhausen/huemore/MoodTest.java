package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

import java.util.Arrays;

//TODO flush out
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

    BulbState bs = new BulbState();
    bs.setOn(true);
    Mood m2 = new Mood(bs);
    assertFalse(m2.getTimeAddressingRepeatPolicy());
    assertFalse(m2.isInfiniteLooping());
    assertTrue(m2.isSimple());
    assertEquals(1, m2.getNumChannels());
    assertEquals(1, m2.getNumTimeslots());
    assertTrue(Arrays.equals(new Event[]{new Event(bs, 0, 0l)}, m2.getEvents()));

  }

  public void testEquals() {
    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.setOn(true);

    Event e1 = new Event(bs1, 0, 0);
    Event e2 = new Event(bs1, 1, 0);
    Event e3 = new Event(bs1, 0, 100l);
    Event e4 = new Event(bs2, 0, 0);

    Mood m1 = new Mood();
    m1.setLoopMilliTime(500l);
    m1.setInfiniteLooping(false);
    m1.setTimeAddressingRepeatPolicy(false);
    m1.setNumChannels(2);
    m1.setEvents(new Event[]{e1, e2, e3});

    assertEquals(m1, m1);

    Mood m2 = new Mood();
    m2.setLoopMilliTime(500l);
    m2.setInfiniteLooping(true);
    m2.setTimeAddressingRepeatPolicy(false);
    m2.setNumChannels(2);
    m2.setEvents(new Event[]{e1, e2, e3});

    assertFalse(m1.equals(m2));

    Mood m3 = new Mood();
    m3.setLoopMilliTime(500l);
    m3.setInfiniteLooping(false);
    m3.setTimeAddressingRepeatPolicy(true);
    m3.setNumChannels(2);
    m3.setEvents(new Event[]{e1, e2, e3});

    assertFalse(m1.equals(m3));

    Mood m4 = new Mood();
    m4.setLoopMilliTime(500l);
    m4.setInfiniteLooping(false);
    m4.setTimeAddressingRepeatPolicy(true);
    m4.setNumChannels(3);
    m4.setEvents(new Event[]{e1, e2, e3});

    assertFalse(m1.equals(m4));

    Mood m5 = new Mood();
    m5.setLoopMilliTime(2300l);
    m5.setInfiniteLooping(false);
    m5.setTimeAddressingRepeatPolicy(false);
    m5.setNumChannels(2);
    m5.setEvents(new Event[]{e1, e2, e3});

    assertFalse(m1.equals(m5));

    Mood m6 = new Mood();
    m6.setLoopMilliTime(500l);
    m6.setInfiniteLooping(false);
    m6.setTimeAddressingRepeatPolicy(false);
    m6.setNumChannels(2);
    m6.setEvents(new Event[]{e1, e2});

    assertFalse(m1.equals(m6));

    Mood m7 = new Mood();
    m7.setLoopMilliTime(500l);
    m7.setInfiniteLooping(false);
    m7.setTimeAddressingRepeatPolicy(false);
    m7.setNumChannels(2);
    m7.setEvents(new Event[]{e4, e2, e3});

    assertEquals(m1, m7);
  }

  /*
  Tests the Off mood
   */
  public void functionality1() {
    BulbState bs1 = new BulbState();
    bs1.setOn(false);

    Event e1 = new Event(bs1, 0, 0);

    Mood m = new Mood();
    m.setEvents(new Event[]{e1});

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
    m.setEvents(new Event[]{e1, e2, e3});

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
    m.setEvents(new Event[]{e1, e2});
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
    m.setEvents(new Event[]{e1, e2});
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
    m.setEvents(new Event[]{e1, e2});
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
