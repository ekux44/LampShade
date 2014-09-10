package com.kuxhausen.huemore;

import android.test.AndroidTestCase;
import android.util.Pair;

import com.kuxhausen.huemore.net.PlayingMood;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

import java.util.Arrays;
import java.util.List;

public class PlayingMoodTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testConstructor() {
    try {
      new PlayingMood(null, "", new Group(null, null), 1, -1000);
      fail();
    } catch (IllegalArgumentException e) {
    }

    new PlayingMood(new Mood(), null, new Group(null, null), 1, -1000);

    try {
      new PlayingMood(new Mood(), "", null, 1, -1000);
      fail();
    } catch (IllegalArgumentException e) {
    }

    try {
      new PlayingMood(new Mood(), "", new Group(null, null), 0, -1000);
      fail();
    } catch (IllegalArgumentException e) {
    }

    String mName = "some mood";
    String gName = "some group";
    Mood m = new Mood();
    Group g = new Group(null, gName);
    PlayingMood pm = new PlayingMood(m, mName, g, 1, -1000);
    assertEquals(g, pm.getGroup());
    assertEquals(m, pm.getMood());
    assertEquals(mName, pm.getMoodName());
    assertEquals(gName, pm.getGroupName());
  }

  /**
   * playing a simple on mood
   */
  public void testFunctionality1() {
    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.set255Bri(127);

    Event e1 = new Event(bs1, 0);
    e1.setMilliTime(0);
    Event e2 = new Event(bs2, 1);
    e2.setMilliTime(0);
    Event[] eRay = {e1, e2};

    Mood m = new Mood();
    m.events = eRay;
    m.setNumChannels(2);

    Long bulb1 = 123l;
    Long bulb2 = 456l;
    Long[] bulbs = {bulb1, bulb2};
    Group g = new Group(Arrays.asList(bulbs), "");

    long startTime = 543l;
    long dayStartTime = 12l;

    PlayingMood pm = new PlayingMood(m, "", g, startTime, dayStartTime);

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime, pm.getNextEventInCurrentMillis());

    List<Pair<List<Long>, BulbState>> toPlay = pm.tick(startTime);
    assertEquals(2, toPlay.size());
    assertEquals(bs1, toPlay.get(0).second);
    assertEquals(1, toPlay.get(0).first.size());
    assertEquals(bulb1, toPlay.get(0).first.get(0));
    assertEquals(bs2, toPlay.get(1).second);
    assertEquals(1, toPlay.get(1).first.size());
    assertEquals(bulb2, toPlay.get(1).first.get(0));

    assertFalse(pm.hasFutureEvents());
    assertEquals(0, pm.tick(startTime + 1).size());
  }


  /**
   * playing a timed, non-looping mood
   */
  public void testFunctionality2() {
    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.set255Bri(127);

    Event e1 = new Event(bs1, 0);
    e1.setMilliTime(0);
    Event e2 = new Event(bs2, 1);
    e2.setMilliTime(100);
    Event[] eRay = {e1, e2};

    Mood m = new Mood();
    m.events = eRay;
    m.setNumChannels(2);

    Long bulb1 = 123l;
    Long bulb2 = 456l;
    Long[] bulbs = {bulb1, bulb2};
    Group g = new Group(Arrays.asList(bulbs), "");

    long startTime = 543l;
    long dayStartTime = 12l;

    PlayingMood pm = new PlayingMood(m, "", g, startTime, dayStartTime);

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime, pm.getNextEventInCurrentMillis());

    List<Pair<List<Long>, BulbState>> tick1 = pm.tick(startTime);
    assertEquals(1, tick1.size());
    assertEquals(bs1, tick1.get(0).second);
    assertEquals(1, tick1.get(0).first.size());
    assertEquals(bulb1, tick1.get(0).first.get(0));

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 100, pm.getNextEventInCurrentMillis());

    assertEquals(0, pm.tick(startTime + 50).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 100, pm.getNextEventInCurrentMillis());

    List<Pair<List<Long>, BulbState>> tick3 = pm.tick(startTime + 100);
    assertEquals(1, tick3.size());
    assertEquals(bs2, tick3.get(0).second);
    assertEquals(1, tick3.get(0).first.size());
    assertEquals(bulb2, tick3.get(0).first.get(0));

    assertFalse(pm.hasFutureEvents());
    assertEquals(0, pm.tick(startTime + 150).size());
  }

  /**
   * playing a timed looping mood
   */
  public void testFunctionality3() {
    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.set255Bri(127);

    Event e1 = new Event(bs1, 0);
    e1.setMilliTime(0);
    Event e2 = new Event(bs2, 1);
    e2.setMilliTime(100);
    Event[] eRay = {e1, e2};

    Mood m = new Mood();
    m.events = eRay;
    m.setNumChannels(2);
    m.setInfiniteLooping(true);
    m.setLoopMilliTime(200);

    Long bulb1 = 123l;
    Long bulb2 = 456l;
    Long[] bulbs = {bulb1, bulb2};
    Group g = new Group(Arrays.asList(bulbs), "");

    long startTime = 543l;
    long dayStartTime = 12l;

    PlayingMood pm = new PlayingMood(m, "", g, startTime, dayStartTime);

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 0, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick1 = pm.tick(startTime + 0);
    assertEquals(1, tick1.size());
    assertEquals(bs1, tick1.get(0).second);
    assertEquals(1, tick1.get(0).first.size());
    assertEquals(bulb1, tick1.get(0).first.get(0));

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 100, pm.getNextEventInCurrentMillis());

    assertEquals(0, pm.tick(startTime + 1).size());

    assertEquals(0, pm.tick(startTime + 99).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 100, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick3 = pm.tick(startTime + 100);
    assertEquals(1, tick3.size());
    assertEquals(bs2, tick3.get(0).second);
    assertEquals(1, tick3.get(0).first.size());
    assertEquals(bulb2, tick3.get(0).first.get(0));

    assertEquals(0, pm.tick(startTime + 101).size());

    assertEquals(0, pm.tick(startTime + 199).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 200, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick5 = pm.tick(startTime + 200);
    assertEquals(1, tick5.size());
    assertEquals(bs1, tick5.get(0).second);
    assertEquals(1, tick5.get(0).first.size());
    assertEquals(bulb1, tick5.get(0).first.get(0));

    assertEquals(0, pm.tick(startTime + 201).size());

    assertEquals(0, pm.tick(startTime + 299).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 300, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick7 = pm.tick(startTime + 300);
    assertEquals(1, tick7.size());
    assertEquals(bs2, tick7.get(0).second);
    assertEquals(1, tick7.get(0).first.size());
    assertEquals(bulb2, tick7.get(0).first.get(0));

    assertEquals(0, pm.tick(startTime + 301).size());

    assertEquals(0, pm.tick(startTime + 399).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(startTime + 400, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick9 = pm.tick(startTime + 400);
    assertEquals(1, tick9.size());
    assertEquals(bs1, tick9.get(0).second);
    assertEquals(1, tick9.get(0).first.size());
    assertEquals(bulb1, tick9.get(0).first.get(0));

    assertTrue(pm.hasFutureEvents());
  }

  /**
   * playing a daily mood
   */
  public void testFunctionality4() {
    long startTime = 543l;
    long dayStartTime = 12l;

    long millisPerHour = 3600000;

    BulbState bs1 = new BulbState();
    bs1.setOn(true);

    BulbState bs2 = new BulbState();
    bs2.set255Bri(127);

    Event e1 = new Event(bs1, 0);
    e1.setMilliTime(dayStartTime + 5 * millisPerHour);
    Event e2 = new Event(bs2, 1);
    e2.setMilliTime(dayStartTime + 13 * millisPerHour);
    Event[] eRay = {e1, e2};

    Mood m = new Mood();
    m.events = eRay;
    m.setNumChannels(2);
    m.setTimeAddressingRepeatPolicy(true);

    Long bulb1 = 123l;
    Long bulb2 = 456l;
    Long[] bulbs = {bulb1, bulb2};
    Group g = new Group(Arrays.asList(bulbs), "");

    PlayingMood pm = new PlayingMood(m, "", g, startTime, dayStartTime);

    assertTrue(pm.hasFutureEvents());
    assertEquals(dayStartTime + 5 * millisPerHour, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick1 = pm.tick(dayStartTime + 1 * millisPerHour);
    assertEquals(2, tick1.size());
    assertEquals(bs1, tick1.get(0).second);
    assertEquals(1, tick1.get(0).first.size());
    assertEquals(bulb1, tick1.get(0).first.get(0));
    assertEquals(bs2, tick1.get(1).second);
    assertEquals(1, tick1.get(1).first.size());
    assertEquals(bulb2, tick1.get(1).first.get(0));

    assertTrue(pm.hasFutureEvents());
    assertEquals(dayStartTime + 5 * millisPerHour, pm.getNextEventInCurrentMillis());

    assertEquals(0, pm.tick(dayStartTime + 2 * millisPerHour).size());

    assertEquals(0, pm.tick(dayStartTime + 3 * millisPerHour).size());

    assertTrue(pm.hasFutureEvents());
    assertEquals(dayStartTime + 5 * millisPerHour, pm.getNextEventInCurrentMillis());
    List<Pair<List<Long>, BulbState>> tick3 = pm.tick(dayStartTime + 5 * millisPerHour);
    assertEquals(1, tick3.size());
    assertEquals(bs1, tick3.get(0).second);
    assertEquals(1, tick3.get(0).first.size());
    assertEquals(bulb1, tick3.get(0).first.get(0));

    //TODO test remaining part of day and looping over with next day

  }
}