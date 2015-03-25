package com.kuxhausen.huemore.editmood;

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

  private NavigationDrawerActivity mParent;
  private EditMoodStateGridFragment mStateGridFragment;
  private EditText mNameEditText;
  private Spinner mMoodTypeSpinner;
  private String mPriorName;
  private Mood mPriorMood;
  private CheckBox mLoop;

  public interface OnCreateMoodListener {

    public void preview();

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.edit_mood_activity, null);

    mParent = (NavigationDrawerActivity) this.getActivity();

    // Inflate the custom view
    mNameEditText =
        (EditText) LayoutInflater.from(mParent).inflate(R.layout.mood_name_edit_text, null);

    mLoop = (CheckBox) myView.findViewById(R.id.loopCheckBox);
    mLoop.setOnCheckedChangeListener(this);
    mMoodTypeSpinner = (Spinner) myView.findViewById(R.id.moodTypeSpinner);
    mMoodTypeSpinner.setOnItemSelectedListener(this);

    // If we're being restored from a previous state,
    // then we don't need to do anything and should return or else
    // we could end up with overlapping fragments.
    if (savedInstanceState != null) {
      mStateGridFragment =
          (EditMoodStateGridFragment) mParent.getSupportFragmentManager().findFragmentById(
              R.id.edit_mood_fragment_container);
      mStateGridFragment.setParentFragment(this);
    } else {
      mStateGridFragment = new EditMoodStateGridFragment();
      mStateGridFragment.setParentFragment(this);

      // In case this activity was started with special instructions from an Intent, pass the
      // Intent's extras to the fragment as arguments
      mStateGridFragment.setArguments(getArguments());

      // Add the fragment to the 'fragment_container' FrameLayout
      getChildFragmentManager()
          .beginTransaction()
          .add(R.id.edit_mood_fragment_container, mStateGridFragment,
               EditMoodStateGridFragment.class.getName()).commit();
    }

    Bundle args = getArguments();
    if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
      String moodName = args.getString(InternalArguments.MOOD_NAME);
      mPriorName = moodName;
      mNameEditText.setText(moodName);

      mPriorMood = Utils.getMoodFromDatabase(moodName, mParent);

      mMoodTypeSpinner
          .setSelection(EditMoodStateGridFragment.calculateMoodType(mPriorMood).ordinal());
      if (mMoodTypeSpinner.getSelectedItemPosition()
          == EditMoodStateGridFragment.PageType.RELATIVE_PAGE
          .ordinal()) {
        setChecked(true);
      } else {
        setChecked(false);
      }

    } else {
      mPriorName = null;
      mPriorMood = null;
    }
    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    this.setHasOptionsMenu(true);

    mParent.getSupportActionBar().setCustomView(mNameEditText);
    mParent.getSupportActionBar().setDisplayShowCustomEnabled(true);
    mParent.getSupportActionBar().setDisplayShowTitleEnabled(false);
  }


  @Override
  public void onPause() {
    super.onPause();
    mParent.getSupportActionBar().setDisplayShowCustomEnabled(false);
    mParent.getSupportActionBar().setDisplayShowTitleEnabled(true);
  }

  public String getName() {
    if (mNameEditText != null) {
      return mNameEditText.getText().toString();
    }
    return "";
  }

  public boolean isChecked() {
    if (mLoop != null) {
      return mLoop.isChecked();
    }
    return false;
  }

  public void setChecked(boolean check) {
    if (mLoop != null) {
      mLoop.setChecked(check);
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
      case R.id.action_play:
        mStateGridFragment.preview();
        return true;
      case R.id.action_help:
        mParent.showHelp(this.getResources().getString(R.string.help_title_editingmoods));
        return true;
      case R.id.action_save:

        String moodName = mNameEditText.getText().toString();
        if (moodName == null || moodName.length() < 1) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mParent);
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
            .put(MoodColumns.COL_MOOD_VALUE, HueUrlEncoder.encode(mStateGridFragment.getMood()));

        if (mPriorName != null) {
          // modify existing mood
          String moodSelect = MoodColumns.COL_MOOD_NAME + "=?";
          String[] moodArg = {mPriorName};
          mParent.getContentResolver()
              .update(MoodColumns.MOODS_URI, mNewValues, moodSelect, moodArg);

          //now remember new mood name
          mPriorName = moodName;
        } else {

          mNewValues.put(MoodColumns.COL_MOOD_PRIORITY, MoodColumns.UNSTARRED_PRIORITY);
          mParent.getContentResolver().insert(MoodColumns.MOODS_URI, mNewValues);
        }

        Toast t =
            Toast.makeText(mParent, mParent.getResources().getString(R.string.saved) + " "
                                    + moodName, Toast.LENGTH_SHORT);
        t.show();
        mParent.onBackPressed();
        return true;
    }
    return false;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (mStateGridFragment != null) {
      mStateGridFragment.setMoodMode(position);
    }
    if (position == EditMoodStateGridFragment.PageType.RELATIVE_PAGE.ordinal()) {
      mLoop.setVisibility(View.VISIBLE);
    } else {
      mLoop.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (mStateGridFragment != null) {
      mStateGridFragment.redrawGrid();
    }
  }
}
