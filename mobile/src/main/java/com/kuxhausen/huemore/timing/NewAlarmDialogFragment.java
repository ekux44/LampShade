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
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
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

public class NewAlarmDialogFragment extends DialogFragment implements OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor>, OnRepeatSelectedListener {

  // Identifies a particular Loader being used in this component
  private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

  private SeekBar brightnessBar;
  private Spinner groupSpinner, moodSpinner;
  private SimpleCursorAdapter groupDataSource, moodDataSource;
  private Button repeatButton;
  private TextView repeatView;
  private Gson gson = new Gson();
  private boolean[] repeats = new boolean[7];
  private TimePicker timePick;
  private DatabaseAlarm priorState;

  public void onLoadLoaderManager(DatabaseAlarm optionalState) {
    if (optionalState != null)
      this.priorState = optionalState;
    if (groupSpinner != null && moodSpinner != null) {
      /*
       * Initializes the CursorLoader. The GROUPS_LOADER value is eventually passed to
       * onCreateLoader().
       */
      LoaderManager lm = getActivity().getSupportLoaderManager();
      lm.restartLoader(GROUPS_LOADER, null, this);
      lm.restartLoader(MOODS_LOADER, null, this);
      // lm.initLoader(GROUPS_LOADER, null, this);
      // lm.initLoader(MOODS_LOADER, null, this);

      int layout =
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
              : android.R.layout.simple_list_item_1;

      String[] gColumns = {GroupColumns.GROUP, BaseColumns._ID};
      groupDataSource =
          new SimpleCursorAdapter(getActivity(), layout, null, gColumns,
              new int[] {android.R.id.text1}, 0);
      groupDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      groupSpinner.setAdapter(groupDataSource);

      String[] mColumns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID};
      moodDataSource =
          new SimpleCursorAdapter(getActivity(), layout, null, mColumns,
              new int[] {android.R.id.text1}, 0);
      moodDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      moodSpinner.setAdapter(moodDataSource);

      if (optionalState != null) {
        // apply initial state
        int moodPos = 0;
        for (int i = 0; i < moodDataSource.getCount(); i++) {
          if (moodDataSource.getItem(i).equals(optionalState.getAlarmState().mood))
            moodPos = i;
        }
        moodSpinner.setSelection(moodPos);

        int groupPos = 0;
        for (int i = 0; i < groupDataSource.getCount(); i++) {
          if (groupDataSource.getItem(i).equals(optionalState.getAlarmState().group))
            groupPos = i;
        }
        groupSpinner.setSelection(groupPos);


        brightnessBar.setProgress(optionalState.getAlarmState().brightness);

        onRepeatSelected(optionalState.getAlarmState().getRepeatingDays());

        Calendar projectedTime = Calendar.getInstance();
        if (optionalState.getAlarmState().isRepeating()) {
          for (int i = 0; i < optionalState.getAlarmState().getRepeatingDays().length; i++) {
            if (optionalState.getAlarmState().getRepeatingDays()[i])
              projectedTime.setTimeInMillis(optionalState.getAlarmState().getRepeatingTimes()[i]);
          }
        } else {
          projectedTime.setTimeInMillis(optionalState.getAlarmState().getTime());
        }
        timePick.setCurrentHour(projectedTime.get(Calendar.HOUR_OF_DAY));
        timePick.setCurrentMinute(projectedTime.get(Calendar.MINUTE));
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    for (int i = 0; i < repeats.length; i++)
      repeats[i] = false;

    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.edit_alarm_dialog, container, false);

    this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);
    Button okayButton = (Button) myView.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    timePick = (TimePicker) myView.findViewById(R.id.alarmTimePicker);
    timePick.setIs24HourView(DateFormat.is24HourFormat(this.getActivity()));

    brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);

    repeatButton = (Button) myView.findViewById(R.id.repeatButton);
    repeatButton.setOnClickListener(this);
    repeatView = (TextView) myView.findViewById(R.id.repeatVisualization);

    groupSpinner = (Spinner) myView.findViewById(R.id.groupSpinner);

    moodSpinner = (Spinner) myView.findViewById(R.id.moodSpinner);

    onLoadLoaderManager(priorState);

    return myView;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.repeatButton:
        RepeatDialogFragment rdf = new RepeatDialogFragment();
        rdf.resultListener = this;
        rdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        break;
      case R.id.okay:
        onCreateAlarm();
        this.dismiss();
        break;
      case R.id.cancel:
        this.dismiss();
        break;
    }
  }

  /**
   * Callback that's invoked when the system has initialized the Loader and is ready to start the
   * query. This usually happens when initLoader() is called. The loaderID argument contains the ID
   * value passed to the initLoader() call.
   */
  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    /*
     * Takes action based on the ID of the Loader that's being created
     */
    switch (loaderID) {
      case GROUPS_LOADER:
        // Returns a new CursorLoader
        String[] gColumns = {GroupColumns.GROUP, BaseColumns._ID};
        return new CursorLoader(getActivity(), // Parent activity context
            DatabaseDefinitions.GroupColumns.GROUPS_URI, // Table
            gColumns, // Projection to return
            null, // No selection clause
            null, // No selection arguments
            null // Default sort order
        );
      case MOODS_LOADER:
        // Returns a new CursorLoader
        String[] mColumns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID};
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
     * Moves the query results into the adapter, causing the ListView fronting this adapter to
     * re-display
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
      int moodPos = 0;
      for (int i = 0; i < moodDataSource.getCount(); i++) {
        if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(
            priorState.getAlarmState().mood))
          moodPos = i;
      }
      moodSpinner.setSelection(moodPos);

      int groupPos = 0;
      for (int i = 0; i < groupDataSource.getCount(); i++) {
        if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(
            priorState.getAlarmState().group))
          groupPos = i;
      }
      groupSpinner.setSelection(groupPos);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
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
    String[] days = c.getResources().getStringArray(R.array.cap_short_repeat_days);

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
    if (priorState != null) {
      // delete old one
      priorState.delete();
    }

    AlarmState as = new AlarmState();
    as.group = ((Cursor) groupSpinner.getSelectedItem()).getString(0);
    as.mood = ((Cursor) moodSpinner.getSelectedItem()).getString(0);
    as.brightness = brightnessBar.getProgress();
    as.setRepeatingDays(repeats);
    as.setScheduledForFuture(true);

    Calendar projectedTime = Calendar.getInstance();
    projectedTime.setLenient(true);
    projectedTime.set(Calendar.HOUR_OF_DAY, timePick.getCurrentHour());
    projectedTime.set(Calendar.MINUTE, timePick.getCurrentMinute());
    projectedTime.set(Calendar.SECOND, 0);

    if (as.isRepeating()) {
      long[] l = new long[7];
      for (int i = 0; i < 7; i++)
        l[i] = projectedTime.getTimeInMillis();
      as.setRepeatingTimes(l);
    } else {
      as.setTime(projectedTime.getTimeInMillis());
    }
    // Defines an object to contain the new values to insert
    ContentValues mNewValues = new ContentValues();
    mNewValues.put(DatabaseDefinitions.AlarmColumns.STATE, gson.toJson(as));

    Uri locationOfNewAlarm =
        getActivity().getContentResolver().insert(DatabaseDefinitions.AlarmColumns.ALARMS_URI,
            mNewValues);

    DatabaseAlarm ar = new DatabaseAlarm(getActivity(), locationOfNewAlarm);
    AlarmReciever.createAlarms(getActivity(), ar);
  }

}
