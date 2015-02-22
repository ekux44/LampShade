package com.kuxhausen.huemore.automation;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.kuxhausen.huemore.Helpers;
import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.DeprecatedGroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;

public class EditActivity extends NetworkManagedActivity implements
                                                         LoaderManager.LoaderCallbacks<Cursor>,
                                                         OnCheckedChangeListener, OnClickListener {

  // don't change value
  protected static final String EXTRA_BUNDLE_SERIALIZED_BY_NAME =
      "com.kuxhausen.huemore.EXTRA_BUNDLE_SERIALIZED_BY_NAME";

  protected static final String PERCENT_BRIGHTNESS_KEY = "com.kuxhausen.huemore.PERCENT_BRIGHTNESS";
  protected static final String PERCENT_BRIGHTNESS_VALUE = "%percentbrightness";

  protected static final String MOOD_NAME_KEY = "com.kuxhausen.huemore.MOOD_NAME";
  protected static final String MOOD_NAME_VALUE = "%moodname";

  protected static final String TASKER_VARIABLE_TARGETS_KEY =
      "net.dinglisch.android.tasker.extras.VARIABLE_REPLACE_KEYS";
  protected static final String TASKER_VARIABLE_TARGETS_VALUE = PERCENT_BRIGHTNESS_KEY + " "
                                                                + MOOD_NAME_KEY;

  private Button okayButton, cancelButton;

  Context context;
  Gson gson = new Gson();

  // Identifies a particular Loader being used in this component
  private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

  private SeekBar brightnessBar;
  private CheckBox brightnessCheckBox;
  private TextView brightnessDescripterTextView;
  private Spinner groupSpinner, moodSpinner;
  private SimpleCursorAdapter groupDataSource, moodDataSource;

  private LegacyGMB priorGMB;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Helpers.applyLocalizationPreference(this);

    setContentView(R.layout.edit_automation);
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    // check full version unlocked
    Bundle b = this.getIntent().getExtras();
    if (b != null
        && b.containsKey(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE)
        && b.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE).containsKey(
        EXTRA_BUNDLE_SERIALIZED_BY_NAME)) {
      setSerializedByName(b.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE).getString(
          EXTRA_BUNDLE_SERIALIZED_BY_NAME));
    }

    okayButton = (Button) this.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    cancelButton = (Button) this.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);

    // We need to use a different list item layout for devices older than Honeycomb
    int layout =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        ? android.R.layout.simple_list_item_activated_1
        : android.R.layout.simple_list_item_1;

    LoaderManager lm = getSupportLoaderManager();
    lm.initLoader(GROUPS_LOADER, null, this);
    lm.initLoader(MOODS_LOADER, null, this);

    brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
    brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // preview();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      }
    });

    brightnessDescripterTextView = (TextView) this.findViewById(R.id.brightnessDescripterTextView);

    brightnessCheckBox = (CheckBox) this.findViewById(R.id.includeBrightnessCheckBox);
    brightnessCheckBox.setOnCheckedChangeListener(this);

    groupSpinner = (Spinner) this.findViewById(R.id.groupSpinner);
    String[] gColumns = {DeprecatedGroupColumns.GROUP, BaseColumns._ID};
    groupDataSource =
        new SimpleCursorAdapter(this, layout, null, gColumns, new int[]{android.R.id.text1}, 0);
    groupDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    groupSpinner.setAdapter(groupDataSource);

    moodSpinner = (Spinner) this.findViewById(R.id.moodSpinner);
    String[] mColumns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID};
    moodDataSource =
        new SimpleCursorAdapter(this, layout, null, mColumns, new int[]{android.R.id.text1}, 0);
    moodDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    moodSpinner.setAdapter(moodDataSource);

    this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.okay:
        Intent i = new Intent();
        i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
                   getSerializedByNamePreview());
        Bundle b = new Bundle();
        b.putString(EXTRA_BUNDLE_SERIALIZED_BY_NAME, getSerializedByName());
        i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, b);
        i.putExtra(TASKER_VARIABLE_TARGETS_KEY, TASKER_VARIABLE_TARGETS_VALUE);
        i.putExtra(PERCENT_BRIGHTNESS_KEY, PERCENT_BRIGHTNESS_VALUE);
        i.putExtra(MOOD_NAME_KEY, MOOD_NAME_VALUE);
        setResult(Activity.RESULT_OK, i);
        super.finish();
        break;
      case R.id.cancel:
        setResult(Activity.RESULT_CANCELED);
        super.finish();
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        this.startActivity(new Intent(this, NavigationDrawerActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public String getSerializedByNamePreview() {
    LegacyGMB gmb = new LegacyGMB();
    gmb.group = ((TextView) groupSpinner.getSelectedView()).getText().toString();
    gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText().toString();
    if (brightnessBar.getVisibility() == View.VISIBLE) {
      gmb.brightness = brightnessBar.getProgress();
    }

    String preview = gmb.group + " \u2192 " + gmb.mood;
    if (brightnessBar.getVisibility() == View.VISIBLE) {
      preview += " @ " + ((gmb.brightness * 100) / 255) + "%";
    }
    return preview;
  }

  public void setSerializedByName(String s) {
    priorGMB = gson.fromJson(s, LegacyGMB.class);

  }

  public String getSerializedByName() {
    LegacyGMB gmb = new LegacyGMB();
    gmb.group = ((TextView) groupSpinner.getSelectedView()).getText().toString();
    gmb.mood = ((TextView) moodSpinner.getSelectedView()).getText().toString();
    if (brightnessBar.getVisibility() == View.VISIBLE) {
      gmb.brightness = brightnessBar.getProgress();
    }
    return gson.toJson(gmb);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    switch (loaderID) {
      case GROUPS_LOADER:
        String[] gColumns = {DeprecatedGroupColumns.GROUP, BaseColumns._ID};
        return new CursorLoader(this, DeprecatedGroupColumns.GROUPS_URI, gColumns, null, null,
                                null);
      case MOODS_LOADER:
        String[] mColumns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID};
        return new CursorLoader(this, MoodColumns.MOODS_URI, mColumns, null,
                                null, null);
      default:
        return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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

    if (priorGMB != null) {

      // apply prior state
      int moodPos = 0;
      for (int i = 0; i < moodDataSource.getCount(); i++) {
        if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(priorGMB.mood)) {
          moodPos = i;
        }
      }
      moodSpinner.setSelection(moodPos);

      int groupPos = 0;
      for (int i = 0; i < groupDataSource.getCount(); i++) {
        if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(priorGMB.group)) {
          groupPos = i;
        }
      }
      groupSpinner.setSelection(groupPos);
      if (priorGMB.brightness != null) {
        brightnessBar.setProgress(priorGMB.brightness);
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    // unregisterForContextMenu(getListView());
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
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (isChecked) {
      brightnessBar.setVisibility(View.VISIBLE);
      brightnessDescripterTextView.setVisibility(View.VISIBLE);
    } else {
      brightnessBar.setVisibility(View.INVISIBLE);
      brightnessDescripterTextView.setVisibility(View.INVISIBLE);
    }
  }
}
