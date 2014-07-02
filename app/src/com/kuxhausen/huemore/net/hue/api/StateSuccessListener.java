package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.PendingStateChange;
import com.kuxhausen.huemore.net.hue.Route;

public class StateSuccessListener extends BasicSuccessListener<LightsPutResponse[]> {

  HubConnection mHubConnection;
  PendingStateChange mRequest;

  public StateSuccessListener(HubConnection hubConnection, PendingStateChange request, Route r) {
    super(hubConnection, r);
    mHubConnection = hubConnection;
    mRequest = request;
  }

  @Override
  public void onResponse(LightsPutResponse[] response) {
    super.onResponse(response);
    if (response.length > 0 && response[0].success != null) {
      mHubConnection.reportStateChangeSucess(mRequest);
    } else {
      mHubConnection.reportStateChangeFailure(mRequest);
    }
  }

}
