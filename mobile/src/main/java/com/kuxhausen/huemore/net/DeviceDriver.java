package com.kuxhausen.huemore.net;

import android.content.Context;
import android.support.v4.util.Pair;

import com.kuxhausen.huemore.net.dev.StateMessage;

import java.util.List;

public interface DeviceDriver {

  public abstract boolean initialize(Context c, DeviceListener listener);

  /**
   * @return list of (bulb id, connectivity)
   */
  public abstract List<Pair<Long, NetworkBulb.ConnectivityState>> getBulbConnectivity();

  /**
   * @return list of (connection id, connectivity)
   */
  public abstract List<Pair<Long, NetworkBulb.ConnectivityState>> getConnectionConnectivity();

  public abstract void targetStateChanged(StateMessage targetState);

  public abstract void bulbNameChanged(Long bulbId, String name);

  public abstract StateMessage getState();

  public abstract void launchOnboarding();
}
