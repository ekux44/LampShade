package com.kuxhausen.huemore.billing;

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

import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.string;
import com.kuxhausen.huemore.billing.googleplay.IabHelper;
import com.kuxhausen.huemore.billing.googleplay.IabResult;
import com.kuxhausen.huemore.billing.googleplay.Purchase;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class UnlocksDialogFragment extends DialogFragment implements
		OnClickListener {

	SharedPreferences settings;
	NavigationDrawerActivity ma;
	Button fiveMoreButton;
	BillingManager bm;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.unlocks_fragment, container, false);
		ma = (NavigationDrawerActivity) this.getActivity();
		bm = ma.getBillingManager();
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
			if (bm.lastQuerriedInventory == null)
				bm.mPlayHelper.queryInventoryAsync(bm.mGotInventoryListener);
			else {
				if (!bm.lastQuerriedInventory
						.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_1))
					bm.mPlayHelper.launchPurchaseFlow(this.ma,
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
			bm.mPlayHelper.queryInventoryAsync(bm.mGotInventoryListener);
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
						PreferenceKeys.BULBS_UNLOCKED,
						PreferenceKeys.ALWAYS_FREE_BULBS);
				if (numUnlocked > previousMax) {
					// Update the number held in settings
					Editor edit = settings.edit();
					edit.putInt(PreferenceKeys.BULBS_UNLOCKED, numUnlocked);
					edit.commit();

				}
			}

		}
	};

}
