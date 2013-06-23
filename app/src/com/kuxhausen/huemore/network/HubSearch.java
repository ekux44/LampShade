package com.kuxhausen.huemore.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.kuxhausen.huemore.state.api.Bridge;
import com.kuxhausen.huemore.state.api.RegistrationRequest;
import com.kuxhausen.huemore.state.api.RegistrationResponse;

public class HubSearch extends AsyncTask<Void, Void, Bridge[]> {

	private OnHubFoundListener mResultListener;
	Gson gson = new Gson();

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnHubFoundListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onHubFoundResult(Bridge[] bridges);
	}

	public HubSearch(OnHubFoundListener resultListener) {
		mResultListener = resultListener;
	}

	public Bridge[] getBridgesAPI() {
		Bridge[] result = null;

		StringBuilder builder = new StringBuilder();

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		HttpGet httpGet = new HttpGet("http://" + "www.meethue.com/api/nupnp");

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
		if (result != null && result.length < 1)
			result = null;
		return result;
	}

	@Override
	protected Bridge[] doInBackground(Void... voids) {

		Bridge[] result = new Bridge[0];
		result = getBridgesAPI();
		if (result != null)
			return result;
		ArrayList<Bridge> results = new ArrayList<Bridge>();

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 120;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 160;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		for (int i = 0; i < 256; i++) {

			if (this.isCancelled())
				i = 256;

			Bridge possible = checkIP((i + 100) % 256, client);
			if (possible != null)
				results.add(possible);
		}
		result = results.toArray(new Bridge[0]);
		return result;

	}

	// TODO rewrite
	private Bridge checkIP(int local, HttpClient client) {
		String username = "asdf";

		Bridge candidate = new Bridge();
		candidate.internalipaddress = "192.168.1." + local;

		StringBuilder builder = new StringBuilder();

		HttpGet httpGet = new HttpGet("http://" + candidate.internalipaddress
				+ "/api/" + username + "/lights/1");

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				return candidate;
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			// TODO deal with null IP from getBridge
		}

		return null;
	}

	@Override
	protected void onPostExecute(Bridge[] bridges) {
		mResultListener.onHubFoundResult(bridges);
	}
}