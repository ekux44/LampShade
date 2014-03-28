package com.kuxhausen.huemore.net.hue;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;

public class HubConnection implements Connection{

	private static final String[] columns = {BaseColumns._ID, NetConnectionColumns.TYPE_COLUMN, NetConnectionColumns.NAME_COLUMN, NetConnectionColumns.DEVICE_ID_COLUMN, NetConnectionColumns.JSON_COLUMN};
	private static final Integer type = NetBulbColumns.NetBulbType.PHILIPS_HUE;
	private static final Gson gson = new Gson();
	
	private Integer mBaseId;
	private String mName, mDeviceId;
	private HubData mData;
	
	public HubConnection(Integer baseId, String name, String deviceId, HubData data){
		mBaseId = baseId;
		mName = name;
		mDeviceId = deviceId;
		mData = data;
	}
	
	public void saveConnection(){
		// TODO Auto-generated method stub
	}	
	
	@Override
	public void initializeConnection(Context c) {
		// TODO Auto-generated method stub
	}
	
	public static ArrayList<HubConnection> loadHubConnections(Context c){
		ArrayList<HubConnection> hubs = new ArrayList<HubConnection>();
		
		
		String[] selectionArgs = {""+NetBulbColumns.NetBulbType.PHILIPS_HUE};
		Cursor cursor = c.getContentResolver().query(NetConnectionColumns.URI, columns, NetConnectionColumns.TYPE_COLUMN + " = ?", selectionArgs, null);
		cursor.moveToPosition(-1);// not the same as move to first!
		while (cursor.moveToNext()) {
			Integer baseId = cursor.getInt(0);
			String name = cursor.getString(2);
			String deviceId = cursor.getString(3);
			HubData data = gson.fromJson(cursor.getString(4), HubData.class);
			hubs.add(new HubConnection(baseId,name,deviceId,data));
		}
		
		//initialize all connections
		for(HubConnection h : hubs)
			h.initializeConnection(c);
		
		return null;
	}
}
