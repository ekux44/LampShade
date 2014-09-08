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
      new PlayingMood(null, "", new Group(null, null), 1);
      fail();
    } catch (IllegalArgumentException e) {
    }

    new PlayingMood(new Mood(), null, new Group(null, null), 1);

    try {
      new PlayingMood(new Mood(), "", null, 1);
      fail();
    } catch (IllegalArgumentException e) {
    }

    try {
      new PlayingMood(new Mood(), "", new Group(null, null), 0);
      fail();
    } catch (IllegalArgumentException e) {
    }

    String mName = "some mood";
    String gName = "some group";
    Mood m = new Mood();
    Group g = new Group(null, gName);
    PlayingMood pm = new PlayingMood(m, mName, g, 1);
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

    Event e1 = new Event(bs1, 0, 0);
    Event[] eRay = {e1};

    Mood m = new Mood();
    m.events = eRay;

    Long bulb1 = 123l;
    Long[] bulbs = {bulb1};
    Group g = new Group(Arrays.asList(bulbs), "");

    long startTime = 543l;

    PlayingMood pm = new PlayingMood(m, "", g, startTime);

    assertTrue(pm.hasFutureEvents());

    assertEquals(startTime, pm.getNextEventInCurrentMillis());

    List<Pair<List<Long>, BulbState>> toPlay = pm.tick(startTime);
    assertEquals(1, toPlay.size());
    assertEquals(bs1, toPlay.get(0).second);
    assertEquals(1, toPlay.get(0).first.size());
    assertEquals(bulb1, toPlay.get(0).first.get(0));

    assertFalse(pm.hasFutureEvents());
  }

}
