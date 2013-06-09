package com.kuxhausen.huemore.timing;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
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
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.timing.RepeatDialogFragment.OnRepeatSelectedListener;

public class NewAlarmDialogFragment extends DialogFragment implements
		OnClickListener, LoaderManager.LoaderCallbacks<Cursor>,
		OnRepeatSelectedListener {

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

	private SeekBar brightnessBar;
	private Spinner groupSpinner, moodSpinner, transitionSpinner;
	private SimpleCursorAdapter groupDataSource, moodDataSource;
	private int[] transitionValues;
	private Button repeatButton;
	private TextView repeatView;
	private Gson gson = new Gson();
	private boolean[] repeats = new boolean[7];
	private TimePicker timePick;
	private AlarmState priorState;

	public void onLoadLoaderManager(AlarmState optionalState) {
		if (optionalState != null) {
			this.priorState = optionalState;
		}
		if (groupSpinner != null && moodSpinner != null) {
			/*
			 * Initializes the CursorLoader. The GROUPS_LOADER value is
			 * eventually passed to onCreateLoader().
			 */
			LoaderManager lm = getActivity().getSupportLoaderManager();
			lm.restartLoader(GROUPS_LOADER, null, this);
			lm.restartLoader(MOODS_LOADER, null, this);
			// lm.initLoader(GROUPS_LOADER, null, this);
			// lm.initLoader(MOODS_LOADER, null, this);

			int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
					: android.R.layout.simple_list_item_1;

			String[] gColumns = { GroupColumns.GROUP, BaseColumns._ID };
			groupDataSource = new SimpleCursorAdapter(getActivity(), layout,
					null, gColumns, new int[] { android.R.id.text1 }, 0);
			groupDataSource
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			groupSpinner.setAdapter(groupDataSource);

			String[] mColumns = { MoodColumns.MOOD, BaseColumns._ID };
			moodDataSource = new SimpleCursorAdapter(getActivity(), layout,
					null, mColumns, new int[] { android.R.id.text1 }, 0);
			moodDataSource
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			moodSpinner.setAdapter(moodDataSource);

			if (optionalState != null) {

				// apply initial state
				Log.e("asdf", "" + moodDataSource.getCount());
				int moodPos = 0;
				for (int i = 0; i < moodDataSource.getCount(); i++) {
					if (moodDataSource.getItem(i).equals(optionalState.mood))
						moodPos = i;
					Log.e("asdf", moodDataSource.getItem(i) + "");
					Log.e("asdf", optionalState.mood + "");
				}
				moodSpinner.setSelection(moodPos);

				int groupPos = 0;
				for (int i = 0; i < groupDataSource.getCount(); i++) {
					if (groupDataSource.getItem(i).equals(optionalState.group))
						groupPos = i;
				}
				groupSpinner.setSelection(groupPos);

				int transitionPos = 0;
				for (int i = 0; i < transitionValues.length; i++)
					if (optionalState.transitiontime == transitionValues[i])
						transitionPos = i;
				transitionSpinner.setSelection(transitionPos);

				brightnessBar.setProgress(optionalState.brightness);

				onRepeatSelected(optionalState.getRepeatingDays());

				Log.e("asdf", "apply prior state");
			}
		}
		Log.e("asdf", "loaderManager has priorState" + (priorState != null));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		for (int i = 0; i < repeats.length; i++)
			repeats[i] = false;

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.edit_alarm_dialog, container,
				false);
		Bundle args = getArguments();

		// this.getDialog().setTitle("New Alarm");
		this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		timePick = (TimePicker) myView.findViewById(R.id.alarmTimePicker);

		brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);

		repeatButton = (Button) myView.findViewById(R.id.repeatButton);
		repeatButton.setOnClickListener(this);
		repeatView = (TextView) myView.findViewById(R.id.repeatVisualization);

		groupSpinner = (Spinner) myView.findViewById(R.id.groupSpinner);

		moodSpinner = (Spinner) myView.findViewById(R.id.moodSpinner);

		transitionSpinner = (Spinner) myView
				.findViewById(R.id.transitionSpinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.transition_names_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		transitionSpinner.setAdapter(adapter);

		transitionValues = getActivity().getResources().getIntArray(
				R.array.transition_values_array);

		onLoadLoaderManager(priorState);

		return myView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.repeatButton:
			RepeatDialogFragment rdf = new RepeatDialogFragment();
			rdf.resultListener = this;
			rdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			break;
		case R.id.okay:
			onCreateAlarm();
			this.dismiss();
			break;
		case R.id.cancel:
			if (priorState != null)
				reCreateAlarm();
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
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(cursor);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(cursor);
			}
			break;
		}

		if (priorState != null) {

			// apply initial state
			Log.e("asdf", "" + moodDataSource.getCount());
			int moodPos = 0;
			for (int i = 0; i < moodDataSource.getCount(); i++) {
				if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(
						priorState.mood))
					moodPos = i;
				Log.e("asdf", ((Cursor) moodDataSource.getItem(i)).getString(0)
						+ "");
				Log.e("asdf", priorState.mood + "");
			}
			moodSpinner.setSelection(moodPos);

			int groupPos = 0;
			for (int i = 0; i < groupDataSource.getCount(); i++) {
				if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(
						priorState.group))
					groupPos = i;
			}
			groupSpinner.setSelection(groupPos);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		switch (loader.getId()) {
		case GROUPS_LOADER:
			if (groupDataSource != null) {
				groupDataSource.changeCursor(null);
			}
			break;
		case MOODS_LOADER:
			if (moodDataSource != null) {
				moodDataSource.changeCursor(null);
			}
			break;
		}
	}

	@Override
	public void onRepeatSelected(boolean[] r) {
		repeatView.setText(repeatsToString(getActivity(), r));
		repeats = r;
	}

	public static String repeatsToString(Context c, boolean[] repeats) {
		String result = "";
		String[] days = c.getResources().getStringArray(
				R.array.cap_short_repeat_days);

		boolean all = true;
		boolean none = false;
		for (boolean bool : repeats) {
			all &= bool;
			none |= bool;
		}
		if (all) {
			result = c.getResources().getString(R.string.cap_short_every_day);
		} else if (!none) {
			result = c.getResources().getString(R.string.cap_short_none);
		} else {
			for (int i = 0; i < 7; i++) {
				if (repeats[i])
					result += days[i] + " ";
			}
		}
		return result;
	}

	public void onCreateAlarm() {
		AlarmState as = new AlarmState();
		as.group = ((Cursor) groupSpinner.getSelectedItem()).getString(0);
		as.mood = ((Cursor) moodSpinner.getSelectedItem()).getString(0);
		as.transitiontime = transitionValues[transitionSpinner
				.getSelectedItemPosition()];
		as.brightness = brightnessBar.getProgress();
		as.setRepeatingDays(repeats);
		as.scheduledForFuture = true;

		Calendar projectedTime = Calendar.getInstance();
		projectedTime.setLenient(true);
		projectedTime.set(Calendar.HOUR_OF_DAY, timePick.getCurrentHour());
		projectedTime.set(Calendar.MINUTE, timePick.getCurrentMinute());
		projectedTime.set(Calendar.SECOND, 0);
		// ensure transition starts ahead to culminate at the specified time
		projectedTime.add(Calendar.SECOND, -as.transitiontime / 10);

		if(as.isRepeating()){
			long[] l = new long[7];
			for(int i = 0; i<7; i++)
				l[i]= projectedTime.getTimeInMillis();
			as.setRepeatingTimes(l);
		}else{
			as.setTime(projectedTime.getTimeInMillis());
		}
		AlarmReciever.createAlarms(getActivity(), as);

		// Defines an object to contain the new values to insert
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(DatabaseDefinitions.AlarmColumns.STATE, gson.toJson(as));

		Uri mNewUri = getActivity().getContentResolver().insert(
				DatabaseDefinitions.AlarmColumns.ALARMS_URI, mNewValues);
	}

	private void reCreateAlarm() {
		AlarmState as = AlarmReciever.createAlarms(getActivity(), priorState);
		as.scheduledForFuture = true;

		// Defines an object to contain the new values to insert
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(DatabaseDefinitions.AlarmColumns.STATE, gson.toJson(as));

		Uri mNewUri = getActivity().getContentResolver().insert(
				DatabaseDefinitions.AlarmColumns.ALARMS_URI, mNewValues);
	}

}
