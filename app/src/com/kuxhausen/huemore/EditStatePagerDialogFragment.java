package com.kuxhausen.huemore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditStatePagerDialogFragment extends DialogFragment implements
		OnClickListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	NewMoodPagerAdapter mNewColorPagerAdapter;
	static OnCreateColorListener[] newColorFragments;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	static int currentPage;

	Spinner transitionSpinner;
	int[] transitionValues;
	Gson gson = new Gson();
	
	public interface OnCreateColorListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public Intent onCreateColor(Integer transitionTime);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.color_dialog_pager, container, false);

	 	transitionSpinner = (Spinner) myView
				.findViewById(R.id.transitionSpinner);
		// Create an ArrayAdapter using the string array and a default
		// spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.transition_names_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		transitionSpinner.setAdapter(adapter);

		transitionValues = getActivity().getResources().getIntArray(
				R.array.transition_values_array);
		
		
		
		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		mNewColorPagerAdapter = new NewMoodPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mNewColorPagerAdapter);
		currentPage = 0;
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;
			}

		});
		this.getDialog().setTitle(
				getActivity().getString(R.string.actionmenu_new_color));

		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);

		newColorFragments = new OnCreateColorListener[2];
		
		
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.BULB_STATE)) {
			BulbState bs = gson.fromJson(args.getString(InternalArguments.BULB_STATE),BulbState.class);
			routeState(bs);
			
			if (bs.transitiontime != null) {
				int pos = 0;
				for (int i = 0; i < transitionValues.length; i++)
					if (bs.transitiontime == transitionValues[i])
						pos = i;
				transitionSpinner.setSelection(pos);
			}
		}
		
		return myView;
	}

	private void routeState(BulbState bs) {
		// TODO Auto-generated method stub
		//mViewPager.setCurrentItem(2);
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class NewMoodPagerAdapter extends FragmentPagerAdapter {

		android.support.v4.app.Fragment frag;

		public NewMoodPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());
			frag = fragment;
		}

		@Override
		public Fragment getItem(int i) {
			if (newColorFragments[i] != null)
				return (Fragment) newColorFragments[i];
			switch (i) {
			case 0:
				newColorFragments[i] = new ColorWheelFragment();
				((ColorWheelFragment) newColorFragments[i]).hideColorLoop();
				return (Fragment) newColorFragments[i];
			case 1:
				newColorFragments[i] = new EditColorTempFragment();
				return (Fragment) newColorFragments[i];
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
			case 0:
				return frag.getActivity().getString(R.string.cap_hue_sat_mode);
			case 1:
				return frag.getActivity().getString(
						R.string.cap_color_temp_mode);
			}
			return "";
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.okay:
			Integer transitionTime = null;
			if (transitionSpinner != null)
				transitionTime = transitionValues[transitionSpinner
						.getSelectedItemPosition()];
			
			Intent i = newColorFragments[currentPage].onCreateColor(transitionTime);
			getTargetFragment().onActivityResult(getTargetRequestCode(),
					i.getExtras().getInt(InternalArguments.COLOR), i);
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
