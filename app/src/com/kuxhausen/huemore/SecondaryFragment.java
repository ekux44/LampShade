package com.kuxhausen.huemore;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Group;

/**
 * @author Eric Kuxhausen
 */
public class SecondaryFragment extends Fragment implements OnConnectionStatusChangedListener,
    OnServiceConnectedListener {

  private NavigationDrawerActivity parrentA;

  private SharedPreferences mSettings;
  private ViewPager mMoodManualViewPager;
  private MoodManualPagerAdapter mMoodManualPagerAdapter;
  private SlidingTabLayout mMoodManualSlidingTabLayout;
  private SeekBar mBrightnessBar;
  private boolean mIsTrackingTouch = false;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.secondary_activity, null);

    parrentA = (NavigationDrawerActivity) this.getActivity();

    parrentA.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    Group currentlySelected = parrentA.getService().getDeviceManager().getSelectedGroup();
    if (currentlySelected != null)
      parrentA.getSupportActionBar().setTitle(currentlySelected.getName());


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
    mBrightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
    mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
        mIsTrackingTouch = false;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        DeviceManager dm = parrentA.getService().getDeviceManager();
        dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
        mIsTrackingTouch = true;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          DeviceManager dm = parrentA.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
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
  }

  @Override
  public void onServiceConnected() {
    parrentA.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
  }

  public void onPause() {
    super.onPause();
    if (parrentA.boundToService())
      parrentA.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
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
    if (mBrightnessBar != null && !mIsTrackingTouch) {
      DeviceManager dm = parrentA.getService().getDeviceManager();
      Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
      if (candidateBrightness != null)
        mBrightnessBar.setProgress(candidateBrightness);
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
