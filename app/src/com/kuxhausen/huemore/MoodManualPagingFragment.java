package com.kuxhausen.huemore;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.network.GetBulbsAttributes;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class MoodManualPagingFragment extends SherlockFragment implements
		OnAttributeListReturnedListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	MoodManualPagerAdapter mMoodManualPagerAdapter;

	private static final int MOOD_LOCATION = 1;
	private static final int MANUAL_LOCATION = 0;

	private static MoodsListFragment moodsListFragment;
	private static ColorWheelFragment colorWheelFragment;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;
	SeekBar brightnessBar;
	public MainActivity parrentActivity;
	int brightness;
	boolean isTrackingTouch = false;
	SharedPreferences settings;
	Gson gson = new Gson();

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnMoodManualSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onMoodManualSelected(String jSon);

	}

	public void invalidateSelection() {
		((MoodsListFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
				.invalidateSelection();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		parrentActivity = (MainActivity) this.getActivity();
		settings = PreferenceManager
				.getDefaultSharedPreferences(parrentActivity);
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.moodmanual_pager, container,
				false);
		Bundle args = getArguments();

		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use
		// getSupportFragmentManager.
		mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mMoodManualPagerAdapter);
		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true)) {
			mViewPager.setCurrentItem(MOOD_LOCATION);
		}

		brightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				BulbState hs = new BulbState();
				hs.bri = brightness;
				hs.on = true;

				String[] brightnessState = { gson.toJson(hs) };
				// TODO deal with off?
				parrentActivity.onBrightnessChanged(brightnessState);
				isTrackingTouch = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				brightness = progress;
			}
		});
		setHasOptionsMenu(true);
		this.setRetainInstance(false);
		return myView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// if ((getResources().getConfiguration().screenLayout &
		// Configuration.SCREENLAYOUT_SIZE_MASK) >=
		// Configuration.SCREENLAYOUT_SIZE_LARGE) {
		// MenuItem unlocksItem = menu.findItem(R.id.action_add_mood);
		// unlocksItem.setEnabled(false);
		// unlocksItem.setVisible(false);

		// }

		if (mViewPager.getCurrentItem() == MOOD_LOCATION
				&& ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE))
			((SherlockListFragment) mMoodManualPagerAdapter
					.getItem(MOOD_LOCATION))
					.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mViewPager.getCurrentItem() == MOOD_LOCATION
				&& ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE))
			return ((SherlockListFragment) mMoodManualPagerAdapter
					.getItem(mViewPager.getCurrentItem()))
					.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
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

	public void pollBrightness() {
		GetBulbsAttributes getBulbsAttributes = new GetBulbsAttributes(
				parrentActivity, parrentActivity.bulbS, this,
				this.parrentActivity);
		getBulbsAttributes.execute();
	}

	@Override
	public void onResume() {
		super.onResume();
		pollBrightness();
	}

	@Override
	public void onDestroy() {
		if (moodsListFragment != null && moodsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(moodsListFragment)
					.commit();
		}
		if (colorWheelFragment != null && colorWheelFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(colorWheelFragment)
					.commit();
		}
		moodsListFragment = null;
		colorWheelFragment = null;

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (moodsListFragment != null && moodsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(moodsListFragment)
					.commit();
		}
		if (colorWheelFragment != null && colorWheelFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(colorWheelFragment)
					.commit();
		}
		moodsListFragment = null;
		colorWheelFragment = null;

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDetach() {
		if (moodsListFragment != null && moodsListFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(moodsListFragment)
					.commit();
		}
		if (colorWheelFragment != null && colorWheelFragment.isResumed()) {
			getFragmentManager().beginTransaction().remove(colorWheelFragment)
					.commit();
		}
		moodsListFragment = null;
		colorWheelFragment = null;

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
	public static class MoodManualPagerAdapter extends FragmentPagerAdapter {

		android.support.v4.app.Fragment frag;

		public MoodManualPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());
			frag = fragment;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case MOOD_LOCATION:
				if (moodsListFragment == null)
					moodsListFragment = new MoodsListFragment();
				return moodsListFragment;
			case MANUAL_LOCATION:
				if (colorWheelFragment == null) {
					colorWheelFragment = new ColorWheelFragment();
					colorWheelFragment.hideTransitionTime();
				}
				return colorWheelFragment;
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
				return frag.getActivity().getString(R.string.cap_moods);
			case MANUAL_LOCATION:
				return frag.getActivity().getString(R.string.cap_manual);
			}
			return "";
		}
	}

	@Override
	public void onListReturned(BulbAttributes[] bulbsAttributes) {
		if (!isTrackingTouch && bulbsAttributes != null
				&& bulbsAttributes.length > 0) {
			int brightnessSum = 0;
			int brightnessPool = 0;
			for (BulbAttributes ba : bulbsAttributes) {
				if (ba != null) {
					if (ba.state.on == false)
						brightnessPool++;
					else {
						brightnessSum += ba.state.bri;
						brightnessPool++;
					}
				}
			}
			if (brightnessPool == 0)
				return;
			int brightnessAverage = brightnessSum / brightnessPool;

			brightness = brightnessAverage;
			brightnessBar.setProgress(brightnessAverage);
		}
	}

}
