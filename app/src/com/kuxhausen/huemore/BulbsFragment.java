package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.GroupBulbPagingFragment.OnBulbGroupSelectedListener;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.state.HueBulb;

public class BulbsFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>,
		GetBulbList.OnListReturnedListener {
	OnBulbGroupSelectedListener mCallback;

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0;
	// public CursorAdapter dataSource;
	public TextView selected; // updated on long click

	ArrayList<String> bulbNameList;
	ArrayAdapter<String> rayAdapter;
	HueBulb[] bulbArray;

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

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.bulb_view, container, false);

		bulbNameList = new ArrayList<String>();
		rayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_1, bulbNameList);
		setListAdapter(rayAdapter);

		GetBulbList pushGroupMood = new GetBulbList();
		pushGroupMood.execute(getActivity(), this);

		return myView;
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

		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_bulb, menu);
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

		// Notify the parent activity of selected item
		Integer[] iPos = { position + 1 };
		mCallback.onGroupBulbSelected(iPos);

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
		// dataSource.changeCursor(cursor);
		registerForContextMenu(getListView());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		// unregisterForContextMenu(getListView());
		// dataSource.changeCursor(null);
	}

	@Override
	public void onListReturned(String jSon) {
		if (jSon == null || jSon.equals(""))
			return;
		Gson gson = new Gson();
		bulbArray = gson.fromJson(jSon, HueBulb[].class);

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int numberBulbsUnlocked = settings.getInt(
				PreferencesKeys.Bulbs_Unlocked, 4);
		if (bulbArray.length > numberBulbsUnlocked) {
			// tell user to upgrade
		}

		for (int i = 0; i < Math.min(bulbArray.length, numberBulbsUnlocked); i++) {
			// bulbNameList.add(bulb.name);
			HueBulb bulb = bulbArray[i];
			bulb.number = i + 1;
			rayAdapter.add(bulb.name);
		}

	}
}
