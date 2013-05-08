package com.kuxhausen.huemore;

import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.kuxhausen.huemore.network.GetBulbsAttributes;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

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
	public MainActivity parrentActivity;
	int brightness;
	boolean isTrackingTouch = false;
	SharedPreferences settings;
	static ColorWheelFragment nchf = null;
	Gson gson = new Gson();

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnMoodManualSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onMoodManualSelected(String jSon);

	}

	public void reset() {
		((MoodsListFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
				.updateGroupView();
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
				parrentActivity
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
		
		parrentActivity.setTitle(parrentActivity.groupS);
		
		GetBulbsAttributes getBulbsAttributes = new GetBulbsAttributes(
				parrentActivity, parrentActivity.bulbS, this);
		getBulbsAttributes.execute();

	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class MoodManualPagerAdapter extends FragmentPagerAdapter {

		android.support.v4.app.Fragment frag;
		
		public MoodManualPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());
			frag= fragment;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case MOOD_LOCATION:
				// TODO cache somewhere
				return new MoodsListFragment();
			case MANUAL_LOCATION:
				nchf = new ColorWheelFragment();
				nchf.hideTransitionTime();
				return nchf;
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

			if (nchf != null) {
				// this button marking approach gets treated like real user
				// input and modifies entire group. BAD
				/*
				 * boolean colorLoopOn = false; for (BulbAttributes ba :
				 * bulbsAttributes) { if (ba != null &&
				 * ba.state.effect.equals("colorloop")) colorLoopOn = true; }
				 * //if(colorLoopOn) //nchf.colorLoop.setChecked(true);
				 */
			}
		}
	}

}
