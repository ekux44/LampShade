package com.kuxhausen.huemore.net.hue.api;

import com.android.volley.VolleyError;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.PendingStateChange;
import com.kuxhausen.huemore.net.hue.Route;

public class StateErrorListener extends BasicErrorListener {

  HubConnection mHubConnection;
  PendingStateChange mRequest;

  public StateErrorListener(HubConnection hubConnection, PendingStateChange request, Route r) {
    super(hubConnection, r);
    mHubConnection = hubConnection;
    mRequest = request;
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    super.onErrorResponse(error);
    mHubConnection.reportStateChangeFailure(mRequest);
  }

}
