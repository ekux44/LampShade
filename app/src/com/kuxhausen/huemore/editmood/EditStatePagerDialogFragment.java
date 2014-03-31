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
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditStatePagerDialogFragment extends DialogFragment implements
		OnClickListener {

	NewMoodPagerAdapter mNewColorPagerAdapter;
	static OnCreateColorListener[] newColorFragments;
	EditMoodStateGridFragment parrentMood; 

	ViewPager mViewPager;
	static int currentPage;
	public final static int SAMPLE_PAGE = 0, RECENT_PAGE = 1, WHEEL_PAGE = 2, TEMP_PAGE = 3;
	private BulbState currentState;
	Spinner transitionSpinner;
	int[] transitionValues;
	Gson gson = new Gson();
	private int row, col;
	
	/** 1 if true, 0 if false **/
	static int hasNoRecentStates = 1;
	
	public BulbState getState(){
		if(currentState==null)
			currentState = new BulbState();
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
		if(this.getActivity()!=null){
			MoodExecuterService service = ((NetworkManagedSherlockFragmentActivity)this.getActivity()).getService();
			service.getMoodPlayer().playMood(service.getDeviceManager().getSelectedGroup(), null, m, null);
		}
	}
	
	public void setParrentMood(EditMoodStateGridFragment eamf){
		parrentMood = eamf;
		if(RecentStatesFragment.extractUniques(eamf.moodRows).size()>0)
			hasNoRecentStates = 0;
		else
			hasNoRecentStates = 1;
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
		mViewPager.setOffscreenPageLimit(3);
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
			row = args.getInt(InternalArguments.ROW);
			col = args.getInt(InternalArguments.COLUMN);
			
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
		else if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(TEMP_PAGE-hasNoRecentStates)).stateChanged())
			mViewPager.setCurrentItem(TEMP_PAGE-hasNoRecentStates);
		else if(((OnCreateColorListener)mNewColorPagerAdapter.getItem(WHEEL_PAGE-hasNoRecentStates)).stateChanged())
			mViewPager.setCurrentItem(WHEEL_PAGE-hasNoRecentStates);
		else
			mViewPager.setCurrentItem(RECENT_PAGE);
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
			
			if(i == SAMPLE_PAGE){
				newColorFragments[i] = new SampleStatesFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			}  else if (i == WHEEL_PAGE - frag.hasNoRecentStates){
				newColorFragments[i] = new EditColorWheelFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			} else if (i == TEMP_PAGE - frag.hasNoRecentStates){
				newColorFragments[i] = new EditColorTempFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			} else if (i == RECENT_PAGE){
				newColorFragments[i] = new RecentStatesFragment();
				newColorFragments[i].setStatePager(frag);
				return (Fragment) newColorFragments[i];
			}
			else
				return null;
		}

		@Override
		public int getCount() {
			return 4-frag.hasNoRecentStates;
		}

		@Override
		public CharSequence getPageTitle(int i) {
			if(i == SAMPLE_PAGE)
				return frag.getActivity().getString(R.string.cap_sample_state);
			else if (i == WHEEL_PAGE - frag.hasNoRecentStates)
				return frag.getActivity().getString(R.string.cap_hue_sat_mode);
			else if (i == TEMP_PAGE - frag.hasNoRecentStates)
				return frag.getActivity().getString(R.string.cap_color_temp_mode);
			else if (i == RECENT_PAGE)
				return frag.getActivity().getString(R.string.cap_recent_state);
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
			i.putExtra(InternalArguments.ROW, row);
			i.putExtra(InternalArguments.COLUMN, col);
			
			if(this.getTargetFragment()!=null)
				getTargetFragment().onActivityResult(-1, -1, i);
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
