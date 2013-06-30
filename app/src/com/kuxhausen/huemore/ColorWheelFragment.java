package com.kuxhausen.huemore;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.NewColorPagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbState;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class ColorWheelFragment extends SherlockFragment implements
		OnCreateColorListener, OnCreateMoodListener,
		OnCheckedChangeListener, com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener {

	public interface OnColorChangedListener {
		void colorChanged(int color, int hue);

		float getSaturation();
	}

	ColorPicker picker;
	SaturationBar saturationBar;
	private BulbState hs;
	Gson gson = new Gson();

	CompoundButton colorLoop;
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

		hs.hue = 0;// todo poll existing saturation if there is one

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color, null);

		picker = (ColorPicker) groupDialogView.findViewById(R.id.picker);
		picker.setOnColorChangedListener(this);
		saturationBar = (SaturationBar) groupDialogView.findViewById(R.id.saturationbar);
		picker.addSaturationBar(saturationBar);

		if (colorLoopLayoutVisible) {
			colorLoop = (CompoundButton) groupDialogView
					.findViewById(R.id.colorLoopCompoundButton);
			colorLoop.setOnCheckedChangeListener(this);
			colorLoopLayout = (LinearLayout) groupDialogView
					.findViewById(R.id.colorLoopLayout);
		} else {
			groupDialogView.findViewById(R.id.colorLoopLayout).setVisibility(
					View.GONE);
		}

		ArrayAdapter<CharSequence> adapter;
		if (transitionLayoutVisible) {
			transitionSpinner = (Spinner) groupDialogView
					.findViewById(R.id.transitionSpinner);
			// Create an ArrayAdapter using the string array and a default
			// spinner
			// layout
			adapter = ArrayAdapter.createFromResource(getActivity(),
					R.array.transition_names_array,
					android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			transitionSpinner.setAdapter(adapter);

			transitionValues = getActivity().getResources().getIntArray(
					R.array.transition_values_array);
			transitionLayout = (LinearLayout) groupDialogView
					.findViewById(R.id.transitionTimeLayout);
		} else {
			groupDialogView.findViewById(R.id.transitionTimeLayout)
					.setVisibility(View.GONE);
		}

		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.BULB_STATE)) {
			BulbState bs = gson.fromJson(
					args.getString(InternalArguments.BULB_STATE),
					BulbState.class);
			if (bs.hue != null)
				hs.hue = bs.hue;
			if (bs.sat != null) {
				hs.sat = bs.sat;
			}
			float[] hsv = { (bs.hue * 360) / 65535, bs.sat / 255f, 1 };
			picker.setColor(Color.HSVToColor(hsv));
			saturationBar.setSaturation(hsv[1]);
			if (transitionLayoutVisible && bs.transitiontime != null) {
				hs.transitiontime = bs.transitiontime;
				int pos = 0;
				for (int i = 0; i < transitionValues.length; i++)
					if (bs.transitiontime == transitionValues[i])
						pos = i;
				transitionSpinner.setSelection(pos);
			}
		}
		
		// Create the AlertDialog object and return it
		return groupDialogView;
	}
	
	public void hideColorLoop() {
		colorLoopLayoutVisible = false;
		colorLoop = null;
		if (colorLoopLayout != null)
			colorLoopLayout.setVisibility(View.GONE);
	}

	public void hideTransitionTime() {
		transitionLayoutVisible = false;
		transitionSpinner = null;
		if (transitionLayout != null)
			transitionLayout.setVisibility(View.GONE);
	}

	public void preview() {
		if(isAdded()){
			String[] states = { gson.toJson(hs) };
			((GodObject)this.getActivity()).updatePreview(states);
		}
	}

	@Override
	public Intent onCreateColor() {
		if (transitionSpinner != null)
			hs.transitiontime = transitionValues[transitionSpinner
					.getSelectedItemPosition()];
		//hs.hue = cpv.getHue();
		Intent i = new Intent();
		i.putExtra(InternalArguments.HUE_STATE, gson.toJson(hs));
		//i.putExtra(InternalArguments.COLOR, cpv.getColor());
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

	@Override
	public void onColorChanged(int color) {
		picker.setOldCenterColor(color);
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hs.hue = (int)((hsv[0] * 65535) / 360);
		hs.sat = (short)(hsv[1] * 255);
		preview();
	}
}
