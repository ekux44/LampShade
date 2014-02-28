package com.kuxhausen.huemore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class GroupBulbPagerAdapter extends FragmentPagerAdapter {

	public static final int GROUP_LOCATION = 1;
	public static final int BULB_LOCATION = 0;

	private GroupListFragment groupListFragment;
	private BulbListFragment bulbListFragment;
	private NetworkManagedSherlockFragmentActivity frag;

	public GroupBulbPagerAdapter(NetworkManagedSherlockFragmentActivity godObject) {
		super(godObject.getSupportFragmentManager());
		frag = godObject;
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case GROUP_LOCATION:
			if (groupListFragment == null) {
				groupListFragment = new GroupListFragment();
			}
			return groupListFragment;
		case BULB_LOCATION:
			if (bulbListFragment == null) {
				bulbListFragment = new BulbListFragment();
			}
			return bulbListFragment;
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

