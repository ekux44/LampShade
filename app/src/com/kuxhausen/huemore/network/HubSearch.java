package com.kuxhausen.huemore.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

public class HubSearch extends AsyncTask<Void, Void, Bridge[]> {

	private Context cont;
	private OnHubFoundListener mResultListener;
	Gson gson = new Gson();
	
	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnHubFoundListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onHubFoundResult(Bridge[] bridges);
	}

	public HubSearch(Activity parrentActivity,	OnHubFoundListener resultListener) {
		cont = parrentActivity;
		mResultListener = resultListener;
	}

	public Bridge[] getBridgesAPI() {
		Bridge[] result = null;
		
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://" + "www.meethue.com/api/nupnp");
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			Log.e("api discovery response code:", ""+statusCode);
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

				try {
					result = gson.fromJson(jSon, Bridge[].class);
				} catch (NullPointerException e) {
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			// TODO deal with null IP from getBridge
		}
		return result;
	}

	@Override
	protected Bridge[] doInBackground(Void... voids) {
		Bridge[] result = null;
		result = getBridgesAPI();
		if(result!=null)
			return result;
		ArrayList<Bridge> results = new ArrayList<Bridge>();
		int local = 0;
		while(result!=null && local<256){
			Bridge possible = checkIP(local);
			if(possible!=null)
				results.add(possible);
			local++;
		}
		result = (Bridge[]) results.toArray();
		return result;
		
		
	}
	
	//TODO rewrite
	private Bridge checkIP(int local){
		Bridge candidate = new Bridge();
		candidate.internalipaddress= "192.168.1."+local;
		
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		
		//TODO use different api
		HttpPost httppost = new HttpPost("http://" + candidate.internalipaddress
				+ "/api/");

		try {
			
			RegistrationRequest request = new RegistrationRequest();
			request.username = "asdf";//username; 
			request.devicetype = "asdf";//deviceType;
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
				return candidate;

		} catch (ClientProtocolException e) {

			// TODO Auto-generated catch block
		} catch (IOException e) {

			// TODO Auto-generated catch block
		} catch (java.lang.IllegalArgumentException e) {
			// TODO deal with null IP from getBridge
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Bridge[] bridges) {
		mResultListener.onHubFoundResult(bridges);
	}
}