package com.kuxhausen.huemore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.HueBulb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class NewGroupDialogFragment extends DialogFragment{

	ArrayList<String> bulbNameList;
	ListView bulbsListView;
	ArrayAdapter<String> rayAdapter;
	EditText nameEditText;
	HueBulb[] bulbArray;
	HashMap<String, Integer> nameToBulb;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		bulbNameList = new ArrayList<String>();
		nameToBulb = new HashMap<String,Integer>();
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_group_dialog,
				null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		bulbsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		rayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_multiple_choice,
				bulbNameList);
		bulbsListView.setAdapter(rayAdapter);
		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);
		
		GetBulbList pushGroupMood = new GetBulbList();
		pushGroupMood.execute(getActivity());
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						
						
						ArrayList<String> checkedBulbs = new ArrayList<String>();
						SparseBooleanArray set = bulbsListView
								.getCheckedItemPositions();
						for (int i = 0; i < rayAdapter.getCount(); i++) {
							if (set.get(i)) {
								checkedBulbs.add(nameToBulb.get((rayAdapter.getItem(i))).toString());
							}
						}

						String groupname = nameEditText.getText().toString();

						for (int i = 0; i < checkedBulbs.size(); i++) {
							// Defines a new Uri object that receives the result
							// of the insertion
							Uri mNewUri;

							// Defines an object to contain the new values to
							// insert
							ContentValues mNewValues = new ContentValues();

							/*
							 * Sets the values of each column and inserts the
							 * word. The arguments to the "put" method are
							 * "column name" and "value"
							 */
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.GROUP,
									groupname);
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.BULB,
									Integer.parseInt(checkedBulbs.get(i)));
							mNewValues
									.put(DatabaseDefinitions.GroupColumns.PRECEDENCE,
											i);

							mNewUri = getActivity()
									.getContentResolver()
									.insert(DatabaseDefinitions.GroupColumns.GROUPS_URI,
											mNewValues // the values to insert
									);
							Log.i("contentAdded", "groupname:" + groupname
									+ " bulb:" + checkedBulbs.get(i));
						}

					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}
	public void populateList(String jSon){
		if(jSon== null || jSon.equals(""))
			return;
		Gson gson = new Gson();
		bulbArray = gson.fromJson(jSon, HueBulb[].class);
		for(int i = 0; i<bulbArray.length; i++){
			//bulbNameList.add(bulb.name);
			HueBulb bulb = bulbArray[i];
			bulb.number = i;
			nameToBulb.put(bulb.name,bulb.number);
	        rayAdapter.add(bulb.name);
		}
	}
	
	
	class GetBulbList extends AsyncTask<Object, Void, String> {

		Context cont;

		@Override
		protected String doInBackground(Object... params) {
			String returnOutput = "";
			// Get session ID
			cont = (Context) params[0];
			Log.i("asyncTask", "doing");

			if (cont == null)
				return returnOutput;

			// Get username and IP from preferences cache
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(cont);
			String bridge = settings.getString(
					PreferencesKeys.Bridge_IP_Address, null);
			String hash = settings.getString(PreferencesKeys.Hashed_Username,
					"");

			if (bridge == null)
				return returnOutput;

			
				StringBuilder builder = new StringBuilder();
				HttpClient client = new DefaultHttpClient();

				HttpGet httpGet = new HttpGet("http://" + bridge + "/api/"
						+ hash + "/lights");
				
				try {

					HttpResponse response = client.execute(httpGet);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					Log.e("asdf", "" + statusCode);
					if (statusCode == 200) {

						Log.e("asdf", response.toString());

						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						
						while ((line = reader.readLine()) != null) {
							builder.append(line);
							returnOutput += line;
						}
						returnOutput ="["+ returnOutput.substring(1,
								returnOutput.length() - 1)+"]";
						returnOutput = returnOutput.replaceAll("\"[:digit:]+\":", "");
						Log.e("asdf", returnOutput);
					} else {
						Log.e("asdf", "Failed");
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				
			}
			Log.i("asyncTask", "finishing");
			return returnOutput;
		}

		@Override
		protected void onPostExecute(String result) {
			populateList(result);
			Log.i("asyncTask", "finished");
		}
	}
}
