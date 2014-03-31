package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kuxhausen.huemore.MoodExecuterService.LocalBinder;
import com.kuxhausen.huemore.MoodExecuterService.OnBrightnessChangedListener;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.network.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class NetworkManagedSherlockFragmentActivity extends
		SherlockFragmentActivity implements OnConnectionStatusChangedListener, OnBrightnessChangedListener{

	private String groupName;
	private int[] groupValues;
	
	private Integer brightnessCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState); // Always call the superclass first
	    
	    if(this.getIntent()!=null && this.getIntent().getExtras()!=null){
	    	String potentialGroupName = this.getIntent().getExtras().getString(InternalArguments.GROUP_NAME);
	    	int[] potentialGroupVals = this.getIntent().getExtras().getIntArray(InternalArguments.GROUP_VALUES);
	    	
	    	if(potentialGroupVals!=null)
	    		setGroup(potentialGroupVals, potentialGroupName);
	    }
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
	    savedInstanceState.putString(InternalArguments.GROUP_NAME, groupName);
	    savedInstanceState.putIntArray(InternalArguments.GROUP_VALUES, groupValues);
	    
    	// Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    // Restore state members from saved instance
	    if(savedInstanceState.getIntArray(InternalArguments.GROUP_VALUES)!=null){
	    	setGroup(savedInstanceState.getIntArray(InternalArguments.GROUP_VALUES),savedInstanceState.getString(InternalArguments.GROUP_NAME));
	    }
	}
	
	public void setGroup(int[] bulbs, String name){
		groupValues = bulbs;
		if(mBound)
			mService.onGroupSelected(bulbs, null, name);
			
		groupName = name;
	}
	public void setBrightness(int b){
		if(mBound)
			mService.setBrightness(b);
		else
			brightnessCache = b;
	}
	
	public String getCurentGroupName(){
		if(groupName!=null)
			return groupName;
		return "";
	}
	public int[] getCurentGroupValues(){
		return groupValues;
	}
	
	
	public OnBulbListReturnedListener bulbListenerFragment;
	
	public void setBulbListenerFragment(OnBulbListReturnedListener frag){
		bulbListenerFragment = frag;
	}
	public OnBulbListReturnedListener getBulbListenerFragment(){
		return bulbListenerFragment;
	}
	
	
	private MoodExecuterService mService = new MoodExecuterService();
    private boolean mBound = false;
	private ArrayList<OnServiceConnectedListener> serviceListeners = new ArrayList<OnServiceConnectedListener>();
    
	//register for a one time on service connected message
	public void registerOnServiceConnectedListener(OnServiceConnectedListener l){
		if(mBound)
			l.onServiceConnected();
		else
			serviceListeners.add(l);
	}
	
	public MoodExecuterService getService(){
		if(mBound)
			return mService;
		else
			return null;
	}
	public boolean boundToService(){
		return mBound;
	}
	
    @Override
	public void onConnectionStatusChanged(boolean status) {
		//override in subclass if needed
	}
    
    @Override
	public void onBrightnessChanged(int brightness) {
    	//override in subclass if needed
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
        	mService.connectionListeners.remove(this);
        	mService.removeBrightnessListener(this);
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
            mService.connectionListeners.add(NetworkManagedSherlockFragmentActivity.this);
            mService.registerBrightnessListener(NetworkManagedSherlockFragmentActivity.this);
            
            for(OnServiceConnectedListener l: serviceListeners){
            	l.onServiceConnected();
            }
            serviceListeners.clear();
            
            if(groupValues!=null){
    			mService.onGroupSelected(groupValues, null, groupName);
            }
            if(brightnessCache!=null){
            	mService.setBrightness(brightnessCache);
            	brightnessCache=null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    public interface OnServiceConnectedListener{
    	public abstract void onServiceConnected();
    }
}
