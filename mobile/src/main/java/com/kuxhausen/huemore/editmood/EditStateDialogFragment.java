package com.kuxhausen.huemore.editmood;

import com.google.gson.Gson;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Group;

public class EditStateDialogFragment extends DialogFragment implements OnClickListener {

  private EditStatePager mStatePagerAdapter;
  private EditMoodStateGridFragment mEditMoodFrag;
  private ViewPager mViewPager;
  private int mCurrentPagePosition;
  private Spinner mTransitionSpinner;
  private int[] mTransitionValues;
  private Gson gson = new Gson();
  private int mRow, mCol;

  public EditMoodStateGridFragment getStateGridFragment() {
    return mEditMoodFrag;
  }

  public void setStateIfVisible(BulbState newState, StateSelector initiator, int pageNo) {
    if (mStatePagerAdapter.convertToPageNumber(mCurrentPagePosition) == pageNo) {
      setSpinner(newState.getTransitionTime());
      this.stateChanged(newState, initiator);
    }
  }

  public interface StateSelector {

    public void stateChanged(BulbState newState);

    public BulbState getState();

    public void initialize(EditStateDialogFragment statePage, BulbState initialState);
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

    // request a window without the title
    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.color_dialog_pager, container, false);

    mTransitionSpinner = (Spinner) myView.findViewById(R.id.transitionSpinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity(), R.array.transition_names_array,
                                        android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mTransitionSpinner.setAdapter(adapter);

    mTransitionValues = getActivity().getResources().getIntArray(R.array.transition_values_array);

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

    if (currentState != null) {
      mViewPager.setCurrentItem(
          mStatePagerAdapter.convertToPagePosition(EditStatePager.RECENT_PAGE));
      setSpinner(currentState.getTransitionTime());
    }

    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);
    Button okayButton = (Button) myView.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    return myView;
  }

  private void setSpinner(Integer priorTransitionTime) {
    if (priorTransitionTime != null) {
      int pos = 0;
      for (int i = 0; i < mTransitionValues.length; i++) {
        if (priorTransitionTime == mTransitionValues[i]) {
          pos = i;
        }
      }
      mTransitionSpinner.setSelection(pos);
    }
  }

  private Integer getTransitionTime() {
    return mTransitionValues[mTransitionSpinner.getSelectedItemPosition()];
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
