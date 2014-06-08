package com.kuxhausen.huemore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class MoodManualPagerAdapter extends FragmentPagerAdapter {

	public static final int MOOD_LOCATION = 1;
	public static final int MANUAL_LOCATION = 0;

	private Fragment frag;
	
	public MoodManualPagerAdapter(Fragment f) {
		super(f.getChildFragmentManager());
		frag = f;
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case MOOD_LOCATION:
			return new MoodListFragment();
		case MANUAL_LOCATION:
			return new ColorWheelFragment();
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
		case MOOD_LOCATION:
			return frag.getString(R.string.moods).toUpperCase();
		case MANUAL_LOCATION:
			return frag.getString(R.string.cap_manual);
		}
		return "";
	}
}
