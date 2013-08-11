package com.kuxhausen.huemore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.MoodExecuterService.LocalBinder;
import com.kuxhausen.huemore.network.ConnectionMonitor;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class NetworkManagedSherlockFragmentActivity extends
		SherlockFragmentActivity implements ConnectionMonitor{

    public MoodExecuterService mService;
    boolean mBound = false;
	
	private boolean hasHubConnection = false;
	
	@Override
	public void setHubConnectionState(boolean connected){
		if(hasHubConnection!=connected){
			hasHubConnection = connected;
			onConnectionStatusChanged();
		}
		//Log.e("setHubConnection", ""+connected);
	}
	public boolean hasHubConnection(){
		return hasHubConnection;
	}
	
	public void onConnectionStatusChanged(){
		//Override in subclasses that want to listen to this
	}
	
	public RequestQueue getRequestQueue() {
		return mService.getRequestQueue();
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
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
