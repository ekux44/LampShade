package com.kuxhausen.huemore.timing;

import com.kuxhausen.huemore.*;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.timing.RepeatDialogFragment.OnRepeatSelectedListener;

import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.database.Cursor;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class NewAlarmDialogFragment extends DialogFragment implements
OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, OnRepeatSelectedListener {

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

	SeekBar brightnessBar;
	Spinner groupSpinner, moodSpinner, transitionSpinner;
	SimpleCursorAdapter groupDataSource, moodDataSource;
	int[] transitionValues;
	Button repeatButton;
	boolean[] repeatDays = new boolean[7];
	TextView repeatView;
	String[] days;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.edit_alarm_dialog, container, false);
		Bundle args = getArguments();

		//this.getDialog().setTitle("New Alarm");
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		
		
		// We need to use a different list item layout for devices older than
				// Honeycomb
				int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
						: android.R.layout.simple_list_item_1;
		
		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		LoaderManager lm = getActivity().getSupportLoaderManager();
		lm.initLoader(GROUPS_LOADER, null, this);
		lm.initLoader(MOODS_LOADER, null, this);
		

		brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
		
		repeatButton = (Button) myView.findViewById(R.id.repeatButton);
		repeatButton.setOnClickListener(this);
		repeatView = (TextView)myView.findViewById(R.id.repeatVisualization);
		
		groupSpinner = (Spinner) myView.findViewById(R.id.groupSpinner);
		String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
		groupDataSource = new SimpleCursorAdapter(getActivity(), layout, null,
				gColumns, new int[] { android.R.id.text1 }, 0);
		groupDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(groupDataSource);
		
		
		moodSpinner = (Spinner) myView.findViewById(R.id.moodSpinner);
		String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
		moodDataSource = new SimpleCursorAdapter(getActivity(), layout, null,
				mColumns, new int[] { android.R.id.text1 }, 0);
		moodDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		moodSpinner.setAdapter(moodDataSource);
		
		transitionSpinner = (Spinner) myView.findViewById(R.id.transitionSpinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.transition_names_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		transitionSpinner.setAdapter(adapter);
		
		
		transitionValues = getActivity().getResources().getIntArray(R.array.transition_values_array);
		days = getActivity().getResources().getStringArray(R.array.cap_short_repeat_days);
		
		return myView;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.repeatButton:
			RepeatDialogFragment rdf = new RepeatDialogFragment();
			rdf.resultListener=this;
			rdf.show(getFragmentManager(), "dialog");
			break;
		case R.id.okay:
			new AlarmReciever(getActivity(),null, 15);
			
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
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
		case GROUPS_LOADER:
			// Returns a new CursorLoader
			String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
					DatabaseDefinitions.GroupColumns.GROUPS_URI, // Table
					gColumns, // Projection to return
					null, // No selection clause
					null, // No selection arguments
					null // Default sort order
			);
		case MOODS_LOADER:
			// Returns a new CursorLoader
			String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
			return new CursorLoader(getActivity(), // Parent activity context
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

	@Override
	public void onRepeatSelected(boolean[] repeats) {
		repeatDays = repeats;
		
		boolean all = true;
		boolean none = false;
		for(boolean bool : repeats){
			all&=bool;
			none|=bool;
		}
		if(all){
			repeatView.setText(getActivity().getResources().getString(R.string.cap_short_every_day));
		}
		else if(!none){
			repeatView.setText(getActivity().getResources().getString(R.string.cap_short_none));
		}
		else{
			String result = "";
			for(int i = 0; i<7; i++){
				if(repeats[i])
					result+=days[i]+" ";
			}
			repeatView.setText(result);
		}
	}

}
