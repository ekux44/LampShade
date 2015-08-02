package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.net.dev.StateMessage;

public interface DeviceListener {

  public abstract void deviceStateChanged(StateMessage state);
}
