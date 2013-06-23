package com.kuxhausen.huemore.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.kuxhausen.huemore.state.api.Bridge;
import com.kuxhausen.huemore.state.api.RegistrationRequest;
import com.kuxhausen.huemore.state.api.RegistrationResponse;

public class Register extends AsyncTask<Void, Integer, String> {

	private Context cont;
	private String username, deviceType;
	private OnRegisterListener mResultListener;
	private Bridge[] bridges;
	Gson gson = new Gson();

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnRegisterListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onRegisterResult(String bridge, String username);
	}

	public Register(Activity parrentActivity, Bridge[] bRidges,
			OnRegisterListener resultListener, String userName,
			String devicetype) {
		cont = parrentActivity;
		this.bridges = bRidges;

		mResultListener = resultListener;
		username = userName;
		deviceType = devicetype;
	}

	@Override
	protected String doInBackground(Void... voids) {
		if (bridges == null)
			return null;
		for (Bridge b : bridges) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://" + b.internalipaddress
					+ "/api/");

			Log.e("asdf", "registrationAttempt:" + b.internalipaddress);
			try {
				RegistrationRequest request = new RegistrationRequest();
				request.username = username;
				request.devicetype = deviceType;
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

				Log.e("asdf", "responseString" + responseString);

				RegistrationResponse[] responseObject = gson.fromJson(
						responseString, RegistrationResponse[].class);
				if (responseObject.length > 0
						&& responseObject[0].success != null)
					return b.internalipaddress;

			} catch (ClientProtocolException e) {

				// TODO Auto-generated catch block
			} catch (IOException e) {

				// TODO Auto-generated catch block
			} catch (java.lang.IllegalArgumentException e) {
				// TODO deal with null IP from getBridge
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(String bridgeIP) {
		mResultListener.onRegisterResult(bridgeIP, username);
	}
}