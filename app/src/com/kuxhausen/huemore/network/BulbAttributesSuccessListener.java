package com.kuxhausen.huemore.network;

import com.kuxhausen.huemore.state.api.BulbAttributes;

public class BulbAttributesSuccessListener extends BasicSuccessListener<BulbAttributes> {

	private final int bulbNum;
	
	public interface OnBulbAttributesReturnedListener {
		public void onAttributesReturned(BulbAttributes result, int bulbNumber);
	}

	
	private final OnBulbAttributesReturnedListener listener;
	
	public BulbAttributesSuccessListener(ConnectionMonitor parrentA, OnBulbAttributesReturnedListener l, int bNum) {
		super(parrentA);
		listener = l;
		bulbNum = bNum;
	}

	@Override
	public void onResponse(BulbAttributes response) {
		super.onResponse(response);
				
		if(listener!=null)
			listener.onAttributesReturned(response, bulbNum);
	}	
}
