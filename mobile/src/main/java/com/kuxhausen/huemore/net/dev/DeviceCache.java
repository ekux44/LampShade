package com.kuxhausen.huemore.net.dev;

import android.support.v4.util.LongSparseArray;

import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;
import com.kuxhausen.huemore.state.BulbState;

public class DeviceCache {

  // Indexed by device id
  private LongSparseArray<BulbState> mBulbStates = new LongSparseArray<>();
  private LongSparseArray<ConnectivityState> mConnectivity = new LongSparseArray<>();

  public BulbState getBulbState(long deviceId) {
    return mBulbStates.get(deviceId);
  }

  public void setBulbState(long deviceId, BulbState newState) {
    mBulbStates.put(deviceId, newState);
  }

  public ConnectivityState getConnectivity(long deviceId) {
    return mConnectivity.get(deviceId);
  }

  public void getConnectivityState(long deviceId, ConnectivityState connectivity) {
    mConnectivity.put(deviceId, connectivity);
  }
}
