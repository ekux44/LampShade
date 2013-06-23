package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.res.Configuration;
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
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

public class MoodsListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	OnMoodSelectedListener mMoodCallback;

	// Identifies a particular Loader being used in this component
	private static final int MOODS_LOADER = 0;
	public CursorAdapter dataSource;

	public TextView selected, longSelected; // updated on long click
	private int selectedPos = -1;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnMoodSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onMoodSelected(String mood);
	}

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
		getLoaderManager().initLoader(MOODS_LOADER, null, this);

		String[] columns = { MoodColumns.MOOD, BaseColumns._ID };
		dataSource = new SimpleCursorAdapter(this.getActivity(), layout, null,
				columns, new int[] { android.R.id.text1 }, 0);

		setListAdapter(dataSource);

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.mood_view, container, false);

		LinearLayout headingRow = (LinearLayout) myView
				.findViewById(R.id.showOnLandScape);
		setHasOptionsMenu(true);
		return myView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_mood, menu);

		// if ((getResources().getConfiguration().screenLayout &
		// Configuration.SCREENLAYOUT_SIZE_MASK) >=
		// Configuration.SCREENLAYOUT_SIZE_LARGE) {
		// MenuItem unlocksItem = menu.findItem(R.id.action_add_mood);
		// unlocksItem.setEnabled(false);
		// unlocksItem.setVisible(false);

		// }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_add_mood:
			EditMoodPagerDialogFragment nmdf = new EditMoodPagerDialogFragment();
			nmdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			mMoodCallback = (MainActivity) activity;
		} catch (ClassCastException e) {
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		// if (getFragmentManager().findFragmentById(R.id.groups_fragment) !=
		// null) {
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		// }

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
		if (longSelected.getText().equals(PreferencesKeys.OFF)
				|| longSelected.getText().equals(PreferencesKeys.ON)
				|| longSelected.getText().equals(PreferencesKeys.RANDOM)) {
			return;
		}
		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_mood, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextmoodmenu_delete: // <-- your custom menu item id here
			String moodSelect = MoodColumns.MOOD + "=?";
			String[] moodArg = { (String) (longSelected).getText() };
			getActivity().getContentResolver().delete(
					DatabaseDefinitions.MoodColumns.MOODSTATES_URI, moodSelect,
					moodArg);
			return true;
		case R.id.contextmoodmenu_edit: // <-- your custom menu item id here
			EditMoodPagerDialogFragment nmdf = new EditMoodPagerDialogFragment();
			Bundle args = new Bundle();
			args.putString(InternalArguments.MOOD_NAME,
					(String) (longSelected).getText());
			nmdf.setArguments(args);
			nmdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
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
		case MOODS_LOADER:
			// Returns a new CursorLoader
			String[] columns = { MoodColumns.MOOD, BaseColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
					DatabaseDefinitions.MoodColumns.MOODS_URI, // Table
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		selected = ((TextView) (v));
		selectedPos = position;

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(selectedPos, true);

		// Notify the parent activity of selected item
		mMoodCallback.onMoodSelected((String) ((TextView) (v)).getText());

	}

}
