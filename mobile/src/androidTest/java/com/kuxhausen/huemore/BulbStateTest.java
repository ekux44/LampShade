package com.kuxhausen.huemore;

import com.google.gson.Gson;

import android.support.test.runner.AndroidJUnit4;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.BulbState.Effect;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BulbStateTest {

  private static final float DELTA = 1e-6f;

  @Test
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

  @Test
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

  @Test
  public void testMiredCT() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getMiredCT());

    bs.setMiredCT(153);
    assertEquals((Integer) 153, bs.getMiredCT());

    bs.setMiredCT(500);
    assertEquals((Integer) 500, bs.getMiredCT());

    bs.setMiredCT(0);
    assertEquals((Integer) 1, bs.getMiredCT());

    bs.setMiredCT(null);
    assertEquals(null, bs.getMiredCT());
  }

  @Test
  public void testKelvinCT() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getKelvinCT());

    bs.setKelvinCT(6535);
    assertEquals((Integer) 6535, bs.getKelvinCT());

    bs.setKelvinCT(2000);
    assertEquals((Integer) 2000, bs.getKelvinCT());

    bs.setKelvinCT(0);
    assertEquals((Integer) 1, bs.getKelvinCT());

    bs.setKelvinCT(null);
    assertEquals(null, bs.getKelvinCT());
  }

  @Test
  public void testXY(){
    BulbState bs = new BulbState();
    assertEquals(null, bs.getXY());

    bs.setXY(new float[]{.2f, .4f});
    assertEquals(.2f, bs.getXY()[0], DELTA);
    assertEquals(.4f, bs.getXY()[1], DELTA);

    // Test the XY array will never be a length other than 2, even if the device sends bad data
    Gson gson = new Gson();
    String json = "{\"xy\":[0.5]}";
    bs = gson.fromJson(json, BulbState.class);
    assertEquals(null, bs.getXY());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testTransitionTime() {
    BulbState bs = new BulbState();
    assertEquals(null, bs.getTransitionTime());

    bs.setTransitionTime(0);
    assertEquals((Integer) 0, bs.getTransitionTime());

    bs.setTransitionTime(600);
    assertEquals((Integer) 600, bs.getTransitionTime());

    bs.setTransitionTime(null);
    assertEquals(null, bs.getTransitionTime());

  }

  @Test
  public void testEquals() {
    BulbState state1 = new BulbState();
    BulbState state2 = new BulbState();
    assertEquals(state1, state2);

    state1.set255Bri(30);
    assertFalse(state1.equals(state2));
    state2.set255Bri(70);
    assertFalse(state1.equals(state2));
    state2.set255Bri(30);
    assertEquals(state1, state2);

    state1.setEffect(Effect.NONE);
    assertFalse(state1.equals(state2));
    state2.setEffect(Effect.COLORLOOP);
    assertFalse(state1.equals(state2));
    state2.setEffect(Effect.NONE);
    assertEquals(state1, state2);

    state1.setTransitionTime(600);
    assertFalse(state1.equals(state2));
    state2.setTransitionTime(10);
    assertFalse(state1.equals(state2));
    state2.setTransitionTime(600);
    assertEquals(state1, state2);

    state1.setAlert(Alert.FLASH_30SEC);
    assertFalse(state1.equals(state2));
    state2.setAlert(Alert.NONE);
    assertFalse(state1.equals(state2));
    state2.setAlert(Alert.FLASH_30SEC);
    assertEquals(state1, state2);

    state1.setKelvinCT(2500);
    assertFalse(state1.equals(state2));
    state2.setKelvinCT(6000);
    assertFalse(state1.equals(state2));
    state2.setKelvinCT(2500);
    assertEquals(state1, state2);

    state1.setOn(false);
    assertFalse(state1.equals(state2));
    state2.setOn(true);
    assertFalse(state1.equals(state2));
    state2.setOn(false);
    assertEquals(state1, state2);

    state1.setXY(new float[]{.2f, .2f});
    assertFalse(state1.equals(state2));
    state2.setXY(new float[]{.75f, .2f});
    assertFalse(state1.equals(state2));
    state2.setXY(new float[]{.2f, .75f});
    assertFalse(state1.equals(state2));
    state2.setXY(new float[]{.75f, .75f});
    assertFalse(state1.equals(state2));
    state2.setXY(new float[]{.2f, .2f});
    assertEquals(state1, state2);
  }
}
