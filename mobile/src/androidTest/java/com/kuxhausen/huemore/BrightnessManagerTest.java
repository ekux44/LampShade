package com.kuxhausen.huemore;

import android.test.AndroidTestCase;

import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.BrightnessManager.BrightnessPolicy;
import com.kuxhausen.huemore.net.NetworkBulb;

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

  public void testPolicy() {
    BrightnessManager manager = new BrightnessManager(new ArrayList<NetworkBulb>());
    assertEquals(manager.getPolicy(), BrightnessPolicy.DIRECT_BRI);

    manager.setPolicy(BrightnessPolicy.VOLUME_BRI);
    assertEquals(manager.getPolicy(), BrightnessPolicy.VOLUME_BRI);

    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    manager.setPolicy(BrightnessPolicy.DIRECT_BRI);
    assertEquals(manager.getPolicy(), BrightnessPolicy.DIRECT_BRI);
  }
}
