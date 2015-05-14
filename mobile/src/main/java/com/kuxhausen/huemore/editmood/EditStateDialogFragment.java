package com.kuxhausen.huemore.editmood;

import com.google.gson.Gson;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Group;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class EditStateDialogFragment extends DialogFragment implements OnClickListener {

  private EditStatePager mStatePagerAdapter;
  private EditMoodStateGridFragment mEditMoodFrag;
  private ViewPager mViewPager;
  private int mCurrentPagePosition;
  private EditText mMinutesEditText, mSecondsEditText;
  private Gson gson = new Gson();
  private int mRow, mCol;

  //TODO consider switching locals when user overrides system language
  private NumberFormat mMinutesFormatter = NumberFormat.getIntegerInstance();
  private NumberFormat mMinutesFallbackFormatter = NumberFormat.getIntegerInstance(Locale.US);
  private NumberFormat mSecondsFormatter = NumberFormat.getNumberInstance();
  private NumberFormat mSecondsFallbackFormatter = NumberFormat.getIntegerInstance(Locale.US);

  {
    if (mSecondsFormatter instanceof DecimalFormat) {
      ((DecimalFormat) mSecondsFormatter).setDecimalSeparatorAlwaysShown(true);
    }
    if (mSecondsFallbackFormatter instanceof DecimalFormat) {
      ((DecimalFormat) mSecondsFallbackFormatter).setDecimalSeparatorAlwaysShown(true);
    }
  }

  public EditMoodStateGridFragment getStateGridFragment() {
    return mEditMoodFrag;
  }

  public void setStateIfVisible(BulbState newState, StateSelector initiator, int pageNo) {
    if (mStatePagerAdapter.convertToPageNumber(mCurrentPagePosition) == pageNo) {
      if (newState.getTransitionTime() != null) {
        setTransitionTime(newState.getTransitionTime());
      }
      this.stateChanged(newState, initiator);
    }
  }

  public interface StateSelector {

    void stateChanged(BulbState newState);

    BulbState getState();

    void initialize(EditStateDialogFragment statePage, BulbState initialState);
  }

  private void stateChanged(BulbState partialNewState, StateSelector initiator) {
    BulbState fullNewState = partialNewState.clone();
    fullNewState.setTransitionTime(getTransitionTime());

    for (StateSelector listener : mStatePagerAdapter.getColorListeners()) {
      if (listener != initiator && listener != null) {
        listener.stateChanged(fullNewState);
      }
    }

    if (this.getActivity() != null) {
      ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();
      if (service != null && service.getDeviceManager().getSelectedGroup() != null) {
        // TODO warn users with toast if no selected group
        DeviceManager dm = service.getDeviceManager();
        Group g = dm.getSelectedGroup();
        if (g != null) {
          BrightnessManager briManager = dm.obtainBrightnessManager(g);
          for (Long bulbId : g.getNetworkBulbDatabaseIds()) {
            if (dm.getNetworkBulb(bulbId) != null) {
              briManager.setState(dm.getNetworkBulb(bulbId), fullNewState);
            }
          }
        }
      }
    }
  }

  public void setEditMoodFrag(EditMoodStateGridFragment frag) {
    mEditMoodFrag = frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);

    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.color_dialog_pager, container, false);

    boolean hasRecentStates = false;
    if (RecentStatesFragment.extractUniques(mEditMoodFrag.moodRows).size() > 0) {
      hasRecentStates = true;
    }
    BulbState currentState = new BulbState();
    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
      mRow = args.getInt(InternalArguments.ROW);
      mCol = args.getInt(InternalArguments.COLUMN);

      currentState =
          gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE), BulbState.class);
    }

    mStatePagerAdapter = new EditStatePager(this, hasRecentStates, currentState);

    mViewPager = (ViewPager) myView.findViewById(R.id.pager);
    mViewPager.setAdapter(mStatePagerAdapter);
    mViewPager.setOffscreenPageLimit(3);

    // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
    // it's PagerAdapter set.
    SlidingTabLayout slidingTabLayout = (SlidingTabLayout) myView.findViewById(R.id.sliding_tabs);
    slidingTabLayout.setViewPager(mViewPager);
    slidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.accent));
    slidingTabLayout.setBackgroundColor(getResources().getColor(R.color.day_primary));

    mCurrentPagePosition = 0;
    slidingTabLayout.setOnPageChangeListener(new SimpleOnPageChangeListener() {

      @Override
      public void onPageSelected(int position) {
        mCurrentPagePosition = position;
      }

    });

    mMinutesEditText = (EditText) myView.findViewById(R.id.minutesEditText);
    mMinutesEditText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
              String corrected = formatMinutes(parseMinutes(v.getText().toString()));
              mMinutesEditText.setText(corrected);
            }
            return false;
          }
        });

    mSecondsEditText = (EditText) myView.findViewById(R.id.secondsEditText);
    mSecondsEditText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              String corrected = formatSeconds(parseSeconds(v.getText().toString()));
              mSecondsEditText.setText(corrected);
            }
            return false;
          }
        });

    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);
    Button okayButton = (Button) myView.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    if (currentState != null) {
      mViewPager.setCurrentItem(
          mStatePagerAdapter.convertToPagePosition(EditStatePager.RECENT_PAGE));
    }

    if (currentState != null && currentState.getTransitionTime() != null) {
      setTransitionTime(currentState.getTransitionTime());
    } else {
      setTransitionTime(4);
    }

    return myView;
  }

  /**
   * @return value in deciseconds
   */
  private int parseMinutes(String editTextInput) {
    int minutes = 0; //default value
    try {
      minutes = mMinutesFormatter.parse(editTextInput).intValue();
    } catch (ParseException e) {
      try {
        minutes = mMinutesFallbackFormatter.parse(editTextInput).intValue();
      } catch (ParseException e2) {
      }
    }
    minutes = Math.max(minutes, 0);
    minutes = Math.min(minutes, 60);

    return minutes * 600;
  }

  /**
   * @return value in deciseconds
   */
  private int parseSeconds(String editTextInput) {
    double seconds = .4; // default value
    try {
      seconds = mSecondsFormatter.parse(editTextInput).doubleValue();
    } catch (ParseException e) {
      try {
        seconds = mSecondsFallbackFormatter.parse(editTextInput).doubleValue();
      } catch (ParseException e2) {
      }
    }
    int deciseconds = (int) Math.round(10 * seconds);
    deciseconds = Math.max(deciseconds, 0);
    deciseconds = Math.min(deciseconds, 599);

    return deciseconds;
  }

  private String formatMinutes(int deciseconds) {
    return mMinutesFormatter.format(Math.min(deciseconds / 600, 60));
  }

  private String formatSeconds(int deciseconds) {
    return mSecondsFormatter.format(deciseconds % 600 / 10.0);
  }

  private void setTransitionTime(int deciseconds) {
    mSecondsEditText.setText(formatSeconds(deciseconds));
    mMinutesEditText.setText(formatMinutes(deciseconds));
  }

  /**
   * @return transitionTime in deciseconds
   */
  private int getTransitionTime() {
    int result = 0;
    result += parseMinutes(mMinutesEditText.getText().toString());
    result += parseSeconds(mSecondsEditText.getText().toString());
    return result;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.okay:
        BulbState state = mStatePagerAdapter.getStateItem(mCurrentPagePosition).getState().clone();
        state.setTransitionTime(getTransitionTime());

        Intent i = new Intent();
        i.putExtra(InternalArguments.HUE_STATE, gson.toJson(state));
        i.putExtra(InternalArguments.ROW, mRow);
        i.putExtra(InternalArguments.COLUMN, mCol);

        if (this.getTargetFragment() != null) {
          getTargetFragment().onActivityResult(-1, -1, i);
        }
        this.dismiss();
        break;
      case R.id.cancel:
        this.dismiss();
        break;
    }
  }
}
