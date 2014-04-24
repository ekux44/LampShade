package com.kuxhausen.huemore.net.hue;

import java.util.ArrayList;
import java.util.PriorityQueue;

import android.content.Context;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.state.BulbState;

public class HueBulb implements NetworkBulb {

	private Long mBaseId;
	private String mName;
	/** for now this is just bulbNumber**/
	private String mDeviceId;
	private HueBulbData mData;
	private Context mContext;
	private HubConnection mConnection;
	
	public BulbState desiredState = new BulbState();
	public ArrayList<PendingStateChange> ongoing = new ArrayList<PendingStateChange>();
	public BulbState confirmed = new BulbState();
	
	//TODO chance once a better Device Id implemented
	public String getHubBulbNumber(){
		return mDeviceId;
	}
	
	public HueBulb(Context c, Long bulbBaseId, String bulbName, String bulbDeviceId, HueBulbData bulbData, HubConnection hubConnection) {
		mContext = c;
		mBaseId = bulbBaseId;
		mName = bulbName;
		mDeviceId = bulbDeviceId;
		mData = bulbData;
		mConnection = hubConnection;
	}

	@Override
	public void setState(BulbState bs) {
		desiredState = bs;
		mConnection.getChangedQueue().add(this);
	}

	@Override
	public BulbState getState() {
		BulbState bs = new BulbState();
		// TODO Auto-generated method stub
		return bs;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void rename(String name) {
		// TODO Auto-generated method stub
		BulbAttributes bAttrs = new BulbAttributes();
		bAttrs.name = name;
		
		NetworkMethods.PreformSetBulbAttributes(mContext, mConnection.getRequestQueue(), mConnection, Integer.parseInt(mDeviceId), bAttrs);
	}

	@Override
	public Long getBaseId() {
		return mBaseId;
	}

	@Override
	public ConnectivityState getConnectivityState() {
		return mConnection.getConnectivityState();
	}
}
