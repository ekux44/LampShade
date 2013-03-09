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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;

public class MoodsFragment extends ListFragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	final static String ARG_POSITION = "position";
	int mCurrentPosition = -1;
	public Context parrentActivity;
	// Identifies a particular Loader being used in this component
	private static final int MOODS_LOADER = 0;
	public CursorAdapter dataSource;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parrentActivity = this.getActivity();

		// If activity recreated (such as from screen rotate), restore
		// the previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
		}

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		getLoaderManager().initLoader(MOODS_LOADER, null, this);

		String[] columns = { MoodColumns.MOOD, MoodColumns._ID };
		Cursor cursor = getActivity().getContentResolver().query(
				DatabaseDefinitions.MoodColumns.CONTENT_URI, // Use the default
																// content URI
																// for the
																// provider.
				columns, // Return the note ID and title for each note.
				null, // No where clause, return all records.
				null, // No where clause, therefore no where column values.
				null // Use the default sort order.
				);

		dataSource = new SimpleCursorAdapter(this.getActivity(),
				R.layout.mood_row, null, columns,
				new int[] { R.id.moodTextView }, 0);

		setListAdapter(dataSource);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.mood_view, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		if (getFragmentManager().findFragmentById(R.id.groups_fragment) != null) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			updateArticleView(args.getInt(ARG_POSITION));
		} else if (mCurrentPosition != -1) {
			// Set article based on saved instance state defined during
			// onCreateView
			updateArticleView(mCurrentPosition);
		}
	}

	public void updateArticleView(int position) {
		// TextView article = (TextView)
		// getActivity().findViewById(R.id.article);
		// article.setText(StaticDataStore.Moods[position]);
		mCurrentPosition = position;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt(ARG_POSITION, mCurrentPosition);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		}
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
		case MOODS_LOADER:
			// Returns a new CursorLoader
			String[] columns = { MoodColumns.MOOD, MoodColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
					DatabaseDefinitions.MoodColumns.CONTENT_URI, // Table
					columns, // Projection to return
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
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		/*
		 * Moves the query results into the adapter, causing the ListView
		 * fronting this adapter to re-display
		 */
		dataSource.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		dataSource.changeCursor(null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(position, true);

		int[] bulbs = { 3, 4 };
		String[] moods = {
				"{\"hue\": 20000, \"sat\": " + (int) (Math.random() * 255)
						+ "}",
				"{\"hue\": 35000, \"sat\": " + (int) (Math.random() * 255)
						+ "}" };
		TransmitGroupMood pushGroupMood = new TransmitGroupMood();
		pushGroupMood.execute(parrentActivity, bulbs, moods);
	}

	private class TransmitGroupMood extends AsyncTask<Object, Void, Integer> {

		Context cont;
		int[] bulbs;
		String[] moods;

		@Override
		protected Integer doInBackground(Object... params) {

			// Get session ID
			cont = (Context) params[0];
			bulbs = (int[]) params[1];
			moods = (String[]) params[2];
			Log.i("asyncTask", "doing");

			// Add username and IP to preferences cache
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
					Log.e("asdf", "" + statusCode);
					if (statusCode == 200) {

						Log.e("asdf", response.toString());

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
						Log.e("asdf", debugOutput);
					} else {
						Log.e("asdf", "Failed");
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.i("asyncTask", "finishing");
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.i("asyncTask", "finished");
		}
	}
}
