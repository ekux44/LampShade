package com.kuxhausen.huemore;

import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.database.DatabaseHelper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
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
		

		Button donateButton = (Button) myView.findViewById(R.id.donateButton);
		donateButton.setOnClickListener(this);

		settings = PreferenceManager.getDefaultSharedPreferences(ma);

		RadioGroup firstViewRadioGroup = (RadioGroup) myView.findViewById(R.id.firstViewSettingsGroup);
		firstViewRadioGroup.setOnCheckedChangeListener(this);
		RadioGroup secondViewRadioGroup = (RadioGroup) myView.findViewById(R.id.secondViewSettingGroup);
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
		case R.id.donateButton:
			ma.mPlayHelper.launchPurchaseFlow(ma,
					PlayItems.BUY_ME_A_BULB_DONATION_1, 10010,
					mPurchaseFinishedListener, "");
			break;
		}

	}

	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			// unlockfreestuff
			// mark in preferences

			if (result.isFailure()) {
				// handle error
				return;
			} else {
				int numUnlocked = 50;
				int previousMax = settings.getInt(
						PreferencesKeys.BULBS_UNLOCKED,
						PreferencesKeys.ALWAYS_FREE_BULBS);
				if (numUnlocked > previousMax) {
					// Update the number held in settings
					Editor edit = settings.edit();
					edit.putInt(PreferencesKeys.BULBS_UNLOCKED, numUnlocked);
					edit.commit();

					ma.databaseHelper.addBulbs(previousMax, numUnlocked);
				}
			}
		}
	};

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
