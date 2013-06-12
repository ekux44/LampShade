package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

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

	private static GroupsListFragment groupsListFragment;
	private static BulbsFragment bulbsFragment;
	
	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;
	SharedPreferences settings;
	MainActivity parrentActivity;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnBulbGroupSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onGroupBulbSelected(Integer[] bulbNum, String name);

	}
	
	public void onSelected(Integer[] bulbNum, String name, GroupsListFragment groups, BulbsFragment bulbs){
		if(groups == groupsListFragment)
			bulbsFragment.invalidateSelection();
		else if(bulbs == bulbsFragment)
			groupsListFragment.invalidateSelection();
		
		((MainActivity)this.getActivity()).onGroupBulbSelected(bulbNum, name);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		settings = PreferenceManager.getDefaultSharedPreferences(this
				.getActivity());

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
		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false)) {
			mViewPager.setCurrentItem(GROUP_LOCATION);
		}
		return myView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		parrentActivity.setTitle(R.string.app_name);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			parrentActivity = (MainActivity) activity;
		} catch (ClassCastException e) {
		}
	}


	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class GroupBulbPagerAdapter extends FragmentPagerAdapter {

		GroupBulbPagingFragment frag;
		
		public GroupBulbPagerAdapter(GroupBulbPagingFragment fragment) {
			super(fragment.getChildFragmentManager());
			frag= fragment;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case GROUP_LOCATION:
				if(groupsListFragment==null){
					groupsListFragment = new GroupsListFragment();
					groupsListFragment.setSelectionListener(frag);
				}
				return groupsListFragment;
			case BULB_LOCATION:
				if(bulbsFragment == null){
					bulbsFragment = new BulbsFragment();
					groupsListFragment.setSelectionListener(frag);
				}
				return bulbsFragment;
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
				return frag.getActivity().getString(R.string.cap_groups);
			case BULB_LOCATION:
				return frag.getActivity().getString(R.string.cap_bulbs);

			}
			return "";
		}
	}

}
