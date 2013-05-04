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

import com.google.gson.Gson;
import com.kuxhausen.huemore.state.api.Bridge;
import com.kuxhausen.huemore.state.api.RegistrationRequest;
import com.kuxhausen.huemore.state.api.RegistrationResponse;

public class Register extends AsyncTask<Void, Integer, String> {

	private Context cont;
	private String username, deviceType;
	private OnRegisterListener mResultListener;
	private Bridge[] bridges;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnRegisterListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onRegisterResult(String bridge, String username);
	}

	public Register(Activity parrentActivity, String ip,
			OnRegisterListener resultListener, String userName,
			String devicetype) {
		cont = parrentActivity;
		if (ip != null && !ip.equals("")) {
			bridges = new Bridge[1];
			bridges[0] = new Bridge();
			bridges[0].internalipaddress = ip;
		}
		mResultListener = resultListener;
		username = userName;
		deviceType = devicetype;
	}

	public void getBridge() {

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://" + "www.meethue.com/api/nupnp");
		bridges = new Bridge[1];
		bridges[0] = new Bridge();
		bridges[0].internalipaddress = "192.168.1.100";

		try {

			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {

				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));

				String line;
				String jSon = "";
				while ((line = reader.readLine()) != null) {
					builder.append(line);
					jSon += line;
				}

				Gson gson = new Gson();
				try {
					// autoselect first hub if multiple hubs
					bridges = gson.fromJson(jSon, Bridge[].class);
				} catch (NullPointerException e) {

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
	}

	@Override
	protected String doInBackground(Void... voids) {
		if (bridges == null || bridges.equals(""))
			getBridge();
		for (Bridge b : bridges) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://" + b.internalipaddress
					+ "/api/");

			try {
				RegistrationRequest request = new RegistrationRequest();
				request.username = username;
				request.devicetype = deviceType;
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