package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.kuxhausen.huemore.NewColorPagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbState;

public class ColorWheelFragment extends Fragment implements
		OnSeekBarChangeListener, OnCreateColorListener, OnCreateMoodListener,
		OnCheckedChangeListener {

	public interface OnColorChangedListener {
		void colorChanged(int color, int hue);
		float getSaturation();
	}

	private OnColorChangedListener mListener;
	private ColorWheelView cpv;
	private BulbState hs;
	Gson gson = new Gson();
	SeekBar seekBar;
	ToggleButton colorLoop;
	Spinner transitionSpinner;
	int[] transitionValues;
	LinearLayout colorLoopLayout, transitionLayout;
	boolean colorLoopLayoutVisible = true, transitionLayoutVisible = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		
		hs = new BulbState();
		hs.on = true;
		hs.effect = "none";

		hs.hue=0;//todo poll existing saturation if there is one
		
		mListener = new OnColorChangedListener() {
			@Override
			public void colorChanged(int color, int hues) {
				hs.hue = hues;
				preview();
			}

			@Override
			public float getSaturation() {
				// TODO Auto-generated method stub
				return seekBar.getProgress()/255f;
			}
		};

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color, null);
		cpv = ((ColorWheelView) groupDialogView.findViewById(R.id.colorWheel));
		cpv.setOnColorChangedListener(mListener);

		seekBar = (SeekBar) groupDialogView.findViewById(R.id.saturationBar);
		seekBar.setOnSeekBarChangeListener(this);
		hs.sat = (short) seekBar.getProgress();

		if(colorLoopLayoutVisible){
		colorLoop = (ToggleButton) groupDialogView
				.findViewById(R.id.colorLoopToggleButton);
		colorLoop.setOnCheckedChangeListener(this);
		colorLoopLayout = (LinearLayout)groupDialogView.findViewById(R.id.colorLoopLayout);
		}else{
			groupDialogView.findViewById(R.id.colorLoopLayout).setVisibility(View.GONE);
		}
		
		ArrayAdapter<CharSequence> adapter;
		if(transitionLayoutVisible){
		transitionSpinner = (Spinner) groupDialogView
				.findViewById(R.id.transitionSpinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.transition_names_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		transitionSpinner.setAdapter(adapter);

		transitionValues = getActivity().getResources().getIntArray(
				R.array.transition_values_array);
		transitionLayout = (LinearLayout)groupDialogView.findViewById(R.id.transitionTimeLayout);
		}else{
			groupDialogView.findViewById(R.id.transitionTimeLayout).setVisibility(View.GONE);
		}
		
		Bundle args = getArguments();
		if(args!=null && args.containsKey(InternalArguments.BULB_STATE))
		{
			BulbState bs = gson.fromJson(args.getString(InternalArguments.BULB_STATE), BulbState.class);
			if(bs.hue!=null)
				hs.hue=bs.hue;
			if(bs.sat!=null){
				hs.sat=bs.sat;
				seekBar.setProgress(bs.sat);
			}
			cpv.setInitialHSV(hs.hue, hs.sat);
			if(transitionLayoutVisible&&bs.transitiontime!=null){
				hs.transitiontime=bs.transitiontime;
				int pos= 0;
				for(int i=0; i<transitionValues.length; i++)
					if(bs.transitiontime==transitionValues[i])
						pos=i;
				transitionSpinner.setSelection(pos);
			}
		}

		// Create the AlertDialog object and return it
		return groupDialogView;
	}

	public void hideColorLoop(){
		colorLoopLayoutVisible = false;
		colorLoop = null;
		if(colorLoopLayout!=null)
			colorLoopLayout.setVisibility(View.GONE);
	}
	public void hideTransitionTime(){
		transitionLayoutVisible = false;
		transitionSpinner = null;
		if(transitionLayout!=null)
			transitionLayout.setVisibility(View.GONE);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		hs.sat = (short) seekBar.getProgress();
		preview();
		cpv.recalculateColor();
	}

	public void preview() {
		String[] states = { gson.toJson(hs) };
		((MainActivity) getActivity()).testMood(states);

	}

	@Override
	public Intent onCreateColor() {
		if(transitionSpinner!=null)
			hs.transitiontime =transitionValues[transitionSpinner.getSelectedItemPosition()];
		hs.hue = cpv.getHue();
		Intent i = new Intent();
		i.putExtra("HueState", gson.toJson(hs));
		i.putExtra("Color", cpv.getColor());
		return i;
	}

	@Override
	public void onCreateMood(String groupname) {
		onCreateColor();
		// Defines a new Uri object that receives the result
		// of the insertion
		Uri mNewUri;

		// Defines an object to contain the new values to
		// insert
		ContentValues mNewValues = new ContentValues();

		/*
		 * Sets the values of each column and inserts the word. The arguments to
		 * the "put" method are "column name" and "value"
		 */
		mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, groupname);
		mNewValues.put(DatabaseDefinitions.MoodColumns.STATE, gson.toJson(hs));
		mNewValues.put(DatabaseDefinitions.MoodColumns.PRECEDENCE, 0);

		mNewUri = getActivity().getContentResolver().insert(
				DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues // the
																		// values
																		// to
																		// insert
				);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		if (isChecked)
			hs.effect = "colorloop";
		else
			hs.effect = "none";
		preview();
	}
}
