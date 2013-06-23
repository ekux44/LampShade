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
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbAttributes;

public class GetBulbsAttributes extends AsyncTask<Void, Void, BulbAttributes[]> {

	private Context cont;
	private Integer[] bulbs;
	private OnAttributeListReturnedListener mResultListener;
	Gson gson = new Gson();
	private MainActivity tracker;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnAttributeListReturnedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onListReturned(BulbAttributes[] bulbsAttributes);
	}

	public GetBulbsAttributes(Context context, Integer[] bulbS,
			OnAttributeListReturnedListener resultListener, MainActivity ma) {
		cont = context;
		bulbs = bulbS;
		mResultListener = resultListener;
		tracker = ma;
		tracker.inFlight.add(this);
	}

	@Override
	protected BulbAttributes[] doInBackground(Void... voids) {

		if (cont == null || bulbs == null || mResultListener == null)
			return null;

		BulbAttributes[] result = new BulbAttributes[bulbs.length];
		

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(cont);
		String bridge = settings.getString(PreferencesKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferencesKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return null;

		for (int i = 0; i < bulbs.length; i++) {

			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();

			HttpGet httpGet = new HttpGet("http://" + bridge + "/api/" + hash
					+ "/lights/" + bulbs[i]);

			String jSon = "";
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
						jSon += line;
					}
					if (jSon.charAt(0) == '[')
						return null;
					// Log.d("asdf",i+ " "+bulbs[i] +jSon);
					result[i] = gson.fromJson(jSon, BulbAttributes.class);
					// Log.d("asdf",
					// result[i].state.hue+" "+result[i].state.sat+" "+result[i].state.xy[0]+" "+result[i].state.xy[1]);

				} else {
					// Hue not found?
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}
		return result;
	}

	@Override
	protected void onPostExecute(BulbAttributes[] result) {
		tracker.inFlight.remove(this);
		mResultListener.onListReturned(result);

	}
}