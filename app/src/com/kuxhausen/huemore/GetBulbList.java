package com.kuxhausen.huemore;

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
import org.apache.http.impl.client.DefaultHttpClient;

import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class GetBulbList extends AsyncTask<Object, Void, String> {

	Context cont;
	OnListReturnedListener mResultListener;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnListReturnedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onListReturned(String jsonResult);
	}

	@Override
	protected String doInBackground(Object... params) {
		String returnOutput = "";
		// Get session ID
		cont = (Context) params[0];
		mResultListener = (OnListReturnedListener) params[1];

		if (cont == null)
			return returnOutput;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(cont);
		String bridge = settings.getString(PreferencesKeys.Bridge_IP_Address,
				null);
		String hash = settings.getString(PreferencesKeys.Hashed_Username, "");

		if (bridge == null)
			return returnOutput;

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();

		HttpGet httpGet = new HttpGet("http://" + bridge + "/api/" + hash
				+ "/lights");

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

				while ((line = reader.readLine()) != null) {
					builder.append(line);
					returnOutput += line;
				}
				returnOutput = "["
						+ returnOutput.substring(1, returnOutput.length() - 1)
						+ "]";
				returnOutput = returnOutput.replaceAll("\"[:digit:]+\":", "");

			} else {

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}

		return returnOutput;
	}

	@Override
	protected void onPostExecute(String jsonResult) {
		mResultListener.onListReturned(jsonResult);

	}
}