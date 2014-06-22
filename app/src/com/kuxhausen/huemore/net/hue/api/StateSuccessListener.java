package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.PendingStateChange;

public class StateSuccessListener extends BasicSuccessListener<LightsPutResponse[]> {

  HubConnection mHubConnection;
  PendingStateChange mRequest;

  public StateSuccessListener(HubConnection hubConnection, PendingStateChange request) {
    super(hubConnection);
    mHubConnection = hubConnection;
    mRequest = request;
  }

  @Override
  public void onResponse(LightsPutResponse[] response) {
    if (response.length > 0 && response[0].success != null) {
      mHubConnection.reportStateChangeSucess(mRequest);
    } else {
      mHubConnection.reportStateChangeFailure(mRequest);
    }
  }

}
