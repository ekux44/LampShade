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
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Group;

/**
 * @author Eric Kuxhausen
 */
public class SecondaryFragment extends Fragment implements OnConnectionStatusChangedListener,
    OnServiceConnectedListener, OnActiveMoodsChangedListener {

  private NavigationDrawerActivity parrentA;

  private SharedPreferences mSettings;
  private ViewPager mMoodManualViewPager;
  private MoodManualPagerAdapter mMoodManualPagerAdapter;
  private SlidingTabLayout mMoodManualSlidingTabLayout;
  private SeekBar mBrightnessBar, mMaxBrightnessBar;
  private boolean mIsTrackingTouch = false;
  private TextView mBrightnessDescriptor;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.secondary_activity, null);

    parrentA = (NavigationDrawerActivity) this.getActivity();

    parrentA.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
      //if not in splitscreen mode, try change page title to any selected group
      if(parrentA.boundToService()) {
        Group currentlySelected = parrentA.getService().getDeviceManager().getSelectedGroup();
        if (currentlySelected != null)
          parrentA.getSupportActionBar().setTitle(currentlySelected.getName());
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
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
        mIsTrackingTouch = false;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
        mIsTrackingTouch = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          DeviceManager dm = parrentA.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
        }
      }
    });

    mMaxBrightnessBar = (SeekBar) myView.findViewById(R.id.maxBrightnessBar);
    mMaxBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
        mIsTrackingTouch = false;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
        mIsTrackingTouch = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          DeviceManager dm = parrentA.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
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

    if (parrentA.boundToService()) {
      setMode();
    }

  }

  @Override
  public void onServiceConnected() {
    parrentA.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
    parrentA.getService().getMoodPlayer().addOnActiveMoodsChangedListener(this);
    setMode();
  }

  @Override
  public void onActiveMoodsChanged() {
    setMode();
  }

  public void setMode() {
    if (!parrentA.boundToService() || mBrightnessBar == null)
      return;
    
    boolean maxBriMode = false;
    Group g = parrentA.getService().getDeviceManager().getSelectedGroup();
    if (g != null && parrentA.getService().getMoodPlayer().conflictsWithOngoingPlaying(g))
      maxBriMode = true;

    if (maxBriMode) {
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
      parrentA.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
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
  public void onConnectionStatusChanged() {
    if(!parrentA.boundToService())
      return;

    if (mBrightnessBar != null && !mIsTrackingTouch
        && mBrightnessBar.getVisibility() == View.VISIBLE) {
      DeviceManager dm = parrentA.getService().getDeviceManager();
      Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
      if (candidateBrightness != null)
        mBrightnessBar.setProgress(candidateBrightness);
    } else if (mMaxBrightnessBar != null && !mIsTrackingTouch
        && mMaxBrightnessBar.getVisibility() == View.VISIBLE) {
      DeviceManager dm = parrentA.getService().getDeviceManager();
      Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
      if (candidateBrightness != null)
        mMaxBrightnessBar.setProgress(candidateBrightness);
    }
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
}
