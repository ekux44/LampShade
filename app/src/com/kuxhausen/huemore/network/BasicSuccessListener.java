package com.kuxhausen.huemore.network;

import com.android.volley.Response.Listener;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;

public class BasicSuccessListener<T> implements Listener<T>{
	
	NetworkManagedSherlockFragmentActivity parrent;

	public BasicSuccessListener(NetworkManagedSherlockFragmentActivity parrentA){
		parrent = parrentA;
	}

	@Override
	public void onResponse(T response) {
		if(parrent!=null)
			parrent.setHubConnectionState(true);
	}	

}
