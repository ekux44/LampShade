package com.kuxhausen.huemore.editmood;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kuxhausen.huemore.MoodRow;
import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;

public class EditMoodFragment extends Fragment implements OnItemSelectedListener,
                                                          OnCheckedChangeListener {

  private NavigationDrawerActivity parrentA;

  EditMoodStateGridFragment stateGridFragment;

  private EditText nameEditText;
  private Spinner moodTypeSpinner;

  String priorName;
  Mood priorMood;
  Gson gson = new Gson();
  private CheckBox loop;

  public interface OnCreateMoodListener {

    public void preview();

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.edit_mood_activity, null);

    parrentA = (NavigationDrawerActivity) this.getActivity();

    parrentA.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    // Inflate the custom view
    nameEditText =
        (EditText) LayoutInflater.from(parrentA).inflate(R.layout.mood_name_edit_text, null);

    loop = (CheckBox) myView.findViewById(R.id.loopCheckBox);
    loop.setOnCheckedChangeListener(this);
    moodTypeSpinner = (Spinner) myView.findViewById(R.id.moodTypeSpinner);
    moodTypeSpinner.setOnItemSelectedListener(this);

    // If we're being restored from a previous state,
    // then we don't need to do anything and should return or else
    // we could end up with overlapping fragments.
    if (savedInstanceState != null) {
      stateGridFragment =
          (EditMoodStateGridFragment) parrentA.getSupportFragmentManager().findFragmentById(
              R.id.edit_mood_fragment_container);
      stateGridFragment.setParentFragment(this);
    } else {
      stateGridFragment = new EditMoodStateGridFragment();
      stateGridFragment.setParentFragment(this);

      // In case this activity was started with special instructions from an Intent, pass the
      // Intent's extras to the fragment as arguments
      stateGridFragment.setArguments(getArguments());

      // Add the fragment to the 'fragment_container' FrameLayout
      getChildFragmentManager()
          .beginTransaction()
          .add(R.id.edit_mood_fragment_container, stateGridFragment,
               EditMoodStateGridFragment.class.getName()).commit();
    }

    Bundle args = getArguments();
    if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
      String moodName = args.getString(InternalArguments.MOOD_NAME);
      priorName = moodName;
      nameEditText.setText(moodName);

      priorMood = Utils.getMoodFromDatabase(moodName, parrentA);

      moodTypeSpinner
          .setSelection(EditMoodStateGridFragment.calculateMoodType(priorMood).ordinal());
      if (moodTypeSpinner.getSelectedItemPosition()
          == EditMoodStateGridFragment.PageType.RELATIVE_PAGE
          .ordinal()) {
        setChecked(true);
      } else {
        setChecked(false);
      }

    } else {
      priorName = null;
      priorMood = null;
    }
    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    this.setHasOptionsMenu(true);

    parrentA.getSupportActionBar().setCustomView(nameEditText);
    parrentA.getSupportActionBar().setDisplayShowCustomEnabled(true);
    parrentA.getSupportActionBar().setDisplayShowTitleEnabled(false);
  }


  @Override
  public void onPause() {
    super.onPause();
    parrentA.getSupportActionBar().setDisplayShowCustomEnabled(false);
    parrentA.getSupportActionBar().setDisplayShowTitleEnabled(true);
  }

  public String getName() {
    if (nameEditText != null) {
      return nameEditText.getText().toString();
    }
    return "";
  }

  public boolean isChecked() {
    if (loop != null) {
      return loop.isChecked();
    }
    return false;
  }

  public void setChecked(boolean check) {
    if (loop != null) {
      loop.setChecked(check);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.action_edit_mood, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        parrentA.onBackPressed();
        return true;
      case R.id.action_play:
        stateGridFragment.preview();
        return true;
      case R.id.action_help:
        parrentA.showHelp(this.getResources().getString(R.string.help_title_editingmoods));
        return true;
      case R.id.action_save:

        String moodName = nameEditText.getText().toString();
        if (moodName == null || moodName.length() < 1) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentA);
          int unnamedNumber = 1 + settings.getInt(PreferenceKeys.UNNAMED_MOOD_NUMBER, 0);
          Editor edit = settings.edit();
          edit.putInt(PreferenceKeys.UNNAMED_MOOD_NUMBER, unnamedNumber);
          edit.commit();
          moodName = this.getResources().getString(R.string.unnamed_mood) + " " + unnamedNumber;
        }

        ContentValues mNewValues = new ContentValues();
        mNewValues.put(MoodColumns.COL_MOOD_NAME, moodName);
        mNewValues.put(MoodColumns.COL_MOOD_LOWERCASE_NAME, moodName.toLowerCase().trim());
        mNewValues
            .put(MoodColumns.COL_MOOD_VALUE, HueUrlEncoder.encode(stateGridFragment.getMood()));

        if (priorName != null) {
          // modify existing mood
          String moodSelect = MoodColumns.COL_MOOD_NAME + "=?";
          String[] moodArg = {priorName};
          parrentA.getContentResolver()
              .update(MoodColumns.MOODS_URI, mNewValues, moodSelect, moodArg);

          //now remember new mood name
          priorName = moodName;
        } else {

          mNewValues.put(MoodColumns.COL_MOOD_PRIORITY, MoodRow.UNSTARRED_PRIORITY);
          parrentA.getContentResolver().insert(MoodColumns.MOODS_URI, mNewValues);
        }

        Toast t =
            Toast.makeText(parrentA, parrentA.getResources().getString(R.string.saved) + " "
                                     + moodName, Toast.LENGTH_SHORT);
        t.show();
        parrentA.onBackPressed();
        return true;
    }
    return false;
  }


  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    // TODO Auto-generated method stub
    if (stateGridFragment != null) {
      stateGridFragment.setMoodMode(position);
    }
    if (position == EditMoodStateGridFragment.PageType.RELATIVE_PAGE.ordinal()) {
      loop.setVisibility(View.VISIBLE);
    } else {
      loop.setVisibility(View.INVISIBLE);
    }
  }


  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub

  }


  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (stateGridFragment != null) {
      stateGridFragment.redrawGrid();
    }
  }
}
