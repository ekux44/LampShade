package com.kuxhausen.huemore;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkManagedSherlockFragmentActivity extends
		SherlockFragmentActivity {

	private RequestQueue volleyRQ;

	public RequestQueue getRequestQueue() {
		return volleyRQ;
	}

	@Override
	public void onStart() {
		super.onResume();

		volleyRQ = Volley.newRequestQueue(this);
	}

	@Override
	public void onStop() {
		super.onPause();

		volleyRQ.cancelAll(this);
	}
}
