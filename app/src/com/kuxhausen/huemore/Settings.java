package com.kuxhausen.huemore;

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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

public class Settings extends DialogFragment implements OnClickListener,
		OnCheckedChangeListener {

	SharedPreferences settings;
	MainActivity ma;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.settings, container, false);
		ma = (MainActivity) this.getActivity();
		this.getDialog().setTitle("Settings");

		Button rateButton = (Button) myView.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(this);

		settings = PreferenceManager.getDefaultSharedPreferences(ma);

		RadioGroup firstViewRadioGroup = (RadioGroup) myView
				.findViewById(R.id.firstViewSettingsGroup);
		firstViewRadioGroup.setOnCheckedChangeListener(this);
		RadioGroup secondViewRadioGroup = (RadioGroup) myView
				.findViewById(R.id.secondViewSettingGroup);
		secondViewRadioGroup.setOnCheckedChangeListener(this);

		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false))
			firstViewRadioGroup.check(R.id.groupsViewRadioButton);
		else
			firstViewRadioGroup.check(R.id.bulbsViewRadioButton);
		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true))
			secondViewRadioGroup.check(R.id.moodsViewRadioButton);
		else
			secondViewRadioGroup.check(R.id.manualViewRadioButton);

		return myView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rateButton:
			ma.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=" + "com.kuxhausen.huemore")));
			break;
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		Editor edit = settings.edit();
		switch (checkedId) {
		case R.id.groupsViewRadioButton:
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, true);
			edit.commit();
			break;
		case R.id.bulbsViewRadioButton:
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false);
			edit.commit();
			break;
		case R.id.moodsViewRadioButton:
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true);
			edit.commit();
			break;
		case R.id.manualViewRadioButton:
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_MOODS, false);
			edit.commit();
			break;

		}

	}
}
