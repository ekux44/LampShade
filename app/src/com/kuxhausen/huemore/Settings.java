package com.kuxhausen.huemore;

import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class Settings extends Activity implements OnClickListener, OnCheckedChangeListener {
	
	IabHelper mPlayHelper;
	SharedPreferences settings;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			initializeActionBar(false);
		}
		
		Button donateButton = (Button) findViewById(R.id.donateButton);
		donateButton.setOnClickListener(this);
		
		settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		RadioGroup firstViewRadioGroup = (RadioGroup) findViewById(R.id.firstViewSettingsGroup);
		firstViewRadioGroup.setOnCheckedChangeListener(this);
		RadioGroup secondViewRadioGroup = (RadioGroup) findViewById(R.id.secondViewSettingGroup);
		secondViewRadioGroup.setOnCheckedChangeListener(this);
		
		if(settings.getBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false))
			firstViewRadioGroup.check(R.id.groupsViewRadioButton);
		else
			firstViewRadioGroup.check(R.id.bulbsViewRadioButton);
		if(settings.getBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true))
			secondViewRadioGroup.check(R.id.moodsViewRadioButton);
		else
			secondViewRadioGroup.check(R.id.manualViewRadioButton);	
		
		
		
		String firstChunk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPUhHgGEdnpyPMAWgP3Xw/jHkReU1O0n6d4rtcULxOrVl/hcZlOsVyByMIZY5wMD84gmMXjbz8pFb4RymFTP7Yp8LSEGiw6DOXc7ydNd0lbZ4WtKyDEwwaio1wRbRPxdU7/4JBpMCh9L6geYx6nYLt0ExZEFxULV3dZJpIlEkEYaNGk/64gc0l34yybccYfORrWzu8u+";
		String secondChunk = "5YxJ5k1ikIJJ2I7/2Rp5AXkj2dWybmT+AGx83zh8+iMGGawEQerGtso9NUqpyZWU08EO9DcF8r2KnFwjmyWvqJ2JzbqCMNt0A08IGQNOrd16/C/65GE6J/EtsggkNIgQti6jD7zd3b2NAQIDAQAB";
		String base64EncodedPublicKey = firstChunk + secondChunk;
		// compute your public key and store it in base64EncodedPublicKey
		mPlayHelper = new IabHelper(this, base64EncodedPublicKey);
		mPlayHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					//Log.d("asdf", "Problem setting up In-app Billing: "+ result);
				} else {
					// Hooray, IAB is fully set up!
					
				}
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void initializeActionBar(Boolean value){
		try {this.getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e){}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.donateButton:
			mPlayHelper.launchPurchaseFlow(this,
					PlayItems.BUY_ME_A_BULB_DONATION_1, 10010,
					mPurchaseFinishedListener, "");
		break;
		}
		
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			//unlockfreestuff
			//mark in preferences

		}
	};

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		Editor edit = settings.edit();
		switch(checkedId){
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
