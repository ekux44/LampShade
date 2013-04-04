package com.kuxhausen.huemore;

import com.google.gson.Gson;
import com.kuxhausen.huemore.NewColorPagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.NewMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.state.BulbState;

import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NewColorHueFragment extends Fragment implements
		OnSeekBarChangeListener, OnCreateColorListener, OnCreateMoodListener {

	public interface OnColorChangedListener {
		void colorChanged(int color, int hue);
	}

	private OnColorChangedListener mListener;
	private int mInitialColor;
	private ColorPickerView cpv;
	private BulbState hs;
	Gson gson = new Gson();
	SeekBar seekBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();

		hs = new BulbState();
		hs.on = true;
		mInitialColor = 0;

		mListener = new OnColorChangedListener() {
			@Override
			public void colorChanged(int color, int hues) {
				hs.hue = hues;
				preview();
			}
		};

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color, null);
		cpv = ((ColorPickerView) groupDialogView.findViewById(R.id.colorWheel));
		cpv.setOnColorChangedListener(mListener);

		seekBar = (SeekBar) groupDialogView.findViewById(R.id.saturationBar);
		seekBar.setOnSeekBarChangeListener(this);
		hs.sat = (short) seekBar.getProgress();

		// builder.setView(new ColorPickerView(getActivity(), l,
		// mInitialColor));

		
		// Create the AlertDialog object and return it
		return groupDialogView;
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
	}

	public void preview() {
		String[] states = { gson.toJson(hs) };
		((MainActivity) getActivity()).testMood(states);

	}

	@Override
	public Intent onCreateColor() {
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
}
