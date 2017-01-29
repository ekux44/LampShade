package com.kuxhausen.huemore.net.hue;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class HueBulbDataTest {

  @Test
  public void testMatches() {
    HueBulbData data1 = new HueBulbData();
    HueBulbData data2 = new HueBulbData();
    assertTrue(data1.matches(data2));

    data1.type = "Extended color light";
    assertFalse(data1.matches(data2));
    data2.type = "Dimmable Light";
    assertFalse(data1.matches(data2));
    data1.type = "Dimmable Light";
    assertTrue(data1.matches(data2));

    data1.modelid = "LCT001";
    assertFalse(data1.matches(data2));
    data2.modelid = "LLM010";
    assertFalse(data1.matches(data2));
    data1.modelid = "LLM010";
    assertTrue(data1.matches(data2));

    data1.swversion = "66009461";
    assertFalse(data1.matches(data2));
    data2.swversion = "66009462";
    assertFalse(data1.matches(data2));
    data1.swversion = "66009462";
    assertTrue(data1.matches(data2));

    data1.uniqueid = "A";
    assertFalse(data1.matches(data2));
    data2.uniqueid = "B";
    assertFalse(data1.matches(data2));
    data1.uniqueid = "B";
    assertTrue(data1.matches(data2));

    data1.manufacturername = "A";
    assertFalse(data1.matches(data2));
    data2.manufacturername = "B";
    assertFalse(data1.matches(data2));
    data1.manufacturername = "B";
    assertTrue(data1.matches(data2));

    data1.luminaireuniqueid = "A";
    assertFalse(data1.matches(data2));
    data2.luminaireuniqueid = "B";
    assertFalse(data1.matches(data2));
    data1.luminaireuniqueid = "B";
    assertTrue(data1.matches(data2));
  }
}
