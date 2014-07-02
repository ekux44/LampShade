package com.kuxhausen.huemore.nfc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;


public class NfcWriterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
    OnCheckedChangeListener, OnClickListener {

  private Button sendButton;
  private NfcAdapter nfcAdapter;
  PendingIntent pendingIntent;
  IntentFilter writeTagFilters[];
  boolean writeMode;

  NavigationDrawerActivity context;

  Gson gson = new Gson();

  // Identifies a particular Loader being used in this component
  private static final int GROUPS_LOADER = 0, MOODS_LOADER = 1;

  private SeekBar brightnessBar;
  private CheckBox brightnessCheckBox;
  private TextView brightnessDescripterTextView;
  private Spinner groupSpinner, moodSpinner;
  private SimpleCursorAdapter groupDataSource, moodDataSource;

  private GroupMoodBrightness priorGMB;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View myView = inflater.inflate(R.layout.nfc_writer, container, false);

    context = (NavigationDrawerActivity) this.getActivity();

    // TODO deal with NFC not supported on this device
    /*
     * if (NfcAdapter.getDefaultAdapter(this) == null) { // hide nfc link if nfc not supported
     * MenuItem nfcItem = menu.findItem(R.id.action_nfc); if (nfcItem != null) {
     * nfcItem.setEnabled(false); nfcItem.setVisible(false); }
     */


    // We need to use a different list item layout for devices older than Honeycomb
    int layout =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
            : android.R.layout.simple_list_item_1;

    brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
    brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        preview();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
    });

    brightnessDescripterTextView =
        (TextView) myView.findViewById(R.id.brightnessDescripterTextView);

    brightnessCheckBox = (CheckBox) myView.findViewById(R.id.includeBrightnessCheckBox);
    brightnessCheckBox.setOnCheckedChangeListener(this);

    groupSpinner = (Spinner) myView.findViewById(R.id.groupSpinner);
    String[] gColumns = {GroupColumns.GROUP, BaseColumns._ID};
    groupDataSource =
        new SimpleCursorAdapter(context, layout, null, gColumns, new int[] {android.R.id.text1}, 0);
    groupDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    groupSpinner.setAdapter(groupDataSource);

    moodSpinner = (Spinner) myView.findViewById(R.id.moodSpinner);
    String[] mColumns = {MoodColumns.MOOD, BaseColumns._ID};
    moodDataSource =
        new SimpleCursorAdapter(context, layout, null, mColumns, new int[] {android.R.id.text1}, 0);
    moodDataSource.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    moodSpinner.setAdapter(moodDataSource);

    nfcAdapter = NfcAdapter.getDefaultAdapter(context);

    sendButton = (Button) myView.findViewById(R.id.writeToTagButton);
    sendButton.setOnClickListener(this);

    pendingIntent =
        PendingIntent.getActivity(context, 0,
            new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
    tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
    writeTagFilters = new IntentFilter[] {tagDetected};

    return myView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.action_write_nfc, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        context.onBackPressed();
        return true;
      case R.id.action_help:
        context.showHelp(this.getResources().getString(R.string.help_title_nfc));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.writeToTagButton:
        preview();
        try {
          if (context.myTag == null) {
            Toast.makeText(context, context.getString(R.string.nfc_tag_not_detected),
                Toast.LENGTH_SHORT).show();
          } else {
            write(getSerializedByValue(), context.myTag);
            Toast.makeText(context, context.getString(R.string.nfc_tag_write_success),
                Toast.LENGTH_SHORT).show();
          }
        } catch (IOException e) {
          Toast.makeText(context, context.getString(R.string.nfc_tag_write_fail),
              Toast.LENGTH_SHORT).show();
          e.printStackTrace();
        } catch (FormatException e) {
          Toast.makeText(context, context.getString(R.string.nfc_tag_write_fail),
              Toast.LENGTH_SHORT).show();
          e.printStackTrace();
        }
        break;
    }
  }

  private void write(String text, Tag tag) throws IOException, FormatException {
    try {
      NdefRecord[] records = {createRecord(text)};
      NdefMessage message = new NdefMessage(records);
      // Get an instance of Ndef for the tag.
      Ndef ndef = Ndef.get(tag);
      // Enable I/O
      ndef.connect();
      // Write the message
      ndef.writeNdefMessage(message);
      // Close the connection
      ndef.close();
    } catch (java.lang.NullPointerException e) {
      Toast.makeText(context, context.getString(R.string.nfc_tag_not_supported), Toast.LENGTH_LONG)
          .show();
    }
  }

  private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
    byte[] uriField = text.getBytes(Charset.forName("US-ASCII"));
    byte[] payload = new byte[uriField.length + 1]; // add 1 for the URI
    // Prefix
    payload[0] = 0x01; // prefixes http://www. to the URI
    System.arraycopy(uriField, 0, payload, 1, uriField.length); // appends
    // URI to
    // payload
    NdefRecord rtdUriRecord =
        new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);

    return rtdUriRecord;
  }

  @Override
  public void onPause() {
    super.onPause();
    WriteModeOff();
  }

  @Override
  public void onResume() {
    super.onResume();
    WriteModeOn();

    LoaderManager lm = context.getSupportLoaderManager();
    lm.initLoader(GROUPS_LOADER, null, this);
    lm.initLoader(MOODS_LOADER, null, this);
    
    setHasOptionsMenu(true);
  }

  private void WriteModeOn() {
    writeMode = true;
    if (nfcAdapter != null)
      nfcAdapter.enableForegroundDispatch(context, pendingIntent, writeTagFilters, null);
  }

  private void WriteModeOff() {
    writeMode = false;
    if (nfcAdapter != null)
      nfcAdapter.disableForegroundDispatch(context);
  }

  public void preview() {

    String groupName = ((TextView) groupSpinner.getSelectedView()).getText().toString();
    Group g = Group.loadFromDatabase(groupName, context);

    String moodName = ((TextView) moodSpinner.getSelectedView()).getText().toString();
    Mood m = Utils.getMoodFromDatabase(moodName, context);

    Integer brightness = null;
    if (brightnessBar.getVisibility() == View.VISIBLE)
      brightness = brightnessBar.getProgress();

    context.getService().getMoodPlayer().playMood(g, m, moodName, brightness, null);
  }

  public String getSerializedByValue() {
    String url = "lampshade.io/nfc?";


    Group g =
        Group.loadFromDatabase(((TextView) groupSpinner.getSelectedView()).getText().toString(),
            context);

    Mood m =
        Utils.getMoodFromDatabase(((TextView) moodSpinner.getSelectedView()).getText().toString(),
            context);

    Integer brightness = null;
    if (brightnessBar.getVisibility() == View.VISIBLE)
      brightness = brightnessBar.getProgress();

    String data = HueUrlEncoder.encode(m, g, brightness, context);
    return url + data;
  }


  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    switch (loaderID) {
      case GROUPS_LOADER:
        String[] gColumns = {GroupColumns.GROUP, BaseColumns._ID};
        return new CursorLoader(context, GroupColumns.GROUPS_URI, gColumns, null, null, null);
      case MOODS_LOADER:
        String[] mColumns = {MoodColumns.MOOD, BaseColumns._ID};
        return new CursorLoader(context, DatabaseDefinitions.MoodColumns.MOODS_URI, mColumns, null,
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
        if (((Cursor) moodDataSource.getItem(i)).getString(0).equals(priorGMB.mood))
          moodPos = i;
      }
      moodSpinner.setSelection(moodPos);

      int groupPos = 0;
      for (int i = 0; i < groupDataSource.getCount(); i++) {
        if (((Cursor) groupDataSource.getItem(i)).getString(0).equals(priorGMB.group))
          groupPos = i;
      }
      groupSpinner.setSelection(groupPos);
      if (priorGMB.brightness != null)
        brightnessBar.setProgress(priorGMB.brightness);
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
