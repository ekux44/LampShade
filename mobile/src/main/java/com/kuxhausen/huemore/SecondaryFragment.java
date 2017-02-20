package com.kuxhausen.huemore;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Group;

/**
 * @author Eric Kuxhausen
 */
public class SecondaryFragment extends Fragment
    implements OnServiceConnectedListener, OnActiveMoodsChangedListener,
               DeviceManager.OnStateChangedListener {

  private NavigationDrawerActivity mParent;

  private SharedPreferences mSettings;
  private ViewPager mMoodManualViewPager;
  private MoodManualPagerAdapter mMoodManualPagerAdapter;
  private SlidingTabLayout mMoodManualSlidingTabLayout;
  private SeekBar mBrightnessBar, mMaxBrightnessBar;
  private TextView mBrightnessDescriptor;
  private boolean mIsTrackingTouch = false;
  private String brightnesstitle = "";
  private String brightnesspercent = ": 0%";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.secondary_activity, null);

    mParent = (NavigationDrawerActivity) this.getActivity();

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
        < Configuration.SCREENLAYOUT_SIZE_LARGE) {
      //if not in splitscreen mode, try change page title to any selected group
      if (mParent.boundToService()) {
        Group currentlySelected = mParent.getService().getDeviceManager().getSelectedGroup();
        if (currentlySelected != null) {
          mParent.getSupportActionBar().setTitle(currentlySelected.getName());
        }
      }
    }

    mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
    // Set up the ViewPager, attaching the adapter.
    mMoodManualViewPager = (ViewPager) myView.findViewById(R.id.manual_mood_pager);
    mMoodManualViewPager.setAdapter(mMoodManualPagerAdapter);

    // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
    // it's PagerAdapter set.
    mMoodManualSlidingTabLayout =
        (SlidingTabLayout) myView.findViewById(R.id.manual_mood_sliding_tabs);
    mMoodManualSlidingTabLayout.setViewPager(mMoodManualViewPager);
    mMoodManualSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(
        R.color.accent));
    mMoodManualSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.day_primary));

    mSettings = PreferenceManager.getDefaultSharedPreferences(mParent);
    if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
      mMoodManualViewPager.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
    }
    mBrightnessDescriptor = (TextView) myView.findViewById(R.id.brightnessDescripterTextView);
    mBrightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
    mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = false;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mParent.boundToService()) {
          if (fromUser) {
            DeviceManager dm = mParent.getService().getDeviceManager();
            if (dm.getSelectedGroup() != null) {
              dm.obtainBrightnessManager(dm.getSelectedGroup()).setBrightness(progress);
              brightnesspercent = ": " + progress + "%";
              mBrightnessDescriptor.setText(brightnesstitle + brightnesspercent);
            }
          }
        }
      }
    });

    mMaxBrightnessBar = (SeekBar) myView.findViewById(R.id.maxBrightnessBar);
    mMaxBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = false;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mParent.boundToService()) {
          if (fromUser) {
            DeviceManager dm = mParent.getService().getDeviceManager();
            if (dm.getSelectedGroup() != null) {
              dm.obtainBrightnessManager(dm.getSelectedGroup()).setBrightness(progress);
              brightnesspercent = ": " + progress + "%";
              mBrightnessDescriptor.setText(brightnesstitle + brightnesspercent);
            }
          }
        }
      }
    });

    return myView;
  }

  @Override
  public void onStart() {
    super.onStart();
    mParent.getSupportActionBar().setElevation(0);
  }

  @Override
  public void onResume() {
    super.onResume();
    mParent.registerOnServiceConnectedListener(this);

    setMode();
  }

  @Override
  public void onServiceConnected() {
    mParent.getService().getDeviceManager().registerBrightnessListener(this);
    mParent.getService().getMoodPlayer().addOnActiveMoodsChangedListener(this);
    setMode();
  }

  @Override
  public void onActiveMoodsChanged() {
    setMode();
  }

  public void setMode() {
    if (!mParent.boundToService() || mBrightnessBar == null) {
      return;
    }

    Group g = mParent.getService().getDeviceManager().getSelectedGroup();
    BrightnessManager bm = mParent.getService().getDeviceManager().peekBrightnessManager(g);

    if (bm != null && bm.getPolicy() == BrightnessManager.BrightnessPolicy.VOLUME_BRI) {
      mBrightnessBar.setVisibility(View.GONE);
      mMaxBrightnessBar.setVisibility(View.VISIBLE);
      ///mBrightnessDescriptor.setText(R.string.max_brightness);
      brightnesstitle = getActivity().getString(R.string.max_brightness);
      mBrightnessDescriptor.setText(brightnesstitle + brightnesspercent);

    } else {
      mBrightnessBar.setVisibility(View.VISIBLE);
      mMaxBrightnessBar.setVisibility(View.GONE);
      ///mBrightnessDescriptor.setText(R.string.brightness);
      brightnesstitle = getActivity().getString(R.string.brightness);
      mBrightnessDescriptor.setText(brightnesstitle + brightnesspercent);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mParent.boundToService()) {
      mParent.getService().getDeviceManager().removeBrightnessListener(this);
      mParent.getService().getMoodPlayer().removeOnActiveMoodsChangedListener(this);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    mParent.getSupportActionBar()
        .setElevation(getResources().getDimension(R.dimen.elevation_action_bar));
  }

  @Override
  public void onSaveInstanceState(Bundle outstate) {
    Editor edit = mSettings.edit();
    switch (mMoodManualViewPager.getCurrentItem()) {
      case MoodManualPagerAdapter.MANUAL_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, false);
        break;
      case MoodManualPagerAdapter.MOOD_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
        break;
    }
    edit.commit();

    super.onSaveInstanceState(outstate);
  }

  public void invalidateSelection() {
    ((MoodListFragment) (mMoodManualPagerAdapter.getItem(MoodManualPagerAdapter.MOOD_LOCATION)))
        .invalidateSelection();
  }

  @Override
  public void onStateChanged() {
    if (mParent != null && mParent.boundToService()) {
      DeviceManager dm = mParent.getService().getDeviceManager();

      if (!mIsTrackingTouch && mBrightnessBar != null && mMaxBrightnessBar != null
          && dm.getSelectedGroup() != null) {
        int brightness = dm.obtainBrightnessManager(dm.getSelectedGroup()).getBrightness();
        mBrightnessBar.setProgress(brightness);
        mMaxBrightnessBar.setProgress(brightness);
        brightnesspercent = ": " + brightness + "%";
        mBrightnessDescriptor.setText(brightnesstitle + brightnesspercent);

      }
    }
  }
}
