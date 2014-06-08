package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class SettingsFragment extends Fragment implements OnClickListener {

	SharedPreferences mSettings;
	private CheckBox mEnableNfcReadPage;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View myView = inflater.inflate(R.layout.settings, container, false);
		
		this.getActivity().setTitle(R.string.action_settings);
		
		Button rateButton = (Button) myView.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(this);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        
		mEnableNfcReadPage = (CheckBox)myView.findViewById(R.id.showNfcReadPageCheckBox);
		if(mSettings.getBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, true)){
			mEnableNfcReadPage.setChecked(true);
		}
		return myView;
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
	public void onStop(){
		super.onStop();
		
		Editor edit = mSettings.edit();
		edit.putBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, mEnableNfcReadPage.isChecked());
		edit.commit();
	}
}
