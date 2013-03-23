package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.Context;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.HueState;

public class MoodsFragment extends ListFragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	OnMoodSelectedListener mMoodCallback;

	final static String ARG_GROUP = "group";
	String mCurrentGroup = null;
	public Context parrentActivity;
	// Identifies a particular Loader being used in this component
	private static final int MOODS_LOADER = 0;
	public CursorAdapter dataSource;
	SeekBar brightnessBar;

	int brightness;
	public TextView selected; // updated on long click

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnMoodSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onMoodSelected(String mood);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		parrentActivity = this.getActivity();

		// If activity recreated (such as from screen rotate), restore
		// the previous article selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentGroup = savedInstanceState.getString(ARG_GROUP);
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

		String[] columns = { MoodColumns.MOOD, BaseColumns._ID };
		dataSource = new SimpleCursorAdapter(this.getActivity(), layout, null,
				columns, new int[] { android.R.id.text1 }, 0);

		setListAdapter(dataSource);

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.mood_view, container, false);

		brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				HueState hs = new HueState();
				hs.bri = brightness;
				hs.on = true;
				Gson gs = new Gson();
				String[] brightnessState = { gs.toJson(hs) };
				// TODO deal with off?
				((MainActivity) parrentActivity)
						.onBrightnessChanged(brightnessState);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				brightness = progress;
			}
		});

		ImageButton newGroup = (ImageButton) myView
				.findViewById(R.id.newMoodButton);
		newGroup.setOnClickListener(this);

		return myView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			mMoodCallback = (MainActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
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

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			updateGroupView(args.getString(ARG_GROUP));
		} else if (mCurrentGroup != null) {
			// Set article based on saved instance state defined during
			// onCreateView
			updateGroupView(mCurrentGroup);
		}
	}

	public void updateGroupView(String group) {
		// TextView article = (TextView)
		// getActivity().findViewById(R.id.article);
		// article.setText(StaticDataStore.Moods[position]);
		
		mCurrentGroup = group;
		// Set the previous selected item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(getListView().getPositionForView(selected), true);
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putString(ARG_GROUP, mCurrentGroup);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		selected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
		if (selected.getText().equals("OFF")) {
			return;
		}
		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.mood_fragment, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.contextmoodmenu_delete: // <-- your custom menu item id here
			String moodSelect = MoodColumns.MOOD + "=?";
			String[] moodArg = { (String) (selected).getText() };
			getActivity().getContentResolver().delete(
					DatabaseDefinitions.MoodColumns.MOODSTATES_URI, moodSelect,
					moodArg);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.newMoodButton:

			NewMoodDialogFragment nmdf = new NewMoodDialogFragment();
			nmdf.show(getFragmentManager(), "dialog");

			break;
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

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(position, true);

		// Notify the parent activity of selected item
		mMoodCallback.onMoodSelected((String) ((TextView) (v)).getText());

	}

}
