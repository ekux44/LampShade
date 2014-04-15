package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.api.BulbState;

public interface NetworkBulb {
	
	public enum ConnectivityState {
		Unknown, Unreachable, Connected
	}
	
	interface ConnectionListener{
		public abstract void onConnectivityChanged(ConnectivityState netState);
	}
	
	interface StateListener{
		public abstract void onStateChanged(BulbState bulbState);
	}
	
	public abstract ConnectivityState getConnectivityState();
	
	public abstract void setState(BulbState bs);
	
	public abstract BulbState getState();
	
	public abstract String getName();
	
	public abstract void rename(String name);
	
	public abstract Long getBaseId();
	
}
