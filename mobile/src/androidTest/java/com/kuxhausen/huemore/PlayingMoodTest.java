package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.net.PlayingMood;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

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


  }

}
