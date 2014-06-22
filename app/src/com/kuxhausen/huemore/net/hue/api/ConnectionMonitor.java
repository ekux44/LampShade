package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.Route;

public interface ConnectionMonitor {
  public abstract void setHubConnectionState(Route r, boolean b);
}
