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
  private BulbState mCurrentState;
  private Spinner mTransitionSpinner;
  private int[] mTransitionValues;
  private Gson gson = new Gson();
  private int mRow, mCol;

  public EditMoodStateGridFragment getStateGridFragment() {
    return mEditMoodFrag;
  }

  public BulbState getState() {
    if (mCurrentState == null) {
      mCurrentState = new BulbState();
    }
    return mCurrentState;
  }

  public void setStateIfVisible(BulbState newState, OnStateChangedListener initiator, int pageNo) {
    if (mStatePagerAdapter.convertToPageNumber(mCurrentPagePosition) == pageNo) {
      mCurrentState = newState.clone();
      setSpinner();
      this.stateChanged(initiator);
    }
  }

  public interface OnStateChangedListener {

    /**
     * return true if well suited to display updated data **
     */
    public boolean stateChanged();

    public void setStatePager(EditStateDialogFragment statePage);
  }

  private void stateChanged(OnStateChangedListener initiator) {
    for (OnStateChangedListener listener : mStatePagerAdapter.getColorListeners()) {
      if (listener != initiator && listener != null) {
        listener.stateChanged();
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
              briManager.setState(dm.getNetworkBulb(bulbId), mCurrentState);
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
    mStatePagerAdapter = new EditStatePager(this, hasRecentStates);

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

    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);
    Button okayButton = (Button) myView.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
      mRow = args.getInt(InternalArguments.ROW);
      mCol = args.getInt(InternalArguments.COLUMN);

      mCurrentState =
          gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE), BulbState.class);
      ((OnStateChangedListener) mStatePagerAdapter.getItemFromPageNumber(
          EditStatePager.RECENT_PAGE)).stateChanged();
      mViewPager
          .setCurrentItem(mStatePagerAdapter.convertToPagePosition(EditStatePager.RECENT_PAGE));

      setSpinner();
    }

    return myView;
  }

  private void setSpinner() {
    if (mCurrentState.getTransitionTime() != null) {
      int pos = 0;
      for (int i = 0; i < mTransitionValues.length; i++) {
        if (mCurrentState.getTransitionTime() == mTransitionValues[i]) {
          pos = i;
        }
      }
      mTransitionSpinner.setSelection(pos);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.okay:
        if (mTransitionSpinner != null) {
          mCurrentState
              .setTransitionTime(mTransitionValues[mTransitionSpinner.getSelectedItemPosition()]);
        }

        Intent i = new Intent();
        i.putExtra(InternalArguments.HUE_STATE, gson.toJson(mCurrentState));
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
