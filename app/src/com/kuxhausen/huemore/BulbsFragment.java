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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.kuxhausen.huemore.GroupBulbPagingFragment.OnBulbGroupSelectedListener;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.Bulb;

public class BulbsFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>,
		GetBulbList.OnBulbListReturnedListener {
	
	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0;
	// public CursorAdapter dataSource;
	public TextView selected, longSelected; // updated on long click
	private int selectedPos = -1;
	private GroupBulbPagingFragment gbpfCallback;
	
	ArrayList<String> bulbNameList;
	ArrayAdapter<String> rayAdapter;
	Bulb[] bulbArray;

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
		
		refreshList();
		
		((MainActivity) getActivity()).bulbListenerFragment = this;
		return myView;
	}
	public void refreshList(){
		GetBulbList pushGroupMood = new GetBulbList(getActivity(), this);
		pushGroupMood.execute();

	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		//if (getFragmentManager().findFragmentById(R.id.groups_fragment) != null) {
			getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		//}
	}

	public void setSelectionListener(GroupBulbPagingFragment gbpf){
		gbpfCallback = gbpf;
	}
	
	public void invalidateSelection() {
		// Set the previous selected item as checked to be unhighlighted when in
		// two-pane layout
		if (selected != null && selectedPos > -1)
			getListView().setItemChecked(selectedPos, false);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		longSelected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
		
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_bulb, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		if (longSelected == null)
			return false;

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextgroupmenu_rename: // <-- your custom menu item id here
			EditBulbDialogFragment ngdf = new EditBulbDialogFragment();
			Bundle args = new Bundle();
			args.putString(InternalArguments.BULB_NAME, (String) (longSelected).getText());
			args.putInt(InternalArguments.BULB_NUMBER, 1+rayAdapter.getPosition((String) (longSelected).getText()));
			ngdf.setArguments(args);
			ngdf.setBulbsFragment(this);
			ngdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selected = ((TextView) (v));
		selectedPos = position;
		
		// Notify the parent activity of selected item
		Integer[] iPos = { position + 1 };
		gbpfCallback.onSelected(iPos, selected.getText().toString(),null, this);

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(selectedPos, true);
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
	public void onListReturned(Bulb[] result) {
		if (result == null)
			return;
		bulbArray = result;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int numberBulbsUnlocked = settings.getInt(
				PreferencesKeys.BULBS_UNLOCKED,
				PreferencesKeys.ALWAYS_FREE_BULBS);
		if (bulbArray.length > numberBulbsUnlocked) {
			// tell user to upgrade

		}
		rayAdapter.clear();
		for (int i = 0; i < Math.min(bulbArray.length, numberBulbsUnlocked); i++) {
			// bulbNameList.add(bulb.name);
			Bulb bulb = bulbArray[i];
			bulb.number = i + 1;
			rayAdapter.add(bulb.name);
		}

	}
}
