package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class SettingsActivity extends SherlockActivity implements
		OnClickListener, android.widget.CompoundButton.OnCheckedChangeListener {

	SharedPreferences mSettings;
	EditText mInternetIP, mInternetPort;
	View mPortForwardingOptions;
	private CheckBox mEnablePortFowarding, mEnableNfcReadPage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.getSupportActionBar().setTitle(R.string.action_settings);

		Button rateButton = (Button) this.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(this);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        
		mEnableNfcReadPage = (CheckBox)this.findViewById(R.id.showNfcReadPageCheckBox);
		if(mSettings.getBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, true)){
			mEnableNfcReadPage.setChecked(true);
		}
		
		mEnablePortFowarding = (CheckBox)this.findViewById(R.id.portForwardingCheckBox);
		mEnablePortFowarding.setOnCheckedChangeListener(this);
		
		mInternetIP = (EditText)this.findViewById(R.id.portForwardingEditText);
		mInternetPort = (EditText)this.findViewById(R.id.portNumberEditText);
		mPortForwardingOptions = this.findViewById(R.id.portForwardingOptions);
		
		String internetBridge = mSettings.getString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null);
		if(internetBridge!=null){
			
			String[] sRay = internetBridge.split(":",2);
			mInternetIP.setText(sRay[0]);
			if(sRay.length>1)
				mInternetPort.setText(sRay[1]);
			mEnablePortFowarding.setChecked(true);
		}else{
			mEnablePortFowarding.setChecked(false);
			mPortForwardingOptions.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rateButton:
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=" + "com.kuxhausen.huemore")));
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
			case R.id.portForwardingCheckBox:
				if(isChecked){
					mPortForwardingOptions.setVisibility(View.VISIBLE);
				}else{
					mPortForwardingOptions.setVisibility(View.GONE);
				}
		}
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = settings.edit();
		
		if(mEnablePortFowarding.isChecked()){
			String candidateRemoteIP = mInternetIP.getText().toString()+":"+mInternetPort.getText().toString();
			if(candidateRemoteIP.length()>1){
				edit.putString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, candidateRemoteIP);
				edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, candidateRemoteIP);
			}
		} else{
			if(settings.contains(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS)){
				edit.remove(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS);
				edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, settings.getString(PreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS, ""));
			}
		}
		edit.putBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, mEnableNfcReadPage.isChecked());
		edit.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this,MainActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
