package com.kuxhausen.huemore.ui;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class SerializedEditorActivity extends NetworkManagedSherlockFragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	Context context;
	Gson gson = new Gson();

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

	private SeekBar brightnessBar;
	private Spinner groupSpinner, moodSpinner;
	private SimpleCursorAdapter groupDataSource, moodDataSource;

	private GroupMoodBrightness priorGMB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;
		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(GROUPS_LOADER, null, this);
		lm.initLoader(MOODS_LOADER, null, this);

		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				preview();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});

		groupSpinner = (Spinner) this.findViewById(R.id.groupSpinner);
		String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
		groupDataSource = new SimpleCursorAdapter(this, layout, null, gColumns,
				new int[] { android.R.id.text1 }, 0);
		groupDataSource
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupDataSource);

		moodSpinner = (Spinner) this.findViewById(R.id.moodSpinner);
		String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
		moodDataSource = new SimpleCursorAdapter(this, layout, null, mColumns,
				new int[] { android.R.id.text1 }, 0);
		moodDataSource
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moodSpinner.setAdapter(moodDataSource);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			initializeActionBar(true);

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initializeActionBar(Boolean value) {
		try {
			this.getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e) {
		}
	}

	public void preview() {
		// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { ((TextView) groupSpinner.getSelectedView())
				.getText().toString() };
		Cursor groupCursor = getContentResolver().query(
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
		while (groupCursor.moveToNext()) {
			groupStates.add(groupCursor.getInt(0));
		}
		Integer[] bulbS = groupStates.toArray(new Integer[groupStates.size()]);

		String moodName = ((TextView) moodSpinner.getSelectedView())
				.getText().toString();
		Mood m = Utils.getMoodFromDatabase(moodName, this);
		
		int brightness = brightnessBar.getProgress();
		for (int i = 0; i < m.events.length; i++) {
			//rewrite the brightness of all events to match brightness bar... need to find a smarter approach to this
			m.events[i].state.bri = brightness;
		}
		
		Utils.transmit(context, InternalArguments.ENCODED_MOOD, m, bulbS, moodName);
	}

	public String getSerializedByNamePreview() {
		GroupMoodBrightness gmb = new GroupMoodBrightness();
		gmb.group = ((TextView) groupSpinner.getSelectedView()).getText()
				.toString();
		gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText()
				.toString();
		gmb.brightness = brightnessBar.getProgress();
		return gmb.group + " -> " + gmb.mood + " @ "
				+ ((gmb.brightness * 100) / 255) + "%";
	}

	public void setSerializedByName(String s) {
		priorGMB = gson.fromJson(s, GroupMoodBrightness.class);

	}

	public String getSerializedByName() {
		GroupMoodBrightness gmb = new GroupMoodBrightness();
		gmb.group = ((TextView) groupSpinner.getSelectedView()).getText()
				.toString();
		gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText()
				.toString();
		gmb.brightness = brightnessBar.getProgress();
		return gson.toJson(gmb);
	}

	public String getSerializedByValue() {
		String url = "kuxhausen.com/HueMore/nfc?";

		// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { ((TextView) groupSpinner.getSelectedView())
				.getText().toString() };
		Cursor groupCursor = getContentResolver().query(
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
		while (groupCursor.moveToNext()) {
			groupStates.add(groupCursor.getInt(0));
		}
		Integer[] bulbS = groupStates.toArray(new Integer[groupStates.size()]);

		Mood m = Utils.getMoodFromDatabase( ((TextView) moodSpinner.getSelectedView())
				.getText().toString(), this);
		
		int brightness = brightnessBar.getProgress();
		for (int i = 0; i < m.events.length; i++) {
			//rewrite the brightness of all events to match brightness bar... need to find a smarter approach to this
			m.events[i].state.bri = brightness;
		}
		
		String data = HueUrlEncoder.encode(m, bulbS);
		return url + data;
	}

	/**
	 * Callback that's invoked when the system has initialized the Loader and is
	 * ready to start the query. This usually happens when initLoader() is
	 * called. The loaderID argument contains the ID value passed to the
	 * initLoader() call.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
		/*
		 * Takes action based on the ID of the Loader that's being created
		 */
		switch (loaderID) {
		case GROUPS_LOADER:
			// Returns a new CursorLoader
			String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
			return new CursorLoader(this, // Parent activity context
					DatabaseDefinitions.GroupColumns.GROUPS_URI, // Table
					gColumns, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		case MOODS_LOADER:
			// Returns a new CursorLoader
			String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
			return new CursorLoader(this, // Parent activity context
					DatabaseDefinitions.MoodColumns.MOODS_URI, // Table
					mColumns, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		default:
			// An invalid id was passed in
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		/*
		 * Moves the query results into the adapter, causing the ListView
		 * fronting this adapter to re-display
		 */
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(cursor);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(cursor);
			}
			break;
		}

		if (priorGMB != null) {

			// apply prior state
			int moodPos = 0;
			for (int i = 0; i < moodDataSource.getCount(); i++) {
				if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(
						priorGMB.mood))
					moodPos = i;
			}
			moodSpinner.setSelection(moodPos);

			int groupPos = 0;
			for (int i = 0; i < groupDataSource.getCount(); i++) {
				if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(
						priorGMB.group))
					groupPos = i;
			}
			groupSpinner.setSelection(groupPos);
			brightnessBar.setProgress(priorGMB.brightness);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(null);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(null);
			}
			break;
		}
	}
}