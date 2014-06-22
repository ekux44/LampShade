package com.kuxhausen.huemore.net.hue.api;

import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

public class BasicErrorListener implements ErrorListener {

  ConnectionMonitor parrent;

  public BasicErrorListener(ConnectionMonitor parrentA) {
    parrent = parrentA;
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    if (parrent != null)
      parrent.setHubConnectionState(false);

    Log.e("volleyError", error.getLocalizedMessage() + "   ");
  }

}
