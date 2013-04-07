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
import android.widget.TextView;

public class Unlocks extends DialogFragment implements OnClickListener {

	SharedPreferences settings;
	MainActivity ma;
	TextView bulbsUnlockedText;
	Button fiveMoreButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.unlocks, container, false);
		ma = (MainActivity) this.getActivity();
		this.getDialog().setTitle("Unlocks");
		
		settings = PreferenceManager.getDefaultSharedPreferences(ma);
		
		Button donateButton = (Button) myView.findViewById(R.id.donateButton);
		donateButton.setOnClickListener(this);
		
		fiveMoreButton = (Button) myView.findViewById(R.id.fiveMoreButton);
		fiveMoreButton.setOnClickListener(this);
		
		bulbsUnlockedText= (TextView)myView.findViewById(R.id.bulbsUnlockedText);
		updateBulbCount();
		
		return myView;
	}

	public void updateBulbCount(){
		int max = settings.getInt(
				PreferencesKeys.BULBS_UNLOCKED,
				PreferencesKeys.ALWAYS_FREE_BULBS);
		bulbsUnlockedText.setText(ma.getResources().getString(R.string.bulbs_unlocked_desciptor)+max+"/50");
		if(max>=50)
			fiveMoreButton.setVisibility(View.INVISIBLE);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.donateButton:
			ma.mPlayHelper.launchPurchaseFlow(ma,
					PlayItems.BUY_ME_A_BULB_DONATION_1, 10010,
					mDonatePurchaseFinishedListener, "");
			break;
		case R.id.fiveMoreButton:
			if (ma.lastQuerriedInventory == null)
				ma.mPlayHelper.queryInventoryAsync(ma.mGotInventoryListener);
			else {
				if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_1))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_1, 10001,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_2))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_2, 10002,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_3))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_3, 10003,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_4))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_4, 10004,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_5))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_5, 10005,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_6))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_6, 10006,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_7))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_7, 10007,
							mPurchaseFinishedListener, "");
				else if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_8))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_8, 10008,
							mPurchaseFinishedListener, "");
			}
			break;
		}
		
	}

	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			ma.mPlayHelper.queryInventoryAsync(ma.mGotInventoryListener);
			updateBulbCount();//TODO may not work
		}
	};
	
	IabHelper.OnIabPurchaseFinishedListener mDonatePurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
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
			updateBulbCount();
		}
	};

}
