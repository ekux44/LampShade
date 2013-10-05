package com.kuxhausen.huemore.network;

import com.kuxhausen.huemore.state.api.Bulb;
import com.kuxhausen.huemore.state.api.BulbList;

public class BulbListSuccessListener extends BasicSuccessListener<BulbList> {

	public interface OnBulbListReturnedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onListReturned(Bulb[] result);
	}

	
	private final OnBulbListReturnedListener listener;
	
	public BulbListSuccessListener(ConnectionMonitor parrentA, OnBulbListReturnedListener l) {
		super(parrentA);
		listener = l;
	}

	@Override
	public void onResponse(BulbList response) {
		super.onResponse(response);
		if(listener!=null)
			listener.onListReturned(response.getList());
	}	
}
