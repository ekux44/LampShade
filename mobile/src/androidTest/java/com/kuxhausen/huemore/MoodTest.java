package com.kuxhausen.huemore;

import android.support.test.runner.AndroidJUnit4;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MoodTest {

  @Test
  public void testConstructor_default() {
    Mood m = new Mood.Builder().build();

    assertFalse(m.isRelativeToMidnight());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(0, m.getNumTimeslots());
    assertEquals(0, m.getEventStatesAsSparseMatrix().length);
  }
  @Test
  public void testConstructor_bulbState() {
    BulbState bs = new BulbState();
    bs.setOn(true);
    Mood m = new Mood.Builder(bs).build();

    assertFalse(m.isRelativeToMidnight());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(1, m.getNumTimeslots());
    assertTrue(Arrays.equals(new Event[]{new Event(bs, 0, 0l)}, m.getEvents()));
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

    Mood m1 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(false)
        .setNumChannels(2)
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertTrue(m1.equals(m1));

    Mood m2 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(true)
        .setRelativeToMidnight(false)
        .setNumChannels(2)
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertFalse(m1.equals(m2));

    Mood m3 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(true)
        .setNumChannels(2)
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertFalse(m1.equals(m3));

    Mood m4 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(true)
        .setNumChannels(3)
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertFalse(m1.equals(m4));

    Mood m5 = new Mood.Builder()
        .setLoopMilliTime(2300l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(false)
        .setNumChannels(2)
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertFalse(m1.equals(m5));

    Mood m6 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(false)
        .setNumChannels(2)
        .setEvents(new Event[]{e1, e2})
        .build();

    assertFalse(m1.equals(m6));

    Mood m7 = new Mood.Builder()
        .setLoopMilliTime(500l)
        .setInfiniteLooping(false)
        .setRelativeToMidnight(false)
        .setNumChannels(2)
        .setEvents(new Event[]{e4, e2, e3})
        .build();

    assertTrue(m1.equals(m7));
  }

  /*
   * Tests the Off mood
   */
  @Test
  public void functionality1() {
    BulbState bs1 = new BulbState();
    bs1.setOn(false);

    Event e1 = new Event(bs1, 0, 0);

    Mood m = new Mood.Builder()
        .setEvents(new Event[]{e1})
        .build();

    assertFalse(m.isRelativeToMidnight());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(1, m.getNumTimeslots());
    assertEquals(1, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
   * Tests the Deep Sea mood
   */
  @Test
  public void functionality2() {
    BulbState bs1 = new BulbState();
    bs1.setXY(new float[]{.3f, .4f});

    BulbState bs2 = new BulbState();
    bs2.setXY(new float[]{.1f, .6f});

    Event e1 = new Event(bs1, 0, 0);
    Event e2 = new Event(bs2, 1, 0);
    Event e3 = new Event(bs1, 2, 0);

    Mood m = new Mood.Builder()
        .setEvents(new Event[]{e1, e2, e3})
        .build();

    assertFalse(m.isRelativeToMidnight());
    assertFalse(m.isInfiniteLooping());
    assertTrue(m.isSimple());
    assertEquals(3, m.getNumChannels());
    assertEquals(1, m.getNumTimeslots());
    assertEquals(1, m.getEventStatesAsSparseMatrix().length);
    assertEquals(3, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
   * Tests a sunset mood
   */
  @Test
  public void functionality3() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setOn(false);

    Event e1 = new Event(bs1, 1, 0);
    Event e2 = new Event(bs2, 1, 5000l);

    Mood m = new Mood.Builder()
        .setEvents(new Event[]{e1, e2})
        .setNumChannels(2)
        .build();

    assertFalse(m.isRelativeToMidnight());
    assertFalse(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(2, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
   * Tests a cycling mood
   */
  @Test
  public void functionality4() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setMiredCT(200);

    Event e1 = new Event(bs1, 1, 0);
    Event e2 = new Event(bs2, 1, 1000l);

    Mood m = new Mood.Builder()
        .setEvents(new Event[]{e1, e2})
        .setNumChannels(2)
        .setInfiniteLooping(true)
        .setLoopMilliTime(2000l)
        .build();

    assertFalse(m.isRelativeToMidnight());
    assertTrue(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(2, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }

  /*
   * Tests a daily mood
   */
  @Test
  public void functionality5() {
    BulbState bs1 = new BulbState();
    bs1.setMiredCT(333);

    BulbState bs2 = new BulbState();
    bs2.setMiredCT(200);

    Event e1 = new Event(bs1, 0, 123456l);
    Event e2 = new Event(bs2, 0, 2468024l);

    Mood m = new Mood.Builder()
        .setEvents(new Event[]{e1, e2})
        .setNumChannels(2)
        .setRelativeToMidnight(true)
        .build();

    assertTrue(m.isRelativeToMidnight());
    assertTrue(m.isInfiniteLooping());
    assertFalse(m.isSimple());
    assertEquals(1, m.getNumChannels());
    assertEquals(2, m.getNumTimeslots());
    assertEquals(2, m.getEventStatesAsSparseMatrix().length);
    assertEquals(1, m.getEventStatesAsSparseMatrix()[0].length);
  }
}
