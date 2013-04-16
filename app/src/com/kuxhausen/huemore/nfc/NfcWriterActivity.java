package com.kuxhausen.huemore.nfc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.billing.Base64;
import com.kuxhausen.huemore.billing.Base64DecoderException;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.BulbState;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.nfc.Tag;
import android.nfc.FormatException;
import android.database.Cursor;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class NfcWriterActivity extends FragmentActivity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor>  {
	private Button sendButton;
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	boolean writeMode;
	Tag myTag;
	Context context;
	Gson gson = new Gson();
	
	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;
	
	SeekBar brightnessBar;
	Spinner groupSpinner, moodSpinner;
	SimpleCursorAdapter groupDataSource, moodDataSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_writer);
		context = this;
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

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
		
		
		
		sendButton = (Button) this.findViewById(R.id.writeToTagButton);
		sendButton.setOnClickListener(this);

		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		
		groupSpinner = (Spinner) this.findViewById(R.id.groupSpinner);
		String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
		groupDataSource = new SimpleCursorAdapter(this, layout, null,
				gColumns, new int[] { android.R.id.text1 }, 0);
		groupDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupDataSource);
		
		
		moodSpinner = (Spinner) this.findViewById(R.id.moodSpinner);
		String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
		moodDataSource = new SimpleCursorAdapter(this, layout, null,
				mColumns, new int[] { android.R.id.text1 }, 0);
		moodDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moodSpinner.setAdapter(moodDataSource);
		
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };

		
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
	public void initializeActionBar(Boolean value) {
		try {
			this.getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.writeToTagButton:
			try {
				if (myTag == null) {
					Toast.makeText(context,
							context.getString(R.string.nfc_tag_not_detected),
							Toast.LENGTH_SHORT).show();
				} else {
					write(getMessage(), myTag);
					Toast.makeText(context,
							context.getString(R.string.nfc_tag_write_success),
							Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				Toast.makeText(context,
						context.getString(R.string.nfc_tag_write_fail),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (FormatException e) {
				Toast.makeText(context,
						context.getString(R.string.nfc_tag_write_fail),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			break;
		}
	}
	
	private String getMessage() {
		String url = "kuxhausen.com/HueMore/nfc?";
		
		// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = {((TextView)groupSpinner.getSelectedView()).getText().toString() };
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

		
		
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { ((TextView)moodSpinner.getSelectedView()).getText().toString() };
		Cursor moodCursor = getContentResolver().query(
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
		while (moodCursor.moveToNext()) {
			moodStates.add(moodCursor.getString(0));
		}
		String[] moodS = moodStates.toArray(new String[moodStates.size()]);
		BulbState[] bsRay = new BulbState[moodS.length];
		
		int brightness = brightnessBar.getProgress();
		for(int i = 0; i<moodS.length; i++){
			System.out.println(moodS[i]);
			bsRay[i] = gson.fromJson(moodS[i], BulbState.class);
			bsRay[i].bri = brightness;
			System.out.println(bsRay[i]);
		}
		
		
		String data = HueNfcEncoder.encode(bulbS, bsRay);
		return url+data;
	}

	private void write(String text, Tag tag) throws IOException,
			FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}

	private NdefRecord createRecord(String text)
			throws UnsupportedEncodingException {
		byte[] uriField = text.getBytes(Charset.forName("US-ASCII"));
		byte[] payload = new byte[uriField.length + 1]; // add 1 for the URI
														// Prefix
		payload[0] = 0x01; // prefixes http://www. to the URI
		System.arraycopy(uriField, 0, payload, 1, uriField.length); // appends
																	// URI to
																	// payload
		NdefRecord rtdUriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_URI, new byte[0], payload);

		return rtdUriRecord;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(
					this,
					this.getString(R.string.nfc_tag_detected)
							+ myTag.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume() {
		super.onResume();
		WriteModeOn();
	}

	private void WriteModeOn() {
		writeMode = true;
		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					writeTagFilters, null);
	}

	private void WriteModeOff() {
		writeMode = false;
		if (nfcAdapter != null)
			nfcAdapter.disableForegroundDispatch(this);
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
		switch(loader.getId()){
			case GROUPS_LOADER: if(groupDataSource!=null) {groupDataSource.changeCursor(cursor);} break;
			case MOODS_LOADER:  if(moodDataSource!=null) {moodDataSource.changeCursor(cursor);} break;
		}
		
		//registerForContextMenu(getListView());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		switch(loader.getId()){
			case GROUPS_LOADER: if(groupDataSource!=null) {groupDataSource.changeCursor(null);} break;
			case MOODS_LOADER: if(moodDataSource!=null){ moodDataSource.changeCursor(null); }break;
		}
	}

}
