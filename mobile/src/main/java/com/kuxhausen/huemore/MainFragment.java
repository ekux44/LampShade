package com.kuxhausen.huemore;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Group;

/**
 * @author Eric Kuxhausen
 */
public class MainFragment extends Fragment implements OnConnectionStatusChangedListener,
    OnServiceConnectedListener, OnActiveMoodsChangedListener {

  private NavigationDrawerActivity mParent;

  private SharedPreferences mSettings;
  private ViewPager mGroupBulbViewPager;
  private GroupBulbPagerAdapter mGroupBulbPagerAdapter;
  private SlidingTabLayout mGroupBulbSlidingTabLayout;
  private MoodManualPagerAdapter mMoodManualPagerAdapter;
  private ViewPager mMoodManualViewPager;
  private SlidingTabLayout mMoodManualSlidingTabLayout;
  private SeekBar mBrightnessBar, mMaxBrightnessBar;
  private boolean mIsTrackingTouch = false;
  private ForwardingPageListener mForwardPage;
  private TextView mBrightnessDescriptor;

  /*
   * @Override public void onConnectionStatusChanged(){ this.supportInvalidateOptionsMenu(); }
   */

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.main_activity, null);

    mParent = (NavigationDrawerActivity) this.getActivity();

    mGroupBulbPagerAdapter = new GroupBulbPagerAdapter(this);
    // Set up the ViewPager, attaching the adapter.
    mGroupBulbViewPager = (ViewPager) myView.findViewById(R.id.bulb_group_pager);
    mGroupBulbViewPager.setAdapter(mGroupBulbPagerAdapter);


    // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
    // it's PagerAdapter set.
    mGroupBulbSlidingTabLayout =
        (SlidingTabLayout) myView.findViewById(R.id.bulb_group_sliding_tabs);
    mGroupBulbSlidingTabLayout.setViewPager(mGroupBulbViewPager);
    mGroupBulbSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(
        R.color.greenwidgets_color));

    // add custom page changed lister to sych bulb/group tabs with nav drawer
    mForwardPage = new ForwardingPageListener();
    mGroupBulbSlidingTabLayout.setOnPageChangeListener(mForwardPage);

    mSettings = PreferenceManager.getDefaultSharedPreferences(mParent);
    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {

      mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
      // Set up the ViewPager, attaching the adapter.
      mMoodManualViewPager = (ViewPager) myView.findViewById(R.id.manual_mood_pager);
      mMoodManualViewPager.setAdapter(mMoodManualPagerAdapter);

      if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
        mMoodManualViewPager.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
      }

      // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
      // it's PagerAdapter set.
      mMoodManualSlidingTabLayout =
          (SlidingTabLayout) myView.findViewById(R.id.manual_mood_sliding_tabs);
      mMoodManualSlidingTabLayout.setViewPager(mMoodManualViewPager);
      mMoodManualSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(
          R.color.redwidgets_color));


      mBrightnessDescriptor = (TextView) myView.findViewById(R.id.brightnessDescripterTextView);
      mBrightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
      mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          DeviceManager dm = mParent.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
          mIsTrackingTouch = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          DeviceManager dm = mParent.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
          mIsTrackingTouch = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (fromUser) {
            DeviceManager dm = mParent.getService().getDeviceManager();
            dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), false);
          }
        }
      });

      mMaxBrightnessBar = (SeekBar) myView.findViewById(R.id.maxBrightnessBar);
      mMaxBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          DeviceManager dm = mParent.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
          mIsTrackingTouch = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          DeviceManager dm = mParent.getService().getDeviceManager();
          dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
          mIsTrackingTouch = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          if (fromUser) {
            DeviceManager dm = mParent.getService().getDeviceManager();
            dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress(), true);
          }
        }
      });

    }
    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    mParent.registerOnServiceConnectedListener(this);
    this.setHasOptionsMenu(true);
    Bundle b = this.getArguments();
    if (b != null && b.containsKey(InternalArguments.GROUPBULB_TAB)) {
      mGroupBulbViewPager.setCurrentItem(b.getInt(InternalArguments.GROUPBULB_TAB));
      b.remove(InternalArguments.GROUPBULB_TAB);
    }

    if (mBrightnessBar != null)
      setMode();
  }

  @Override
  public void onServiceConnected() {
    mParent.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
    mParent.getService().getMoodPlayer().addOnActiveMoodsChangedListener(this);
    if (mBrightnessBar != null)
      setMode();
  }

  @Override
  public void onActiveMoodsChanged() {
    if (mBrightnessBar != null)
      setMode();
  }

  public void setMode() {
    if (!mParent.boundToService() || mBrightnessBar == null)
      return;

    boolean maxBriMode = false;
    Group g = mParent.getService().getDeviceManager().getSelectedGroup();
    if (g != null && mParent.getService().getMoodPlayer().conflictsWithOngoingPlaying(g))
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
    if (mParent.boundToService()) {
      mParent.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
      mParent.getService().getMoodPlayer().removeOnActiveMoodsChangedListener(this);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outstate) {
    Editor edit = mSettings.edit();
    switch (mGroupBulbViewPager.getCurrentItem()) {
      case GroupBulbPagerAdapter.BULB_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false);
        break;
      case GroupBulbPagerAdapter.GROUP_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, true);
        break;
    }
    if (mMoodManualViewPager != null) {
      switch (mMoodManualViewPager.getCurrentItem()) {
        case MoodManualPagerAdapter.MANUAL_LOCATION:
          edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, false);
          break;
        case MoodManualPagerAdapter.MOOD_LOCATION:
          edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
          break;
      }
    }
    edit.commit();
    super.onSaveInstanceState(outstate);
  }

  public void invalidateSelection() {
    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
      ((MoodListFragment) (mMoodManualPagerAdapter.getItem(MoodManualPagerAdapter.MOOD_LOCATION)))
          .invalidateSelection();
    }
  }

  public void setTab(int tabNum) {
    if (mGroupBulbViewPager != null)
      mGroupBulbViewPager.setCurrentItem(tabNum);
  }

  public void onConnectionStatusChanged() {
    if (mBrightnessBar != null && !mIsTrackingTouch) {
      DeviceManager dm = mParent.getService().getDeviceManager();
      Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
      if (candidateBrightness != null)
        mBrightnessBar.setProgress(candidateBrightness);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.main, menu);

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
      MenuItem bothItem = menu.findItem(R.id.action_add_both);
      if (bothItem != null) {
        bothItem.setEnabled(false);
        bothItem.setVisible(false);
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {

      case R.id.action_add_both:
        AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
        addBoth
            .show(mParent.getSupportFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  class ForwardingPageListener extends SimpleOnPageChangeListener {

    @Override
    public void onPageSelected(int pagerPosition) {
      mParent.markSelected(pagerPosition);
    }

  }
}
