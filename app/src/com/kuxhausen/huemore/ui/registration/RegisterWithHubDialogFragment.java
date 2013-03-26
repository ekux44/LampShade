package com.kuxhausen.huemore.ui.registration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.string;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.network.Register;
import com.kuxhausen.huemore.network.Register.OnRegisterListener;
import com.kuxhausen.huemore.state.HueBridge;
import com.kuxhausen.huemore.state.RegistrationRequest;
import com.kuxhausen.huemore.state.RegistrationResponse;

public class RegisterWithHubDialogFragment extends DialogFragment implements
		OnRegisterListener {

	public final long length_in_milliseconds = 15000;
	public final long period_in_milliseconds = 1000;
	public ProgressBar progressBar;
	public CountDownTimer countDownTimer;
	public Register networkRegister;
	public Context parrentActivity;
	public OnRegisterListener me;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		parrentActivity = this.getActivity();
		me = this;
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
					networkRegister = new Register();
					networkRegister.execute(parrentActivity, me, getUserName(),
							getDeviceType());
				}
			}

			@Override
			public void onFinish() {
				if (isAdded()) {
					// try one last time
					networkRegister = new Register();
					networkRegister.execute(parrentActivity, me, getUserName(),
							getDeviceType());

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
			edit.putString(PreferencesKeys.Bridge_IP_Address, bridge);
			edit.putString(PreferencesKeys.Hashed_Username, username);
			edit.commit();

			// done with registration dialog
			dismiss();
		}

	}

}