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

  private NavigationDrawerActivity parrentA;

  private SharedPreferences mSettings;
  private ViewPager mMoodManualViewPager;
  private MoodManualPagerAdapter mMoodManualPagerAdapter;
  private SlidingTabLayout mMoodManualSlidingTabLayout;
  private SeekBar mBrightnessBar, mMaxBrightnessBar;
  private TextView mBrightnessDescriptor;
  private boolean mIsTrackingTouch = false;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.secondary_activity, null);

    parrentA = (NavigationDrawerActivity) this.getActivity();

    parrentA.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
        < Configuration.SCREENLAYOUT_SIZE_LARGE) {
      //if not in splitscreen mode, try change page title to any selected group
      if (parrentA.boundToService()) {
        Group currentlySelected = parrentA.getService().getDeviceManager().getSelectedGroup();
        if (currentlySelected != null) {
          parrentA.getSupportActionBar().setTitle(currentlySelected.getName());
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
        R.color.redwidgets_color));

    mSettings = PreferenceManager.getDefaultSharedPreferences(parrentA);
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
        if (fromUser) {
          DeviceManager dm = parrentA.getService().getDeviceManager();
          if (dm.getSelectedGroup() != null) {
            dm.obtainBrightnessManager(dm.getSelectedGroup()).setBrightness(progress);
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
        if (fromUser) {
          DeviceManager dm = parrentA.getService().getDeviceManager();
          if (dm.getSelectedGroup() != null) {
            dm.obtainBrightnessManager(dm.getSelectedGroup()).setBrightness(progress);
          }
        }
      }
    });

    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    parrentA.registerOnServiceConnectedListener(this);
    this.setHasOptionsMenu(true);

    setMode();
  }

  @Override
  public void onServiceConnected() {
    parrentA.getService().getDeviceManager().registerBrightnessListener(this);
    parrentA.getService().getMoodPlayer().addOnActiveMoodsChangedListener(this);
    setMode();
  }

  @Override
  public void onActiveMoodsChanged() {
    setMode();
  }

  public void setMode() {
    if (!parrentA.boundToService() || mBrightnessBar == null) {
      return;
    }

    Group g = parrentA.getService().getDeviceManager().getSelectedGroup();
    BrightnessManager bm = parrentA.getService().getDeviceManager().peekBrightnessManager(g);

    if (bm != null && bm.getPolicy() == BrightnessManager.BrightnessPolicy.VOLUME_BRI) {
      mBrightnessBar.setVisibility(View.GONE);
      mMaxBrightnessBar.setVisibility(View.VISIBLE);
      mBrightnessDescriptor.setText(R.string.max_brightness);
    } else {
      mBrightnessBar.setVisibility(View.VISIBLE);
      mMaxBrightnessBar.setVisibility(View.GONE);
      mBrightnessDescriptor.setText(R.string.brightness);
    }
  }

  public void onPause() {
    super.onPause();
    if (parrentA.boundToService()) {
      parrentA.getService().getDeviceManager().removeBrightnessListener(this);
      parrentA.getService().getMoodPlayer().removeOnActiveMoodsChangedListener(this);
    }
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
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        parrentA.onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onStateChanged() {
    if (parrentA != null && parrentA.boundToService()) {
      DeviceManager dm = parrentA.getService().getDeviceManager();

      if (!mIsTrackingTouch && mBrightnessBar != null && mMaxBrightnessBar != null
          && dm.getSelectedGroup() != null) {
        int brightness = dm.obtainBrightnessManager(dm.getSelectedGroup()).getBrightness();
        mBrightnessBar.setProgress(brightness);
        mMaxBrightnessBar.setProgress(brightness);
      }
    }
  }
}
