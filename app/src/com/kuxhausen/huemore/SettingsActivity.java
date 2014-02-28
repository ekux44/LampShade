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

	SharedPreferences settings;
	EditText internetIP, internetPort;
	View portForwardingOptions;
	CheckBox enablePortFowarding;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.getSupportActionBar().setTitle(R.string.action_settings);

		Button rateButton = (Button) this.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(this);

		settings = PreferenceManager.getDefaultSharedPreferences(this);
        
		enablePortFowarding = (CheckBox)this.findViewById(R.id.portForwardingCheckBox);
		enablePortFowarding.setOnCheckedChangeListener(this);
		
		internetIP = (EditText)this.findViewById(R.id.portForwardingEditText);
		internetPort = (EditText)this.findViewById(R.id.portNumberEditText);
		portForwardingOptions = this.findViewById(R.id.portForwardingOptions);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String internetBridge = settings.getString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null);
		if(internetBridge!=null){
			
			String[] sRay = internetBridge.split(":",2);
			internetIP.setText(sRay[0]);
			if(sRay.length>1)
				internetPort.setText(sRay[1]);
			enablePortFowarding.setChecked(true);
		}else{
			enablePortFowarding.setChecked(false);
			portForwardingOptions.setVisibility(View.GONE);
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
		if(isChecked){
			portForwardingOptions.setVisibility(View.VISIBLE);
		}else{
			portForwardingOptions.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onStop(){
		super.onStop();
		if(enablePortFowarding.isChecked()){
			String candidateRemoteIP = internetIP.getText().toString()+":"+internetPort.getText().toString();
			if(candidateRemoteIP.length()>1){
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				Editor edit = settings.edit();
				edit.putString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, candidateRemoteIP);
				edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, candidateRemoteIP);
				edit.commit();
			}
		}
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
