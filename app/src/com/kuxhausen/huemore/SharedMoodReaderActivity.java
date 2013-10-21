package com.kuxhausen.huemore;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.DecodeErrorActivity;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class SharedMoodReaderActivity extends NetworkManagedSherlockFragmentActivity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	private SharedMoodReaderActivity me;

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0;

	private SeekBar brightnessBar;
	private Spinner groupSpinner;
	private SimpleCursorAdapter groupDataSource;
	
	private EditText name;
	
	Button previewButton;
	Mood sharedMood;
	
	boolean isTrackingTouch = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shared_mood_reader);

		me = this;
		
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;
		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(GROUPS_LOADER, null, this);

		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				me.setBrightness(seekBar.getProgress());
				isTrackingTouch = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				me.setBrightness(seekBar.getProgress());
				isTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					me.setBrightness(seekBar.getProgress());
				}
			}
		});

		groupSpinner = (Spinner) this.findViewById(R.id.groupSpinner);
		String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
		groupDataSource = new SimpleCursorAdapter(this, layout, null, gColumns,
				new int[] { android.R.id.text1 }, 0);
		groupDataSource
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupDataSource);

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		previewButton = (Button) this.findViewById(R.id.previewButton);
		previewButton.setOnClickListener(this);
		
		name = (EditText)this.findViewById(R.id.moodNameEditText);
		
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
	}

	@Override
	public void onBrightnessChanged(int brightness) {
		if(brightnessBar!=null && !isTrackingTouch)
			brightnessBar.setProgress(brightness);
	}
	@Override
	public void onResume() {
		super.onResume();
		
		Uri data = getIntent().getData();
		String encodedMood = data.getQuery();
		try {
			sharedMood = HueUrlEncoder.decode(encodedMood).second.first;
		} catch (InvalidEncodingException e) {
			BrokenLinkDialogFragment bldf = new BrokenLinkDialogFragment();
			bldf.show(this.getSupportFragmentManager(),InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		} catch (FutureEncodingException e) {
			PromptUpdateDialogFragment pudf = new PromptUpdateDialogFragment();
			pudf.show(this.getSupportFragmentManager(),InternalArguments.FRAG_MANAGER_DIALOG_TAG);

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.previewButton:
				Utils.transmit(me, sharedMood, getBulbs(), name.getText().toString(),null);
				break;
			case R.id.okay:
				String moodName = name.getText().toString();
				// delete any old mood with same name //todo warn users
				String moodSelect = MoodColumns.MOOD + "=?";
				String[] moodArg = { moodName };
				this.getContentResolver().delete(
						DatabaseDefinitions.MoodColumns.MOODS_URI,
						moodSelect, moodArg);
				
				ContentValues mNewValues = new ContentValues();
				mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, moodName);
				mNewValues.put(DatabaseDefinitions.MoodColumns.STATE, HueUrlEncoder.encode(sharedMood));
				
				this.getContentResolver().insert(DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues);
				this.finish();
				break;
			case R.id.cancel:
				this.finish();
				break;
		}
	}
	
	public Integer[] getBulbs(){
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
		return groupStates.toArray(new Integer[groupStates.size()]);
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
		}
	}
}
