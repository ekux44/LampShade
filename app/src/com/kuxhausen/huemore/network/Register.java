package com.kuxhausen.huemore.network;

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

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.HueBridge;
import com.kuxhausen.huemore.state.RegistrationRequest;
import com.kuxhausen.huemore.state.RegistrationResponse;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

public class Register extends AsyncTask<Object, Integer, Boolean> {

	Context cont;
	String bridge = "";
	String username;
	String deviceType;

	OnRegisterListener mResultListener;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnRegisterListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onRegisterResult(boolean success, String bridge, String username);
	}
	
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

	@Override
	protected Boolean doInBackground(Object... params) {
		
			// Get session ID
			cont = (Context) params[0];
			mResultListener = (OnRegisterListener)params[1];
			username = (String) params[2];
			deviceType = (String) params[3];

			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://" + getBridge()
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
					return true;

			} catch (ClientProtocolException e) {

				// TODO Auto-generated catch block
			} catch (IOException e) {

				// TODO Auto-generated catch block
			} catch (java.lang.IllegalArgumentException e) {
				// TODO deal with null IP from getBridge
			}

		

		return false;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		mResultListener.onRegisterResult(success, bridge, username);
	}
}