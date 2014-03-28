package com.kuxhausen.huemore.net;

import java.util.ArrayList;

import com.kuxhausen.huemore.net.hue.HubConnection;

import android.content.Context;

public class DeviceManager {
	
	ArrayList<Connection> mConnections;
	
	public DeviceManager(Context c){
		//load all connections from the database 
		mConnections = new ArrayList<Connection>();
		mConnections.addAll(HubConnection.loadHubConnections(c));
		
		//do something with bulbs
	}
	
}
