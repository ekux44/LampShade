package com.kuxhausen.huemore.net;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.network.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.api.BulbState;

import android.content.Context;
import android.util.Pair;

public class DeviceManager implements ConnectionMonitor{
	
	private ArrayList<Connection> mConnections;
	private Context mContext;
	private Group selectedGroup;
	private String selectedGroupName;
	private ArrayList<OnConnectionStatusChangedListener> connectionListeners = new ArrayList<OnConnectionStatusChangedListener>();
	public ArrayList<OnBrightnessChangedListener> brightnessListeners = new ArrayList<OnBrightnessChangedListener>();
	
	
	public DeviceManager(Context c){
		mContext = c;
		
		//load all connections from the database 
		mConnections = new ArrayList<Connection>();
		mConnections.addAll(HubConnection.loadHubConnections(c, this));
		
		//do something with bulbs
		
		
	}
	
	public void onDestroy() {
		for(Connection c : mConnections)
			c.onDestroy();
	}
	
	public Group getSelectedGroup(){
		return selectedGroup;
	}
	public String getSelectedGroupName(){
		return selectedGroupName;
	}
	
	public void onGroupSelected(Group group, Integer optionalBri, String groupName){
		selectedGroup = group;
		selectedGroupName = groupName;
		
		((HubConnection)mConnections.get(0)).onGroupSelected(selectedGroup,optionalBri,groupName);
	}
	
	public void addOnConnectionStatusChangedListener(OnConnectionStatusChangedListener l){
		connectionListeners.add(l);
	}
	
	public void removeOnConnectionStatusChangedListener(OnConnectionStatusChangedListener l){
		connectionListeners.remove(l);
	}
	
	@Override
	public void setHubConnectionState(boolean connected){
		if(hasHubConnection!=connected){
			hasHubConnection = connected;
			for(OnConnectionStatusChangedListener l : connectionListeners)
				l.onConnectionStatusChanged(connected);	
		}
		if(!connected){
			//TODO rate limit
			NetworkMethods.PreformGetBulbList(mContext, getRequestQueue(), this, null);
		}
	}
	public boolean hasHubConnection(){
		return hasHubConnection;
	}

	public interface OnBrightnessChangedListener {
		public void onBrightnessChanged(int brightness);
	}
	
	/** announce brightness to any listeners **/
	public void onBrightnessChanged(){
		for(OnBrightnessChangedListener l : brightnessListeners){
			l.onBrightnessChanged(getMaxBrightness());
		}
	}
	public void registerBrightnessListener(OnBrightnessChangedListener l){
		if(getMaxBrightness()!=null)
			l.onBrightnessChanged(getMaxBrightness());
		brightnessListeners.add(l);
	}
	
	public void removeBrightnessListener(OnBrightnessChangedListener l){
		brightnessListeners.remove(l);
	}
	
	
	//TODO clean up/remove everything below this line
	
	private boolean hasHubConnection = false;
	
	
	public void transmit(int bulb, BulbState bs){
		((HubConnection)mConnections.get(0)).queue.add(new Pair<Integer,BulbState>(bulb,bs));
	}
	
	public RequestQueue getRequestQueue() {
		return ((HubConnection)mConnections.get(0)).getRequestQueue();
	}

	public Integer getMaxBrightness(){
		return ((HubConnection)mConnections.get(0)).getMaxBrightness();
	}
	
	/** doesn't notify listeners **/
	public synchronized void setBrightness(int brightness){
		((HubConnection)mConnections.get(0)).setBrightness(brightness, selectedGroup);
	}

}
