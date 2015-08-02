package com.kuxhausen.huemore.net.dev;

import android.content.Context;
import android.support.v4.util.Pair;

import com.kuxhausen.huemore.net.DeviceDriver;
import com.kuxhausen.huemore.net.DeviceListener;
import com.kuxhausen.huemore.net.NetworkBulb;

import java.util.List;

public class SampleDeviceDriver implements DeviceDriver{

  @Override
  public boolean initialize(Context c, DeviceListener listener) {
    return false;
  }

  /**
   * @return list of (bulb id, connectivity)
   */
  @Override
  public ConnectivityMessage getBulbConnectivity() {
    return null;
  }

  /**
   * @return list of (connection id, connectivity)
   */
  @Override
  public ConnectivityMessage getConnectionConnectivity() {
    return null;
  }

  @Override
  public void targetStateChanged(StateMessage targetState) {

  }

  @Override
  public void bulbNameChanged(BulbNameMessage targetName) {

  }

  @Override
  public StateMessage getState() {
    return null;
  }

  @Override
  public void launchOnboarding() {

  }
}
