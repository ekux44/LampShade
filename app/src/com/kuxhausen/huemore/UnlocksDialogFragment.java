package com.kuxhausen.huemore;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

public class UnlocksDialogFragment extends DialogFragment implements
		OnClickListener {

	SharedPreferences settings;
	MainActivity ma;
	Button fiveMoreButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.unlocks, container, false);
		ma = (MainActivity) this.getActivity();
		this.getDialog().setTitle(R.string.action_unlocks);

		settings = PreferenceManager.getDefaultSharedPreferences(ma);

		fiveMoreButton = (Button) myView.findViewById(R.id.fiveMoreButton);
		fiveMoreButton.setOnClickListener(this);

		return myView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fiveMoreButton:
			if (ma.lastQuerriedInventory == null)
				ma.mPlayHelper.queryInventoryAsync(ma.mGotInventoryListener);
			else {
				if (!ma.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_1))
					ma.mPlayHelper.launchPurchaseFlow(this.ma,
							PlayItems.FIVE_BULB_UNLOCK_1, 10009,
							mPurchaseFinishedListener, "");
				else
					Toast.makeText(ma,
							ma.getString(R.string.unlock_pro_already),
							Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			ma.mPlayHelper.queryInventoryAsync(ma.mGotInventoryListener);
		}
	};

	IabHelper.OnIabPurchaseFinishedListener mDonatePurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

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

}
