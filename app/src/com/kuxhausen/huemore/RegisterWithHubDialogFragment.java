package com.kuxhausen.huemore;

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
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.HueBridge;
import com.kuxhausen.huemore.state.RegistrationRequest;
import com.kuxhausen.huemore.state.RegistrationResponse;

public class RegisterWithHubDialogFragment extends DialogFragment {

	public final long length_in_milliseconds = 15000;
	public final long period_in_milliseconds = 1000;
	public ProgressBar progressBar;
	public CountDownTimer countDownTimer;
	public Register networkRegister;
	public Context parrentActivity;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		parrentActivity = this.getActivity();
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
					networkRegister.execute(parrentActivity);
				}
			}

			@Override
			public void onFinish() {
				if (isAdded()) {
					// try one last time
					networkRegister = new Register();
					networkRegister.execute(parrentActivity);

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

	public class Register extends AsyncTask<Object, Integer, Boolean> {

		Context cont;
		String bridge = "";

		public String getBridge() {

			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet("http://"
					+ "www.meethue.com/api/nupnp");
			bridge = "192.168.1.100";

			try {

				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				if (statusCode == 200) {

					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					bridge = "";
					String line;
					String jSon = "";
					while ((line = reader.readLine()) != null) {
						builder.append(line);
						jSon += line;
					}

					Gson gson = new Gson();
					try {
						// autoselect first hub if multiple hubs
						bridge = gson.fromJson(jSon, HueBridge[].class)[0].internalipaddress;
					} catch (NullPointerException e) {
						// publishProgress(-1);
						// TODO
					}

				} else {

				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				// TODO deal with null IP from getBridge
			}

			return bridge;
		}

		/*
		 * protected void onProgressUpdate(Integer progress) { if(progress ==
		 * -1){ Toast t = new Toast(parrentActivity);
		 * t.setDuration(Toast.LENGTH_LONG); t.setText(
		 * "Please make sure this device is on the same wifi as the hub and has internet access, then try to register with hub again"
		 * ); t.show(); } }
		 */

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
			if (isAdded()) {
				return getString(R.string.app_name);
			}
			return null;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			if (isAdded()) {
				// Get session ID
				cont = (Context) params[0];

				// Create a new HttpClient and Post Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://" + getBridge()
						+ "/api/");

				try {
					RegistrationRequest request = new RegistrationRequest();
					request.username = getUserName();
					request.devicetype = getDeviceType();
					Gson gson = new Gson();
					String registrationRequest = gson.toJson(request);

					StringEntity se = new StringEntity(registrationRequest);

					// sets the post request as the resulting string
					httppost.setEntity(se);
					// sets a request header so the page receiving the request
					// will know what to do with it
					httppost.setHeader("Accept", "application/json");
					httppost.setHeader("Content-type", "application/json");

					// execute HTTP post request
					HttpResponse response = httpclient.execute(httppost);

					// analyze the response
					String responseString = EntityUtils.toString(response
							.getEntity());
					responseString = responseString.substring(1,
							responseString.length() - 1);// pull off the outer
															// brackets

					RegistrationResponse responseObject = gson.fromJson(
							responseString, RegistrationResponse.class);
					if (responseObject.success != null)
						return true;

				} catch (ClientProtocolException e) {

					// TODO Auto-generated catch block
				} catch (IOException e) {

					// TODO Auto-generated catch block
				} catch (java.lang.IllegalArgumentException e) {
					// TODO deal with null IP from getBridge
				}

			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {

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
				edit.putString(PreferencesKeys.Hashed_Username, getUserName());
				edit.commit();

				// done with registration dialog
				dismiss();
			}
		}
	}
}