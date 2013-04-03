package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kuxhausen.huemore.GroupBulbPagingFragment.OnBulbGroupSelectedListener;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;

public class GroupsFragment extends ListFragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	OnBulbGroupSelectedListener mCallback;

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0;
	public CursorAdapter dataSource;
	public TextView selected; // updated on long click

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		getLoaderManager().initLoader(GROUPS_LOADER, null, this);

		String[] columns = { GroupColumns.GROUP, BaseColumns._ID };

		dataSource = new SimpleCursorAdapter(this.getActivity(), layout, null,
				columns, new int[] { android.R.id.text1 }, 0);

		setListAdapter(dataSource);

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.group_view, container, false);

		ImageButton newGroup = (ImageButton) myView
				.findViewById(R.id.newGroupButton);
		newGroup.setOnClickListener(this);

		LinearLayout headingRow = (LinearLayout) myView
				.findViewById(R.id.showOnLandScape);
		if (headingRow.getVisibility() == View.GONE)
			setHasOptionsMenu(true);
		return myView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.newGroupButton:

			NewGroupDialogFragment ngdf = new NewGroupDialogFragment();
			ngdf.show(getFragmentManager(), "dialog");

			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_group, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_add:
			NewGroupDialogFragment ngdf = new NewGroupDialogFragment();
			ngdf.show(getFragmentManager(), "dialog");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		if (getFragmentManager().findFragmentById(R.id.groups_fragment) != null) {
			getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			mCallback = (MainActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		selected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
		if (selected.getText().equals(PreferencesKeys.ALL)) {
			return;
		}
		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_group, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		if (selected == null)
			return false;

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextgroupmenu_delete: // <-- your custom menu item id here
			String groupSelect = GroupColumns.GROUP + "=?";
			String[] groupArg = { (String) (selected).getText() };
			getActivity().getContentResolver().delete(
					DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
					groupSelect, groupArg);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selected = ((TextView) (v));

		// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { (String) ((TextView) (v)).getText() };
		Cursor cursor = getActivity().getContentResolver().query(
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
		Integer[] bulbS = groupStates.toArray(new Integer[groupStates.size()]);

		// Notify the parent activity of selected bulbs
		mCallback.onGroupBulbSelected(bulbS);

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(position, true);
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
			String[] columns = { GroupColumns.GROUP, BaseColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
					DatabaseDefinitions.GroupColumns.GROUPS_URI, // Table
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