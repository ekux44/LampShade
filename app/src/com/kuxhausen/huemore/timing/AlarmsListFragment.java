package com.kuxhausen.huemore.timing;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class AlarmsListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	// Identifies a particular Loader being used in this component
	private static final int ALARMS_LOADER = 0;
	private AlarmRowAdapter dataSource;
	private AlarmRow selectedRow;

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
		dataSource = new AlarmRowAdapter(this.getActivity(),
				R.layout.alarm_row, null, columns,
				new int[] { R.id.subTextView }, 0);

		setListAdapter(dataSource);

		View myView = inflater.inflate(R.layout.alarm_view, null);

		LinearLayout headingRow = (LinearLayout) myView
				.findViewById(R.id.showOnLandScape);
		if (headingRow.getVisibility() == View.GONE)
			setHasOptionsMenu(true);

		getActivity().setTitle(R.string.alarms);

		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		return myView;
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
			nadf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			nadf.onLoadLoaderManager(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		LinearLayout selected = (LinearLayout) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
		selectedRow = ((AlarmRow) selected.getChildAt(0).getTag());
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_alarm, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextalarmmenu_edit: // <-- your custom menu item id here
			NewAlarmDialogFragment nadf = new NewAlarmDialogFragment();
			nadf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			Log.e("asdf", "edit has state"
					+ (selectedRow.getAlarmState() != null));
			nadf.onLoadLoaderManager(selectedRow.getAlarmState());

			// also delete
			if (selectedRow.isScheduled())
				selectedRow.toggle();

			String moodSelect2 = BaseColumns._ID + "=?";
			String[] moodArg2 = { "" + selectedRow.getID() };
			getActivity().getContentResolver().delete(AlarmColumns.ALARMS_URI,
					moodSelect2, moodArg2);

			return true;

		case R.id.contextalarmmenu_delete: // <-- your custom menu item id here
			if (selectedRow.isScheduled())
				selectedRow.toggle();

			String moodSelect = BaseColumns._ID + "=?";
			String[] moodArg = { "" + selectedRow.getID() };
			getActivity().getContentResolver().delete(AlarmColumns.ALARMS_URI,
					moodSelect, moodArg);
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
		// Log.e("onCreateLoader", ""+loaderID);
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
		// Log.e("onLoaderFinished", arg0.toString());
		/*
		 * Moves the query results into the adapter, causing the ListView
		 * fronting this adapter to re-display
		 */
		dataSource.changeCursor(cursor);
		registerForContextMenu(getListView());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// Log.e("onLoaderReset", arg0.toString());
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		dataSource.changeCursor(null);
	}
}
