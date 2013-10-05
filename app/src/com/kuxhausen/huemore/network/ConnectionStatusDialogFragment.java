package com.kuxhausen.huemore.network;

import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity.OnServiceConnectedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConnectionStatusDialogFragment extends DialogFragment implements OnConnectionStatusChangedListener, OnClickListener, OnServiceConnectedListener{
	
	GodObject parrentActivity;
	Button leftButton, rightButton;
	TextView connectionStatusMessage;
	ProgressBar checkingConnectionInProgress;
	
	private boolean connectionStatus;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.connection_status_dialog_fragment, container, false);
		
		this.getDialog().setTitle(R.string.action_hub_status);
		
		leftButton = (Button) myView.findViewById(R.id.cancel);
		leftButton.setOnClickListener(this);
		rightButton = (Button) myView.findViewById(R.id.okay);
		rightButton.setOnClickListener(this);
		
		connectionStatusMessage = (TextView)myView.findViewById(R.id.connectionStatusTextView);
		
		checkingConnectionInProgress = (ProgressBar)myView.findViewById(R.id.checkingConnectionProgressBar);
		
		//just so the UI is filled
		this.onConnectionStatusChanged(false);
		
		parrentActivity.registerOnServiceConnectedListener(this);
		
		return myView;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		parrentActivity = (GodObject)activity;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		if(parrentActivity.boundToService())
			parrentActivity.getService().connectionListeners.remove(this);
	}

	@Override
	public void onConnectionStatusChanged(boolean status) {
		connectionStatus = status;
		if(status){
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
			String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,"");
			
			connectionStatusMessage.setText(this.getString(R.string.hub_connection)+": "+bridge);
			leftButton.setText(R.string.reset_hub_connection);
			rightButton.setText(R.string.accept);
			checkingConnectionInProgress.setVisibility(View.GONE);
			
		}else{
			connectionStatusMessage.setText(R.string.hub_connection_error);
			leftButton.setText(R.string.cancel);
			rightButton.setText(R.string.reset_hub_connection);
			checkingConnectionInProgress.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.okay: 
			if(connectionStatus){
				this.dismiss();
			}
			else{
				DiscoverHubDialogFragment dhdf1 = new DiscoverHubDialogFragment();
				dhdf1.show(parrentActivity.getSupportFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				this.dismiss();
			}
			break;
		case R.id.cancel:
			if(connectionStatus){
				DiscoverHubDialogFragment dhdf1 = new DiscoverHubDialogFragment();
				dhdf1.show(parrentActivity.getSupportFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				this.dismiss();
			}
			else{
				this.dismiss();
			}
			break;
		}
	}

	@Override
	public void onServiceConnected() {
		this.onConnectionStatusChanged(parrentActivity.getService().hasHubConnection());
		parrentActivity.getService().connectionListeners.add(this);
	}
}
