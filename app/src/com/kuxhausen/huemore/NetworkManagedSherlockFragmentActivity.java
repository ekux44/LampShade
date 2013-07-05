package com.kuxhausen.huemore;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class NetworkManagedSherlockFragmentActivity extends
		SherlockFragmentActivity {

	private RequestQueue volleyRQ;

	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}

	@Override
	public void onStart() {
		super.onStart();
		volleyRQ = Volley.newRequestQueue(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
	}
	
	@Override
	public void onDestroy() {
		volleyRQ.cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		volleyRQ.cancelAll(InternalArguments.PERMANENT_NETWORK_REQUEST);
		super.onDestroy();
	}
}
