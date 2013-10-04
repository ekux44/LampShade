package com.kuxhausen.huemore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.MoodExecuterService.LocalBinder;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.network.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class NetworkManagedSherlockFragmentActivity extends
		SherlockFragmentActivity implements OnConnectionStatusChangedListener{

    
	public ServiceHolder mServiceHolder = new ServiceHolder();
    boolean mBound = false;
	
	
    @Override
	public void onConnectionStatusChanged(boolean status) {
		//override in subclass if needed
	}
	
	public RequestQueue getRequestQueue() {
		return mServiceHolder.mService.getRequestQueue();
	}

	@Override
	public void onStart() {
		super.onStart();
		// Bind to LocalService
        Intent intent = new Intent(this, MoodExecuterService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		
		// Unbind from the service
        if (mBound) {
        	mServiceHolder.mService.connectionListeners.remove(this);
            unbindService(mConnection);
            mBound = false;
        }
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mServiceHolder.mService = binder.getService();
            mBound = true;
            mServiceHolder.mService.connectionListeners.add(NetworkManagedSherlockFragmentActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    public class ServiceHolder implements ConnectionMonitor{
    	public MoodExecuterService mService;

		@Override
		public void setHubConnectionState(boolean b) {
			if(mService!=null)
				mService.setHubConnectionState(b);
		}
    }

}
