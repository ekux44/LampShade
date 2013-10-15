package com.kuxhausen.huemore.editmood;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
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

	ViewPager mViewPager;
	static int currentPage;
	public final static int SAMPLE_PAGE = 0, RECENT_PAGE = 1, WHEEL_PAGE = 2, TEMP_PAGE = 3;
	private BulbState currentState;
	Spinner transitionSpinner;
	int[] transitionValues;
	Gson gson = new Gson();
	
	public BulbState getState(){
		if(currentState==null){
			currentState = new BulbState();
			currentState.on = true;
		}
		return currentState;
	}
	public void setState(BulbState newState, OnCreateColorListener initiator, String optionalMessage){		
		if(newState!=null && newState.xy!=null)
			Log.e("setStateInPager",newState.xy[0]+"  "+newState.clone().xy[0]+"  "+optionalMessage);
		else if(newState!=null && newState.ct!=null)
			Log.e("setStateInPager", newState.ct +" "+optionalMessage);
		currentState = newState.clone();
		setSpinner();
		this.stateChanged(initiator);
	}
	
	public interface OnCreateColorListener {		
		/*** return true if well suited to display updated data***/
		public boolean stateChanged();
		
		public void setStatePager(EditStatePagerDialogFragment statePage);
	}

	private void stateChanged(OnCreateColorListener initiator){
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
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.transition_names_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		transitionSpinner.setAdapter(adapter);

		transitionValues = getActivity().getResources().getIntArray(
				R.array.transition_values_array);
		
		
		
		// Create an adapter that when requested, will return a fragment
		// representing an object in the collection.
		mNewColorPagerAdapter = new NewMoodPagerAdapter(this);

		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mNewColorPagerAdapter);
		currentPage = 0;
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;
			}

		});
		this.getDialog().setTitle(getActivity().getString(R.string.actionmenu_new_color));

		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);

		newColorFragments = new OnCreateColorListener[mNewColorPagerAdapter.getCount()];
		
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
			
			currentState = gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE),BulbState.class);
			routeState(currentState);
			
			setSpinner();
		}
		
		return myView;
	}
	
	private void setSpinner(){
		if (currentState.transitiontime != null) {
			int pos = 0;
			for (int i = 0; i < transitionValues.length; i++)
				if (currentState.transitiontime == transitionValues[i])
					pos = i;
			transitionSpinner.setSelection(pos);
		}
	}

	private void routeState(BulbState bs) {
		if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(RECENT_PAGE)).stateChanged())
			mViewPager.setCurrentItem(RECENT_PAGE);
		else if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(SAMPLE_PAGE)).stateChanged())
			mViewPager.setCurrentItem(SAMPLE_PAGE);
		else if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(TEMP_PAGE)).stateChanged())
			mViewPager.setCurrentItem(TEMP_PAGE);
		else{
			((OnCreateColorListener)mNewColorPagerAdapter.getItem(WHEEL_PAGE)).stateChanged();
			mViewPager.setCurrentItem(WHEEL_PAGE);
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
