package com.kuxhausen.huemore.ui.registration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.Register;
import com.kuxhausen.huemore.network.Register.OnRegisterListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

public class RegisterWithHubDialogFragment extends DialogFragment implements
		OnRegisterListener {

	public final long length_in_milliseconds = 15000;
	public final long period_in_milliseconds = 1000;
	public ProgressBar progressBar;
	public CountDownTimer countDownTimer;
	public Register networkRegister;
	public Activity parrentActivity;
	public OnRegisterListener me;
	String ip = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		parrentActivity = this.getActivity();
		me = this;
		if (savedInstanceState != null) {
			ip = savedInstanceState.getString("IP");
		}
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View registerWithHubView = inflater.inflate(R.layout.register_with_hub,
				null);
		builder.setView(registerWithHubView);
		progressBar = (ProgressBar) registerWithHubView
				.findViewById(R.id.timerProgressBar);

		countDownTimer = new CountDownTimer(length_in_milliseconds,
				period_in_milliseconds) {
			private boolean warned = false;

			@Override
			public void onTick(long millisUntilFinished) {
				if (isAdded()) {
					progressBar
							.setProgress((int) (((length_in_milliseconds - millisUntilFinished) * 100.0) / length_in_milliseconds));
					networkRegister = new Register(parrentActivity, ip, me,
							getUserName(), getDeviceType());
					networkRegister.execute();
				}
			}

			@Override
			public void onFinish() {
				if (isAdded()) {
					// try one last time
					networkRegister = new Register(parrentActivity, ip, me,
							getUserName(), getDeviceType());
					networkRegister.execute();

					// launch the failed registration dialog
					RegistrationFailDialogFragment rfdf = new RegistrationFailDialogFragment();
					rfdf.show(getFragmentManager(), "dialog");

					dismiss();
				}
			}
		};
		countDownTimer.start();
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		countDownTimer.cancel();
		onDestroyView();
	}

	public String getUserName() {

		try {
			MessageDigest md;
			String serialID = Settings.Secure.ANDROID_ID;
			md = MessageDigest.getInstance("MD5");
			String resultString = new BigInteger(1, md.digest(serialID
					.getBytes())).toString(16);

			return resultString;
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		// fall back on hash of hueMore if android ID fails
		return "f01623452466afd4eba5c1ed0a0a9395";
	}

	public String getDeviceType() {
		return getString(R.string.app_name);
	}

	@Override
	public void onRegisterResult(boolean success, String bridge, String username) {

		if (success && isAdded()) {
			countDownTimer.cancel();

			// Show the success dialog
			RegistrationSuccessDialogFragment rsdf = new RegistrationSuccessDialogFragment();
			rsdf.show(getFragmentManager(), "dialog");

			// Add username and IP to preferences cache
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(parrentActivity);

			Editor edit = settings.edit();
			edit.putString(PreferencesKeys.BRIDGE_IP_ADDRESS, bridge);
			edit.putString(PreferencesKeys.HASHED_USERNAME, username);
			edit.commit();

			// done with registration dialog
			dismiss();
		}

	}

}