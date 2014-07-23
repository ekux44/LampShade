package com.kuxhausen.huemore.net.hue;

import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;

public class Route {

  public String address;
  public ConnectivityState state;

  public Route(String a, ConnectivityState cs) {
    address = a;
    state = cs;
  }

  /**
   * returns which is better connected Connected > Unknown > Unreachable
   *
   * @param other to compare with
   * @return false if equivalently connected
   */
  public boolean isMoreConnectedThan(ConnectivityState other) {
    if (state == ConnectivityState.Connected) {
      if (other == ConnectivityState.Unknown || other == ConnectivityState.Unreachable) {
        return true;
      }
    } else if (state == ConnectivityState.Unknown) {
      if (other == ConnectivityState.Unreachable) {
        return true;
      }
    }
    return false;
  }
}
