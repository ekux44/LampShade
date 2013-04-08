package com.kuxhausen.huemore;

import com.google.gson.Gson;
import com.kuxhausen.huemore.database.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.network.GetBulbsAttributes;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.state.BulbAttributes;
import com.kuxhausen.huemore.state.BulbState;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MoodManualPagingFragment extends Fragment implements
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

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;
	SeekBar brightnessBar;
	public Context parrentActivity;
	int brightness;
	boolean isTrackingTouch = false;
	SharedPreferences settings;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnMoodManualSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onMoodManualSelected(String jSon);

	}

	public void reset() {
		((MoodsFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
				.updateGroupView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		parrentActivity = this.getActivity();
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
				Gson gs = new Gson();
				String[] brightnessState = { gs.toJson(hs) };
				// TODO deal with off?
				((MainActivity) parrentActivity)
						.onBrightnessChanged(brightnessState);
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

		return myView;
	}

	@Override
	public void onResume() {
		super.onResume();
		GetBulbsAttributes getBulbsAttributes = new GetBulbsAttributes();
		getBulbsAttributes.execute(parrentActivity,
				((MainActivity) parrentActivity).bulbS, this);

	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class MoodManualPagerAdapter extends FragmentPagerAdapter {

		public MoodManualPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());

			// write your code here
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case MOOD_LOCATION:
				// TODO cache somewhere
				return new MoodsFragment();
			case MANUAL_LOCATION:
				return new NewColorHueFragment();
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
				return "MOODS";// TODO figure out how to make static references
								// to strings.xml
			case MANUAL_LOCATION:
				return "MANUAL";
			}
			return "";
		}
	}

	@Override
	public void onListReturned(BulbAttributes[] bulbsAttributes) {
		if (!isTrackingTouch && bulbsAttributes != null && bulbsAttributes.length>0) {
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
			if(brightnessPool==0)
				return;
			int brightnessAverage = brightnessSum / brightnessPool;

			brightness = brightnessAverage;
			brightnessBar.setProgress(brightnessAverage);
		}
	}

}
