package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.android.volley.Network;
import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.BrightnessManager.BrightnessPolicy;
import com.kuxhausen.huemore.net.MockNetBulb;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.state.BulbState;

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
  public void testSetBrightness1(){
    ArrayList<NetworkBulb> list = new ArrayList<NetworkBulb>();
    MockNetBulb aBulb = new MockNetBulb();
    MockNetBulb bBulb = new MockNetBulb();
    list.add(aBulb);
    list.add(bBulb);

    //remember to keep raw manipulation scaled by BS.bri range (0-255)
    aBulb.mKnown.bri = 51;
    bBulb.mKnown.bri = 102;

    BrightnessManager manager = new BrightnessManager(list);
    //should return the initial average of known, scaled from 1-100
    assertEquals(manager.getBrightness(), 30);

    manager.setBrightness(60);
    assertEquals(manager.getBrightness(), 60);

    manager.setPolicy(BrightnessPolicy.VOLUME_BRI);
    assertEquals(manager.getBrightness(),60);

    manager.setBrightness(40);
    assertEquals(manager.getBrightness(),40);

    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    assertEquals(manager.getBrightness(),40);
  }

  public void testSetState1() {
    try {
      BrightnessManager manager = new BrightnessManager(new ArrayList<NetworkBulb>());
      manager.setState(new MockNetBulb(), new BulbState());
      fail();
    } catch (AssertionError e) {
    }
  }

  public void testSetState2(){
    ArrayList<NetworkBulb> list = new ArrayList<NetworkBulb>();
    MockNetBulb aBulb = new MockNetBulb();
    MockNetBulb bBulb = new MockNetBulb();
    list.add(aBulb);
    list.add(bBulb);

    BulbState aState = new BulbState();
    aState.bri = 129;
    aState.ct = 300;
    aState.transitiontime = 60;

    BulbState bState = new BulbState();
    bState.effect = "colorloop";
    bState.alert = "lselect";
    bState.ct = 300;

    BrightnessManager manager = new BrightnessManager(list);
    manager.setState(aBulb, aState);

    manager.setState(bBulb, bState);

    assertEquals(aBulb.getState(NetworkBulb.GetStateConfidence.KNOWN), aState);
    assertEquals(bBulb.getState(NetworkBulb.GetStateConfidence.DESIRED), bState);
    //TODO

  }
}
