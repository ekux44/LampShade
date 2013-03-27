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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.database.DatabaseHelper;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.ui.registration.RegisterWithHubDialogFragment;

/**
 * @author Eric Kuxhausen
 * 
 */
public class MainActivity extends FragmentActivity implements
		GroupBulbPagingFragment.OnBulbGroupSelectedListener,
		MoodsFragment.OnMoodSelectedListener {

	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	Integer[] bulbS;
	String mood;
	IabHelper mPlayHelper;

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
			// GroupsFragment firstFragment = new GroupsFragment();

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
			databaseHelper.initialPopulate();// initialize database

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
		String firstChunk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPUhHgGEdnpyPMAWgP3Xw/jHkReU1O0n6d4rtcULxOrVl/hcZlOsVyByMIZY5wMD84gmMXjbz8pFb4RymFTP7Yp8LSEGiw6DOXc7ydNd0lbZ4WtKyDEwwaio1wRbRPxdU7/4JBpMCh9L6geYx6nYLt0ExZEFxULV3dZJpIlEkEYaNGk/64gc0l34yybccYfORrWzu8u+";
		String secondChunk = "5YxJ5k1ikIJJ2I7/2Rp5AXkj2dWybmT+AGx83zh8+iMGGawEQerGtso9NUqpyZWU08EO9DcF8r2KnFwjmyWvqJ2JzbqCMNt0A08IGQNOrd16/C/65GE6J/EtsggkNIgQti6jD7zd3b2NAQIDAQAB";
		String base64EncodedPublicKey= firstChunk + secondChunk;
				// compute your public key and store it in base64EncodedPublicKey
		   mPlayHelper = new IabHelper(this, base64EncodedPublicKey);
		   mPlayHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   public void onIabSetupFinished(IabResult result) {
			      if (!result.isSuccess()) {
			         // Oh noes, there was a problem.
			         Log.d("asdf", "Problem setting up In-app Billing: " + result);
			      }            
			         // Hooray, IAB is fully set up!  
			   }
			});
	}

	@Override
	public void onDestroy() {
	   super.onDestroy();
	   if (mPlayHelper != null) mPlayHelper.dispose();
	   mPlayHelper = null;
	}
	
	@Override
	public void onGroupBulbSelected(Integer[] bulb) {
		bulbS = bulb;

		// Capture the article fragment from the activity layout
		MoodManualPagingFragment moodFrag = (MoodManualPagingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.moods_fragment);

		if (moodFrag != null) {
			// If article frag is available, we're in two-pane layout...

			// Call a method in the ArticleFragment to update its content
			moodFrag.reset();

		} else {
			// If the frag is not available, we're in the one-pane layout and
			// must swap frags...

			// Create fragment and give it an argument for the selected article
			MoodManualPagingFragment newFragment = new MoodManualPagingFragment();
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		}

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

	/*
	 * test mood by applying to json states array to these bulbs
	 * 
	 * @param states
	 */
	/*
	 * public void testMood(Integer[] bulbs, String[] states) {
	 * TransmitGroupMood pushGroupMood = new TransmitGroupMood();
	 * pushGroupMood.execute(this, bulbs, states); }
	 */

	/**
	 * test mood by applying to json states array to previously selected moods
	 * 
	 * @param states
	 */
	public void testMood(String[] states) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood();
		pushGroupMood.execute(this, bulbS, states);
	}

	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;

		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { mood };
		Cursor cursor = getContentResolver().query(
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

}
