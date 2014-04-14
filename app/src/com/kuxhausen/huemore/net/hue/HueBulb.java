package com.kuxhausen.huemore.net.hue;

import android.content.Context;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class HueBulb implements NetworkBulb {

	private Long mBaseId;
	private String mName;
	/** for now this is just bulbNumber**/
	private String mDeviceId;
	private HueBulbData mData;
	private Context mContext;
	private HubConnection mConnection;
	
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public BulbState getState() {
		// TODO Auto-generated method stub
		return null;
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
	public String getUniqueId() {
		return mDeviceId;
	}

	@Override
	public ConnectivityState getConnectivityState() {
		// TODO Auto-generated method stub
		return ConnectivityState.Unknown;
	}

}
