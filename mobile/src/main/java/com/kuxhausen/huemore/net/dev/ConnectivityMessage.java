package com.kuxhausen.huemore.net.dev;

import android.support.v4.util.Pair;

import com.kuxhausen.huemore.net.NetworkBulb;

import java.util.ArrayList;

/**
 * For encapsulating connectivity data associated with devices
 */
public class ConnectivityMessage {

  ArrayList<Pair<Long, NetworkBulb.ConnectivityState>> mData = new ArrayList<>();

  public void addDevice(long id, NetworkBulb.ConnectivityState connectivity) {
    mData.add(Pair.create(id, connectivity));
  }

  public ArrayList<Pair<Long, NetworkBulb.ConnectivityState>> getMessage() {
    return mData;
  }
}
