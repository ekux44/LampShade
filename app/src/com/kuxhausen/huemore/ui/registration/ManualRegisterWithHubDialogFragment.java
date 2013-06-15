package com.kuxhausen.huemore.ui.registration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.Register;
import com.kuxhausen.huemore.network.Register.OnRegisterListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.Bridge;

public class ManualRegisterWithHubDialogFragment extends DialogFragment
		implements OnRegisterListener {

	public Register networkRegister;
	public Activity parrentActivity;
	public OnRegisterListener me;
	public EditText IPV4part1;
	public EditText IPV4part2;
	public EditText IPV4part3;
	public EditText IPV4part4;
	Gson gson = new Gson();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		parrentActivity = this.getActivity();
		me = this;
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View registerWithHubView = inflater.inflate(
				R.layout.manually_register_with_hub, null);

		IPV4part1 = (EditText) registerWithHubView
				.findViewById(R.id.IPv4editText1);
		IPV4part2 = (EditText) registerWithHubView
				.findViewById(R.id.IPv4editText2);
		IPV4part3 = (EditText) registerWithHubView
				.findViewById(R.id.IPv4editText3);
		IPV4part4 = (EditText) registerWithHubView
				.findViewById(R.id.IPv4editText4);

		builder.setView(registerWithHubView);
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String ip = IPV4part1.getText().toString() + InternalArguments.IPV4dot
								+ IPV4part2.getText().toString() + InternalArguments.IPV4dot
								+ IPV4part3.getText().toString() + InternalArguments.IPV4dot
								+ IPV4part4.getText().toString();
						RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
						Bundle args = new Bundle();
						Bridge b = new Bridge();
						b.internalipaddress = ip;
						Bridge[] bRay = {b};
						args.putString(InternalArguments.BRIDGES, gson.toJson(bRay));
						rwhdf.setArguments(args);
						rwhdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

						dismiss();
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						RegistrationFailDialogFragment rfdf = new RegistrationFailDialogFragment();
						rfdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

						dismiss();
					}
				});

		// Create the AlertDialog object and return it
		return builder.create();
	}

	public String getUserName() {

		try {
			MessageDigest md;
			String serialID = Settings.Secure.ANDROID_ID;
			md = MessageDigest.getInstance(InternalArguments.MD5);
			String resultString = new BigInteger(1, md.digest(serialID
					.getBytes())).toString(16);

			return resultString;
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		// fall back on hash of hueMore if android ID fails
		return InternalArguments.FALLBACK_USERNAME_HASH;
	}

	public String getDeviceType() {
		return getString(R.string.app_name);
	}

	@Override
	public void onRegisterResult(String bridgeIP, String username) {

		if (bridgeIP != null && isAdded()) {

			// Show the success dialog
			RegistrationSuccessDialogFragment rsdf = new RegistrationSuccessDialogFragment();
			rsdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

			// Add username and IP to preferences cache
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(parrentActivity);

			Editor edit = settings.edit();
			edit.putString(PreferencesKeys.BRIDGE_IP_ADDRESS, bridgeIP);
			edit.putString(PreferencesKeys.HASHED_USERNAME, username);
			edit.commit();

			// done with registration dialog
			dismiss();
		}

	}

}