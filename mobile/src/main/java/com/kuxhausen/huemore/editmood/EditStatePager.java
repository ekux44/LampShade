package com.kuxhausen.huemore.editmood;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.kuxhausen.huemore.R;

public class EditStatePager extends FragmentPagerAdapter {

  public final static int SAMPLE_PAGE = 0, RECENT_PAGE = 1, WHEEL_PAGE = 2, TEMP_PAGE = 3;

  private EditStateDialogFragment.OnStateChangedListener[] newColorFragments;
  private EditStateDialogFragment frag;
  private boolean mHasRecentStates;

  public EditStatePager(EditStateDialogFragment fragment, boolean hasRecentStates) {
    super(fragment.getChildFragmentManager());
    frag = fragment;
    mHasRecentStates = hasRecentStates;

    newColorFragments = new EditStateDialogFragment.OnStateChangedListener[getCount()];
  }

  public EditStateDialogFragment.OnStateChangedListener[] getColorListeners() {
    return newColorFragments;
  }

  public int convertToPageNumber(int position) {
    // Add +1 to pages offset due to missing Recent Page
    return position + ((!mHasRecentStates && position >= EditStatePager.RECENT_PAGE) ? 1 : 0);
  }

  /**
   * reverse of {@link #convertToPageNumber(int) }
   */
  public int convertToPagePosition(int pageNumber) {
    return pageNumber - ((!mHasRecentStates && pageNumber > EditStatePager.RECENT_PAGE) ? 1 : 0);
  }

  public Fragment getItemFromPageNumber(int pageNo) {
    return getItem(convertToPagePosition(pageNo));
  }

  @Override
  public Fragment getItem(int position) {
    if (newColorFragments[position] != null) {
      return (Fragment) newColorFragments[position];
    }

    int page = convertToPageNumber(position);
    switch (page) {
      case SAMPLE_PAGE:
        newColorFragments[position] = new SampleStatesFragment();
        newColorFragments[position].setStatePager(frag);
        return (Fragment) newColorFragments[position];
      case WHEEL_PAGE:
        newColorFragments[position] = new EditColorWheelFragment();
        newColorFragments[position].setStatePager(frag);
        return (Fragment) newColorFragments[position];
      case TEMP_PAGE:
        newColorFragments[position] = new EditColorTempFragment();
        newColorFragments[position].setStatePager(frag);
        return (Fragment) newColorFragments[position];
      case RECENT_PAGE:
        newColorFragments[position] = new RecentStatesFragment();
        newColorFragments[position].setStatePager(frag);
        return (Fragment) newColorFragments[position];
      default:
        return null;
    }
  }

  @Override
  public int getCount() {
    return 4 + (mHasRecentStates ? 0 : -1);
  }

  @Override
  public CharSequence getPageTitle(int position) {
    int page = convertToPageNumber(position);
    switch (page) {
      case SAMPLE_PAGE:
        return frag.getActivity().getString(R.string.cap_sample_state);
      case WHEEL_PAGE:
        return frag.getActivity().getString(R.string.cap_hue_sat_mode);
      case TEMP_PAGE:
        return frag.getActivity().getString(R.string.cap_color_temp_mode);
      case RECENT_PAGE:
        return frag.getActivity().getString(R.string.cap_recent_state);
    }
    return "";
  }
}
