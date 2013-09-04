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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.api.Bulb;

public class GetBulbList extends AsyncTask<Object, Void, Bulb[]> {

	private Context cont;
	private OnBulbListReturnedListener mResultListener;
	Gson gson = new Gson();
	private GodObject tracker;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnBulbListReturnedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onListReturned(Bulb[] result);
	}

	public GetBulbList(Context context,
			OnBulbListReturnedListener resultListener, GodObject ma) {
		cont = context;
		mResultListener = resultListener;
		tracker = ma;
		tracker.getInFlight().add(this);
	}

	@Override
	protected Bulb[] doInBackground(Object... params) {
		Bulb[] returnOutput = null;

		if (cont == null || mResultListener == null)
			return returnOutput;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(cont);
		String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferenceKeys.HASHED_USERNAME, "");

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
				String jSon = "";

				while ((line = reader.readLine()) != null) {
					builder.append(line);
					jSon += line;
				}
				jSon = "[" + jSon.substring(1, jSon.length() - 1) + "]";
				jSon = jSon.replaceAll("\"[:digit:]+\":", "");

				returnOutput = gson.fromJson(jSon, Bulb[].class);
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
	protected void onPostExecute(Bulb[] result) {
		tracker.getInFlight().remove(this);
		mResultListener.onListReturned(result);

		if(result!=null && result.length>0){
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(cont);
			Editor edit = settings.edit();
			edit.putInt(PreferenceKeys.NUMBER_OF_CONNECTED_BULBS,result.length);
			edit.commit();
			
			tracker.setHubConnectionState(true);
		} else{
			tracker.setHubConnectionState(false);
		}
	}
}