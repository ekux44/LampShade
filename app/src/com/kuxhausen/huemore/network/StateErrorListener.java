package com.kuxhausen.huemore.network;

import com.android.volley.VolleyError;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.PendingStateChange;

public class StateErrorListener extends BasicErrorListener{
	
	HubConnection mHubConnection;
	PendingStateChange mRequest;
	
	public StateErrorListener(HubConnection hubConnection, PendingStateChange request){
		super(hubConnection);
		mHubConnection=hubConnection;
		mRequest=request;
	}
	
	@Override
	public void onErrorResponse(VolleyError error) {
		mHubConnection.reportStateChangeFailure(mRequest);
	}	

}
