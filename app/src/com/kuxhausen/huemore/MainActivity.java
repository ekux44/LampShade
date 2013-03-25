package com.kuxhausen.huemore;

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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kuxhausen.huemore.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;

public class MainActivity extends FragmentActivity implements
		GroupBulbPagingFragment.OnHeadlineSelectedListener,
		MoodsFragment.OnMoodSelectedListener {

	DatabaseHelper helper = new DatabaseHelper(this);
	String group;
	Integer[] bulbS;
	String mood;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hue_more);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
		GroupBulbPagingFragment firstFragment = new GroupBulbPagingFragment();
		//		GroupsFragment firstFragment = new GroupsFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();

		}
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!settings.contains(PreferencesKeys.First_Run)) {
			helper.initialPopulate();// initialize database

			// Mark no longer first run in preferences cache
			Editor edit = settings.edit();
			edit.putBoolean(PreferencesKeys.First_Run, false);
			edit.putInt(PreferencesKeys.Bulbs_Unlocked, 10);// TODO load from
															// google store
			edit.commit();
		}
		// check to see if the bridge IP address is setup yet
		if (!settings.contains(PreferencesKeys.Bridge_IP_Address)) {
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			rwhdf.show(this.getSupportFragmentManager(), "dialog");
		}
	}

	@Override
	public void onGroupSelected(String groupParam) {
		group = groupParam;
		// The user selected the headline of an article from the
		// HeadlinesFragment

		// Capture the article fragment from the activity layout
		MoodsFragment moodFrag = (MoodsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.moods_fragment);

		if (moodFrag != null) {
			// If article frag is available, we're in two-pane layout...

			// Call a method in the ArticleFragment to update its content
			moodFrag.updateGroupView(groupParam);

		} else {
			// If the frag is not available, we're in the one-pane layout and
			// must swap frags...

			// Create fragment and give it an argument for the selected article
			MoodsFragment newFragment = new MoodsFragment();
			Bundle args = new Bundle();
			args.putString(MoodsFragment.ARG_GROUP, groupParam);
			newFragment.setArguments(args);
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
		//pushMoodGroup();
	}

	@Override
	public void onMoodSelected(String moodParam) {
		mood = moodParam;
		pushMoodGroup();
	}

	public void onBrightnessChanged(String brightnessState[]) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood();
		pushGroupMood.execute(this, bulbS, brightnessState);
	}

	public void testMood(Integer[] bulbs, String[] states) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood();
		pushGroupMood.execute(this, bulbs, states);
	}

	private void pushMoodGroup() {
		if (group == null || mood == null)
			return;
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { group };
		Cursor cursor = getContentResolver().query(
				DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use the
																	// default
																	// content
																	// URI
																	// for the
																	// provider.
				groupColumns, // Return the note ID and title for each note.
				GroupColumns.GROUP + "=?", // selection clause
				gWhereClause, // selection clause args
				null // Use the default sort order.
				);

		ArrayList<Integer> groupStates = new ArrayList<Integer>();
		while (cursor.moveToNext()) {
			groupStates.add(cursor.getInt(0));
		}
		bulbS = groupStates.toArray(new Integer[groupStates.size()]);
		
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { mood };
		cursor = getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use the
																// default
																// content URI
																// for the
																// provider.
				moodColumns, // Return the note ID and title for each note.
				MoodColumns.MOOD + "=?", // selection clause
				mWereClause, // election clause args
				null // Use the default sort order.
				);

		ArrayList<String> moodStates = new ArrayList<String>();
		while (cursor.moveToNext()) {
			moodStates.add(cursor.getString(0));
		}
		String[] moodS = moodStates.toArray(new String[moodStates.size()]);

		TransmitGroupMood pushGroupMood = new TransmitGroupMood();
		pushGroupMood.execute(this, bulbS, moodS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_register_with_hub:
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			rwhdf.show(getSupportFragmentManager(), "dialog");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class TransmitGroupMood extends AsyncTask<Object, Void, Integer> {

		Context cont;
		Integer[] bulbs;
		String[] moods;

		@Override
		protected Integer doInBackground(Object... params) {

			// Get session ID
			cont = (Context) params[0];
			bulbs = (Integer[]) params[1];
			moods = (String[]) params[2];
			
			if (cont == null || bulbs == null || moods == null)
				return -1;

			// Get username and IP from preferences cache
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(cont);
			String bridge = settings.getString(
					PreferencesKeys.Bridge_IP_Address, null);
			String hash = settings.getString(PreferencesKeys.Hashed_Username,
					"");

			if (bridge == null)
				return -1;

			for (int i = 0; i < bulbs.length; i++) {

				StringBuilder builder = new StringBuilder();
				HttpClient client = new DefaultHttpClient();

				HttpPut httpPut = new HttpPut("http://" + bridge + "/api/"
						+ hash + "/lights/" + bulbs[i] + "/state");
				try {

					StringEntity se = new StringEntity(moods[i % moods.length]);

					// sets the post request as the resulting string
					httpPut.setEntity(se);

					HttpResponse response = client.execute(httpPut);
					StatusLine statusLine = response.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {

						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						String debugOutput = "";
						while ((line = reader.readLine()) != null) {
							builder.append(line);
							debugOutput += line;
						}
					} else {
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
		}
	}
}
