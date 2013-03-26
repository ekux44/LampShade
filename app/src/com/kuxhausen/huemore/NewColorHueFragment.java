package com.kuxhausen.huemore;

import com.google.gson.Gson;
import com.kuxhausen.huemore.NewColorPagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.HueState;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NewColorHueFragment extends Fragment implements
		OnSeekBarChangeListener, OnCreateColorListener {

	public interface OnColorChangedListener {
		void colorChanged(int color, int hue);
	}

	private OnColorChangedListener mListener;
	private int mInitialColor;
	private ColorPickerView cpv;
	private HueState hs;
	Gson gson = new Gson();
	SeekBar seekBar;
	Integer[] bulbS;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hs = new HueState();
		hs.on = true;
		hs.bri = 128;
		mInitialColor = 0;

		mListener = new OnColorChangedListener() {
			@Override
			public void colorChanged(int color, int hues) {
				hs.hue = hues;
				preview();
			}
		};

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color,
				null);
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

	public void setPreviewGroups(Integer[] bulbs) {
		bulbS = bulbs;
	}

	public void preview() {
		String[] states = { gson.toJson(hs) };
		((MainActivity) getActivity()).testMood(bulbS, states);

	}

	@Override
	public Intent onCreateMood() {
		hs.hue = cpv.getHue();
		Intent i = new Intent();
		i.putExtra("HueState", gson.toJson(hs));
		i.putExtra("Color", cpv.getColor());
		return i;
	}

}
