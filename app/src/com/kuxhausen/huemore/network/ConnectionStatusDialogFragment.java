package com.kuxhausen.huemore.network;

import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity.OnServiceConnectedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class ConnectionStatusDialogFragment extends DialogFragment implements OnConnectionStatusChangedListener, OnClickListener, OnServiceConnectedListener, OnCheckedChangeListener{
	
	NetworkManagedSherlockFragmentActivity parrentActivity;
	Button leftButton, rightButton;
	TextView connectionStatusMessage;
	ProgressBar checkingConnectionInProgress;
	RadioButton localIP, internetIP;
	
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
		this.onConnectionStatusChanged();
		
		parrentActivity.registerOnServiceConnectedListener(this);
		
/*		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(parrentActivity);
		String localBridge = settings.getString(PreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS, null);
		String internetBridge = settings.getString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null);
		
		RadioGroup rb = (RadioGroup)myView.findViewById(R.id.ipSelectionGroup);
		if(localBridge!=null && internetBridge!=null){
			localIP = (RadioButton)myView.findViewById(R.id.localIP);
			localIP.setText(localBridge);
			internetIP = (RadioButton)myView.findViewById(R.id.internetIP);			
			internetIP.setText(internetBridge);
			
			String currentBridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS, null);
			if(currentBridge.equals(internetBridge))
				rb.check(R.id.internetIP);
			rb.setOnCheckedChangeListener(this);
		}else{
			rb.setVisibility(View.GONE);
		}
*/		
		return myView;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		parrentActivity = (NetworkManagedSherlockFragmentActivity)activity;
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
		if(parrentActivity.boundToService())
			parrentActivity.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
	}

	@Override
	public void onConnectionStatusChanged() {
/*		connectionStatus = status;
		if(status){
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
//			String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,"");
			
//			connectionStatusMessage.setText(this.getString(R.string.hub_connection)+": "+bridge);
			leftButton.setText(R.string.reset_hub_connection);
			rightButton.setText(R.string.accept);
			checkingConnectionInProgress.setVisibility(View.GONE);
			
		}else{
			connectionStatusMessage.setText(R.string.hub_connection_error);
			leftButton.setText(R.string.cancel);
			rightButton.setText(R.string.reset_hub_connection);
			checkingConnectionInProgress.setVisibility(View.VISIBLE);
		}
*/		
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
		this.onConnectionStatusChanged();
		parrentActivity.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
		Editor edit = settings.edit();
		switch(checkedId){
/*		case R.id.internetIP:
			edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, settings.getString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null));
			edit.commit();
			this.dismiss();
			break;
		case R.id.localIP:
			edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, settings.getString(PreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS, null));
			edit.commit();
			this.dismiss();
			break;
*/		}
	}
}
