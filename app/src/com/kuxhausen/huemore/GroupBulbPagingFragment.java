package com.kuxhausen.huemore;

import java.lang.reflect.Field;

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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

public class GroupBulbPagingFragment extends SherlockFragment {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	GroupBulbPagerAdapter mGroupBulbPagerAdapter;

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
	GodObject parrentActivity;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnBulbGroupSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onGroupBulbSelected(Integer[] bulbNum, String name);

	}

	public void onSelected(Integer[] bulbNum, String name,
			GroupsListFragment groups, BulbsFragment bulbs) {
		if (groups == groupsListFragment && groups != null
				&& bulbsFragment != null)
			bulbsFragment.invalidateSelection();
		if (bulbs == bulbsFragment && bulbs != null
				&& groupsListFragment != null)
			groupsListFragment.invalidateSelection();

		if (parrentActivity != null || bulbNum == null || name == null)
			parrentActivity.onGroupBulbSelected(bulbNum, name);
		// ((GodObject)this.getSherlockActivity()).onGroupBulbSelected(bulbNum,
		// name);

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
		mGroupBulbPagerAdapter = new GroupBulbPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mGroupBulbPagerAdapter);
		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false)) {
			if (mViewPager.getCurrentItem() != GROUP_LOCATION)
				mViewPager.setCurrentItem(GROUP_LOCATION);
		} else {
			if (mViewPager.getCurrentItem() != BULB_LOCATION)
				mViewPager.setCurrentItem(BULB_LOCATION);
		}

		setHasOptionsMenu(true);
		this.setRetainInstance(false);
		return myView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		((SherlockListFragment) mGroupBulbPagerAdapter.getItem(mViewPager
				.getCurrentItem())).onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return ((SherlockListFragment) mGroupBulbPagerAdapter
				.getItem(mViewPager.getCurrentItem()))
				.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		getSherlockActivity().getSupportActionBar().setTitle(R.string.app_name);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			parrentActivity = (GodObject) activity;
		} catch (ClassCastException e) {
		}
	}

	@Override
	public void onDestroy() {
		if (groupsListFragment != null && groupsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(groupsListFragment)
					.commit();
		}
		if (bulbsFragment != null && bulbsFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(bulbsFragment)
					.commit();
		}
		groupsListFragment = null;
		bulbsFragment = null;

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (groupsListFragment != null && groupsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(groupsListFragment)
					.commit();
		}
		if (bulbsFragment != null && bulbsFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(bulbsFragment)
					.commit();
		}
		groupsListFragment = null;
		bulbsFragment = null;

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDetach() {
		if (groupsListFragment != null && groupsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(groupsListFragment)
					.commit();
		}
		if (bulbsFragment != null && bulbsFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(bulbsFragment)
					.commit();
		}
		groupsListFragment = null;
		bulbsFragment = null;

		super.onDetach();

		try {
			Field childFragmentManager = Fragment.class
					.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
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
			frag = fragment;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case GROUP_LOCATION:
				if (groupsListFragment == null) {
					groupsListFragment = new GroupsListFragment();
					groupsListFragment.setSelectionListener(frag);
				}
				return groupsListFragment;
			case BULB_LOCATION:
				if (bulbsFragment == null) {
					bulbsFragment = new BulbsFragment();
					bulbsFragment.setSelectionListener(frag);
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
