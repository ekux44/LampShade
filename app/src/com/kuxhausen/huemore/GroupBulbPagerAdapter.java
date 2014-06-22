package com.kuxhausen.huemore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class GroupBulbPagerAdapter extends FragmentPagerAdapter {

  public static final int GROUP_LOCATION = 1;
  public static final int BULB_LOCATION = 0;

  private Fragment frag;

  public GroupBulbPagerAdapter(Fragment f) {
    super(f.getChildFragmentManager());
    frag = f;
  }

  @Override
  public Fragment getItem(int i) {
    switch (i) {
      case GROUP_LOCATION:
        return new GroupListFragment();
      case BULB_LOCATION:
        return new BulbListFragment();
      default:
        return null;
    }
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
      case GROUP_LOCATION:
        return frag.getString(R.string.groups).toUpperCase();
      case BULB_LOCATION:
        return frag.getString(R.string.cap_bulbs);
    }
    return "";
  }
}
