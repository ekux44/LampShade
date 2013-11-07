package com.kuxhausen.huemore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class SettingsDialogFragment extends DialogFragment implements
		OnCheckedChangeListener, android.widget.CompoundButton.OnCheckedChangeListener {

	SharedPreferences settings;
	NetworkManagedSherlockFragmentActivity ma;
	
	EditText internetIP;
	TextView portForwardingInstructions;
	CheckBox enablePortFowarding;
	
	private static final String PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.settings, container, false);
		ma = (NetworkManagedSherlockFragmentActivity) this.getActivity();
		this.getDialog().setTitle(R.string.action_settings);

		settings = PreferenceManager.getDefaultSharedPreferences(ma);

		RadioGroup firstViewRadioGroup = (RadioGroup) myView
				.findViewById(R.id.firstViewSettingsGroup);
		firstViewRadioGroup.setOnCheckedChangeListener(this);
		RadioGroup secondViewRadioGroup = (RadioGroup) myView
				.findViewById(R.id.secondViewSettingGroup);
		secondViewRadioGroup.setOnCheckedChangeListener(this);

		if (settings.getBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false))
			firstViewRadioGroup.check(R.id.groupsViewRadioButton);
		else
			firstViewRadioGroup.check(R.id.bulbsViewRadioButton);
		if (settings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true))
			secondViewRadioGroup.check(R.id.moodsViewRadioButton);
		else
			secondViewRadioGroup.check(R.id.manualViewRadioButton);

		enablePortFowarding = (CheckBox)myView.findViewById(R.id.portForwardingCheckBox);
		enablePortFowarding.setOnCheckedChangeListener(this);
		
		internetIP = (EditText)myView.findViewById(R.id.portForwardingEditText);
		portForwardingInstructions = (TextView)myView.findViewById(R.id.portForwardingTextView);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ma);
		String internetBridge = settings.getString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null);
		if(internetBridge!=null){
			internetIP.setText(internetBridge);
			enablePortFowarding.setChecked(true);
		}else{
			enablePortFowarding.setChecked(false);
			internetIP.setVisibility(View.GONE);
			portForwardingInstructions.setVisibility(View.GONE);
		}
		
		return myView;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		Editor edit = settings.edit();
		switch (checkedId) {
		case R.id.groupsViewRadioButton:
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, true);
			edit.commit();
			break;
		case R.id.bulbsViewRadioButton:
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false);
			edit.commit();
			break;
		case R.id.moodsViewRadioButton:
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
			edit.commit();
			break;
		case R.id.manualViewRadioButton:
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, false);
			edit.commit();
			break;

		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			internetIP.setVisibility(View.VISIBLE);
			portForwardingInstructions.setVisibility(View.VISIBLE);
		}else{
			internetIP.setVisibility(View.GONE);
			portForwardingInstructions.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onStop(){
		super.onStop();
		if(enablePortFowarding.isChecked()){
			String candidateRemoteIP = internetIP.getText().toString();
			//validate IP
			Pattern pattern = Pattern.compile(PATTERN);
		    Matcher matcher = pattern.matcher(candidateRemoteIP);
			if(matcher.matches()){
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ma);
				Editor edit = settings.edit();
				edit.putString(PreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, candidateRemoteIP);
				edit.putString(PreferenceKeys.BRIDGE_IP_ADDRESS, candidateRemoteIP);
				edit.commit();
			}
		}
	}
}
