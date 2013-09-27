package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.Utils;

public class MoodListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	OnMoodSelectedListener mMoodCallback;

	// Identifies a particular Loader being used in this component
	private static final int MOODS_LOADER = 0;
	public CursorAdapter dataSource;

	public TextView selected, longSelected; // updated on long click
	private int selectedPos = -1; 
	private ShareActionProvider mShareActionProvider;

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

		setHasOptionsMenu(true);
		getSherlockActivity().supportInvalidateOptionsMenu();
		return myView;
	}

	/** Returns a share intent */
    private Intent getDefaultShareIntent(String mood){
 
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT");
        intent.putExtra(Intent.EXTRA_TEXT,mood+" #LampShadeIO http://lampshade.io/share?"+HueUrlEncoder.encode(Utils.getMoodFromDatabase(mood, this.getActivity())));
        return intent;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_mood, menu);

		if ((getResources().getConfiguration().screenLayout &
					Configuration.SCREENLAYOUT_SIZE_MASK) >=
					Configuration.SCREENLAYOUT_SIZE_LARGE) {
			MenuItem unlocksItem = menu.findItem(R.id.action_add_mood);
			unlocksItem.setEnabled(false);
			unlocksItem.setVisible(false);
		}
		if (selectedPos>-1 
				&& selected!=null
				&& !selected.getText().equals(this.getActivity().getString(R.string.cap_off))
				&& !selected.getText().equals(this.getActivity().getString(R.string.cap_on))
				&& !selected.getText().equals(this.getActivity().getString(R.string.cap_random))) {
			/** Getting the actionprovider associated with the menu item whose id is share */
			mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
	
			/** Getting the target intent */
			Intent intent = getDefaultShareIntent(""+selected.getText());
	
			/** Setting a share intent */
			if(intent!=null)
					mShareActionProvider.setShareIntent(intent);
		}else{
			MenuItem shareItem = menu.findItem(R.id.action_share);
			shareItem.setEnabled(false);
			shareItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_add_mood:
			Intent i = new Intent(this.getActivity(), EditMoodPagerDialogFragment.class);
			i.putExtra(InternalArguments.SERIALIZED_GOD_OBJECT, ((GodObject)this.getActivity()).getSerialized());
			this.getActivity().startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMoodCallback = (GodObject) activity;
	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
	}
	@Override
	public void onResume(){
		super.onResume();
		this.invalidateSelection();
	}

	public void invalidateSelection() {
		// Set the previous selected item as checked to be unhighlighted when in
		// two-pane layout
		if (selected != null && selectedPos > -1)
			getListView().setItemChecked(selectedPos, false);
		selectedPos = -1;
		selected = null;
		getSherlockActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		longSelected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
		if (longSelected.getText().equals(this.getActivity().getString(R.string.cap_off))
				|| longSelected.getText().equals(this.getActivity().getString(R.string.cap_on))
				|| longSelected.getText().equals(this.getActivity().getString(R.string.cap_random))) {
			return;
		}
		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_mood, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {

		case R.id.contextmoodmenu_delete:
			String moodSelect = MoodColumns.MOOD + "=?";
			String[] moodArg = { (String) (longSelected).getText() };
			getActivity().getContentResolver().delete(
					DatabaseDefinitions.MoodColumns.MOODS_URI, moodSelect,
					moodArg);
			return true;
		case R.id.contextmoodmenu_edit:
			Intent i = new Intent(this.getActivity(), EditMoodPagerDialogFragment.class);
			i.putExtra(InternalArguments.SERIALIZED_GOD_OBJECT, ((GodObject)this.getActivity()).getSerialized());
			i.putExtra(InternalArguments.MOOD_NAME, (String) (longSelected).getText());
			this.getActivity().startActivity(i);
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
		getSherlockActivity().supportInvalidateOptionsMenu();
	}

}
