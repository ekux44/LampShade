package com.kuxhausen.huemore.net.dev;

import android.content.Context;

public class SampleDeviceDriver implements DeviceDriver {

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
