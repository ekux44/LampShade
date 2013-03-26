package com.kuxhausen.huemore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GroupBulbPagingFragment extends Fragment {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	GroupBulbPagerAdapter mGroupMoodPagerAdapter;

	private static final int GROUP_LOCATION = 1;
	private static final int BULB_LOCATION = 0;
	
	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnBulbGroupSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onGroupBulbSelected(Integer[] bulbNum);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.pager, container, false);
		Bundle args = getArguments();

		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use
		// getSupportFragmentManager.
		mGroupMoodPagerAdapter = new GroupBulbPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mGroupMoodPagerAdapter);

		return myView;
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class GroupBulbPagerAdapter extends FragmentPagerAdapter {

		public GroupBulbPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());

			// write your code here
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case GROUP_LOCATION:
				// TODO cache somewhere
				return new GroupsFragment();
			case BULB_LOCATION:
				return new BulbsFragment();
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
				return "GROUPS";// TODO figure out how to make static references
								// to strings.xml
			case BULB_LOCATION:
				return "BULBS";
			}
			return "";
		}
	}

}
