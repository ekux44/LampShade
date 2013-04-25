package com.kuxhausen.huemore.timing;

import com.kuxhausen.huemore.*;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

public class AlarmsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

	// Identifies a particular Loader being used in this component
	private static final int ALARMS_LOADER = 0;
	public AlarmRowAdapter dataSource;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// We need to use a different list item layout for devices older than
				// Honeycomb
				int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
						: android.R.layout.simple_list_item_1;

				/*
				 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
				 * passed to onCreateLoader().
				 */
				getLoaderManager().initLoader(ALARMS_LOADER, null, this);

				String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
				dataSource = new AlarmRowAdapter(this.getActivity(), R.layout.alarm_row, null,
						columns, new int[] { R.id.subTextView }, 0);
				
				setListAdapter(dataSource);
				
		
		
		
		View myView = inflater.inflate(R.layout.alarm_view, null);
		
		LinearLayout headingRow = (LinearLayout) myView
				.findViewById(R.id.showOnLandScape);
		if (headingRow.getVisibility() == View.GONE)
			setHasOptionsMenu(true);
		getActivity().setTitle(R.string.alarms);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			initializeActionBar(true);
		}
		return myView;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		LinearLayout row = (LinearLayout)v;
		CompoundButton onButton= (CompoundButton)row.getChildAt(1);
		if(onButton.isChecked()){
			//TODO make sure scheduled
		}else{
			//TODO make sure not scheduled
		}

	}
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void initializeActionBar(Boolean value) {
		try {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e) {
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_alarm, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			return true;
		
		case R.id.action_add_alarm:
			NewAlarmDialogFragment nadf = new NewAlarmDialogFragment();
			nadf.show(getFragmentManager(), "dialog");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_alarm, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextalarmmenu_delete: // <-- your custom menu item id here
			//TODO
			
			//String moodSelect = MoodColumns.MOOD + "=?";
			//String[] moodArg = { (String) (selected).getText() };
			//getActivity().getContentResolver().delete(
			//		DatabaseDefinitions.MoodColumns.MOODSTATES_URI, moodSelect,
			//		moodArg);
			return true;

		default:
			return super.onContextItemSelected(item);
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
		case ALARMS_LOADER:
			// Returns a new CursorLoader
			String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
					AlarmColumns.ALARMS_URI, // Table
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
		registerForContextMenu(getListView());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		dataSource.changeCursor(null);
	}
}
