package com.kuxhausen.huemore;

import com.google.gson.Gson;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.BulbState.Effect;

public class BulbStateTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPercentBrightness() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getPercentBri());

    bs.setPercentBri(39);
    assertEquals((Integer) 39, bs.getPercentBri());

    bs.setPercentBri(100);
    assertEquals((Integer) 100, bs.getPercentBri());

    bs.setPercentBri(1);
    assertEquals((Integer) 1, bs.getPercentBri());

    bs.setPercentBri(200);
    assertEquals((Integer) 100, bs.getPercentBri());

    bs.setPercentBri(0);
    assertEquals((Integer) 1, bs.getPercentBri());

    bs.setPercentBri(null);
    assertEquals(null, bs.getPercentBri());
  }

  public void test256Brightness() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.get255Bri());

    bs.set255Bri(39);
    assertEquals((Integer) 39, bs.get255Bri());

    bs.set255Bri(255);
    assertEquals((Integer) 255, bs.get255Bri());

    bs.set255Bri(1);
    assertEquals((Integer) 1, bs.get255Bri());

    bs.set255Bri(400);
    assertEquals((Integer) 255, bs.get255Bri());

    bs.set255Bri(0);
    assertEquals((Integer) 1, bs.get255Bri());

    bs.set255Bri(null);
    assertEquals(null, bs.get255Bri());
  }

  public void testOn() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getOn());

    bs.setOn(true);
    assertEquals((Boolean) true, bs.getOn());

    bs.setOn(false);
    assertEquals((Boolean) false, bs.getOn());

    bs.setOn(null);
    assertEquals(null, bs.getOn());
  }

  public void testEffect() {
    Gson gson = new Gson();
    BulbState bs = new BulbState();
    assertEquals(null, bs.getEffect());

    bs.setEffect(Effect.NONE);
    String json1 = gson.toJson(bs);
    assertEquals(Effect.NONE, bs.getEffect());
    assertEquals(Effect.NONE, gson.fromJson(json1, BulbState.class).getEffect());

    bs.setEffect(Effect.COLORLOOP);
    String json2 = gson.toJson(bs);
    assertEquals(Effect.COLORLOOP, bs.getEffect());
    assertEquals(Effect.COLORLOOP, gson.fromJson(json2, BulbState.class).getEffect());

    bs.setEffect(null);
    String json3 = gson.toJson(bs);
    assertEquals(null, bs.getEffect());
    assertEquals(null, gson.fromJson(json3, BulbState.class).getEffect());
  }

  public void testAlert() {
    Gson gson = new Gson();
    BulbState bs = new BulbState();
    assertEquals(null, bs.getAlert());

    bs.setAlert(Alert.NONE);
    String json1 = gson.toJson(bs);
    assertEquals(Alert.NONE, bs.getAlert());
    assertEquals(Alert.NONE, gson.fromJson(json1, BulbState.class).getAlert());

    bs.setAlert(Alert.FLASH_ONCE);
    String json2 = gson.toJson(bs);
    assertEquals(Alert.FLASH_ONCE, bs.getAlert());
    assertEquals(Alert.FLASH_ONCE, gson.fromJson(json2, BulbState.class).getAlert());

    bs.setAlert(Alert.FLASH_30SEC);
    String json3 = gson.toJson(bs);
    assertEquals(Alert.FLASH_30SEC, bs.getAlert());
    assertEquals(Alert.FLASH_30SEC, gson.fromJson(json3, BulbState.class).getAlert());

    bs.setAlert(null);
    String json4 = gson.toJson(bs);
    assertEquals(null, bs.getAlert());
    assertEquals(null, gson.fromJson(json4, BulbState.class).getAlert());
  }
}
