package com.kuxhausen.huemore.editmood;

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
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditStatePagerDialogFragment extends DialogFragment implements
		OnClickListener {

	NewMoodPagerAdapter mNewColorPagerAdapter;
	static OnCreateColorListener[] newColorFragments;
	EditAdvancedMoodFragment parrentMood; 

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	static int currentPage;

	private BulbState currentState;
	
	Spinner transitionSpinner;
	int[] transitionValues;
	Gson gson = new Gson();
	
	public BulbState getState(){
		if(currentState==null)
			currentState = new BulbState();
		return currentState;
	}
	public void setState(BulbState newState, OnCreateColorListener initiator){
		currentState = newState;//gson.fromJson(gson.toJson(newState), BulbState.class);;
		this.stateChanged(initiator);
	}
	
	public interface OnCreateColorListener {		
		/*** return true if well suited to display updated data***/
		public boolean stateChanged();
		
		public void setStatePager(EditStatePagerDialogFragment statePage);
	}

	public void stateChanged(OnCreateColorListener initiator){
		for(OnCreateColorListener listener : newColorFragments){
			if(listener!=initiator && listener!=null)
				listener.stateChanged();
		}
		
		Mood m = Utils.generateSimpleMood(currentState);	
		Utils.transmit(this.getActivity(), InternalArguments.ENCODED_TRANSIENT_MOOD, m, ((GodObject) getActivity()).getBulbs(), null);
	}
	
	public void setParrentMood(EditAdvancedMoodFragment eamf){
		parrentMood = eamf;
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

		newColorFragments = new OnCreateColorListener[mNewColorPagerAdapter.getCount()];
		
		
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
			
			currentState = gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE),BulbState.class);
			routeState(currentState);
			
			if (currentState.transitiontime != null) {
				int pos = 0;
				for (int i = 0; i < transitionValues.length; i++)
					if (currentState.transitiontime == transitionValues[i])
						pos = i;
				transitionSpinner.setSelection(pos);
			}
		}
		
		return myView;
	}

	private void routeState(BulbState bs) {
		((RecentStatesFragment)mNewColorPagerAdapter.getItem(1)).loadPrevious(bs, parrentMood.dataRay);
		if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(1)).stateChanged())
			mViewPager.setCurrentItem(1);
		else if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(0)).stateChanged())
			mViewPager.setCurrentItem(0);
		else if(bs.ct!=null){
			mViewPager.setCurrentItem(3);
			((EditColorTempFragment)mNewColorPagerAdapter.getItem(mViewPager.getCurrentItem())).loadPrevious(bs);
		}
		else{
			mViewPager.setCurrentItem(2);
			((OnCreateColorListener)mNewColorPagerAdapter.getItem(mViewPager.getCurrentItem())).stateChanged();
		}
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class NewMoodPagerAdapter extends FragmentPagerAdapter {

		EditStatePagerDialogFragment frag;

		public NewMoodPagerAdapter(EditStatePagerDialogFragment fragment) {
			super(fragment.getChildFragmentManager());
			frag = fragment;
		}

		@Override
		public Fragment getItem(int i) {
			if (newColorFragments[i] != null)
				return (Fragment) newColorFragments[i];
			switch (i) {
			case 0:
				newColorFragments[i] = new SampleStatesFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			case 1:
				newColorFragments[i] = new RecentStatesFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			case 2:
				newColorFragments[i] = new EditColorWheelFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			case 3:
				newColorFragments[i] = new EditColorTempFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return frag.getActivity().getString(R.string.cap_sample_state);
			case 1:
				return frag.getActivity().getString(R.string.cap_recent_state);
			case 2:
				return frag.getActivity().getString(R.string.cap_hue_sat_mode);
			case 3:
				return frag.getActivity().getString(
						R.string.cap_color_temp_mode);
			}
			return "";
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okay:
			if (transitionSpinner != null)
				currentState.transitiontime = transitionValues[transitionSpinner
						.getSelectedItemPosition()];
			
			Intent i = new Intent();
			i.putExtra(InternalArguments.HUE_STATE, gson.toJson(currentState));
			
			if(i!=null)
				getTargetFragment().onActivityResult(getTargetRequestCode(), -1, i);
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
