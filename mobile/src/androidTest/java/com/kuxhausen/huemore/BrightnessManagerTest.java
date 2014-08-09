package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.BrightnessManager.BrightnessPolicy;
import com.kuxhausen.huemore.net.MockNetBulb;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.BulbState.Effect;

import java.util.ArrayList;

public class BrightnessManagerTest extends AndroidTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testConstructor() {
    try {
      BrightnessManager manager = new BrightnessManager(null);
      fail();
    } catch (AssertionError e) {
    }
  }

  public void testPolicyToggle1() {
    BrightnessManager manager = new BrightnessManager(new ArrayList<NetworkBulb>());
    assertEquals(manager.getPolicy(), BrightnessPolicy.DIRECT_BRI);

    manager.setPolicy(BrightnessPolicy.VOLUME_BRI);
    assertEquals(manager.getPolicy(), BrightnessPolicy.VOLUME_BRI);

    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    assertEquals(manager.getPolicy(), BrightnessPolicy.DIRECT_BRI);
  }

  //test get/set brightness across the policies
  public void testSetBrightness1() {
    ArrayList<NetworkBulb> list = new ArrayList<NetworkBulb>();
    MockNetBulb aBulb = new MockNetBulb();
    MockNetBulb bBulb = new MockNetBulb();
    list.add(aBulb);
    list.add(bBulb);

    //remember to keep raw manipulation scaled by BS.bri range (0-255)
    aBulb.mKnown.setPercentBri(20);
    bBulb.mKnown.setPercentBri(40);

    BrightnessManager manager = new BrightnessManager(list);
    //should return the initial average of known, scaled from 1-100
    assertEquals(manager.getBrightness(), 30);

    manager.setBrightness(60);
    assertEquals(manager.getBrightness(), 60);

    manager.setPolicy(BrightnessPolicy.VOLUME_BRI);
    assertEquals(manager.getBrightness(), 60);

    manager.setBrightness(40);
    assertEquals(manager.getBrightness(), 40);

    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    assertEquals(manager.getBrightness(), 40);
  }

  public void testSetState1() {
    try {
      BrightnessManager manager = new BrightnessManager(new ArrayList<NetworkBulb>());
      manager.setState(new MockNetBulb(), new BulbState());
      fail();
    } catch (AssertionError e) {
    }
  }

  public void testSetState2() {
    ArrayList<NetworkBulb> list = new ArrayList<NetworkBulb>();
    MockNetBulb aBulb = new MockNetBulb();
    MockNetBulb bBulb = new MockNetBulb();
    list.add(aBulb);
    list.add(bBulb);

    BrightnessManager manager = new BrightnessManager(list);

    BulbState emptyState = new BulbState();
    assertEquals(emptyState, aBulb.getState(NetworkBulb.GetStateConfidence.DESIRED));
    assertEquals(emptyState, aBulb.getState(NetworkBulb.GetStateConfidence.KNOWN));
    assertFalse(emptyState.equals(aBulb.getState(NetworkBulb.GetStateConfidence.GUESS)));

    BulbState state1 = new BulbState();
    state1.setPercentBri(37);
    state1.setMiredCT(300);
    state1.transitiontime = 60;

    aBulb.mKnown = state1;
    assertEquals(state1, aBulb.getState(NetworkBulb.GetStateConfidence.DESIRED));
    assertEquals(state1, aBulb.getState(NetworkBulb.GetStateConfidence.KNOWN));

    BulbState state2 = new BulbState();
    state2.setPercentBri(97);
    state2.setEffect(Effect.COLORLOOP);
    state2.setAlert(Alert.FLASH_30SEC);
    state2.setMiredCT(300);

    manager.setState(bBulb, state2);
    assertEquals(state2, bBulb.getState(NetworkBulb.GetStateConfidence.DESIRED));
    assertEquals(emptyState, bBulb.getState(NetworkBulb.GetStateConfidence.KNOWN));

    manager.setState(aBulb, state2);
    assertEquals(state2, bBulb.getState(NetworkBulb.GetStateConfidence.DESIRED));
    BulbState combined = state1.clone();
    combined.merge(state2);
    assertEquals(combined, bBulb.getState(NetworkBulb.GetStateConfidence.KNOWN));
  }
}
