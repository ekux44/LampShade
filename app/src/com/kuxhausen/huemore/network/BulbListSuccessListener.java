package com.kuxhausen.huemore.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.api.Bulb;
import com.kuxhausen.huemore.state.api.BulbList;

public class BulbListSuccessListener extends BasicSuccessListener<BulbList> {

	public interface OnBulbListReturnedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onListReturned(Bulb[] result);
	}

	
	private final OnBulbListReturnedListener listener;
	private final Context context;
	
	public BulbListSuccessListener(ConnectionMonitor parrentA, OnBulbListReturnedListener l, Context c) {
		super(parrentA);
		listener = l;
		context = c;
	}

	@Override
	public void onResponse(BulbList response) {
		super.onResponse(response);
		
		if(context!=null && response!=null && response.getList().length>0){
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			Editor edit = settings.edit();
			edit.putInt(PreferenceKeys.NUMBER_OF_CONNECTED_BULBS, response.getList().length);
			edit.commit();
		}
		
		if(listener!=null)
			listener.onListReturned(response.getList());
	}	
}
