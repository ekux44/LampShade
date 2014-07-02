package com.kuxhausen.huemore.net.hue.api;

import com.android.volley.Response.Listener;
import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;
import com.kuxhausen.huemore.net.hue.Route;

public class BasicSuccessListener<T> implements Listener<T> {

  ConnectionMonitor parrent;
  Route mRoute;

  public BasicSuccessListener(ConnectionMonitor parrentA, Route route) {
    parrent = parrentA;
    mRoute = route;
  }

  @Override
  public void onResponse(T response) {
    if (parrent != null)
      parrent.setHubConnectionState(mRoute, ConnectivityState.Connected);
  }

}
