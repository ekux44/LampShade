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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class NewColorTempFragment extends Fragment implements
		OnSeekBarChangeListener, OnCreateColorListener {

	private int mInitialColor;
	private BulbState hs;
	Gson gson = new Gson();
	SeekBar seekBar;
	EditText tempEditText;
	int seekBarOffset = 2000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();

		hs = new BulbState();
		hs.on = true;
		mInitialColor = 0;

		View groupDialogView = inflater.inflate(R.layout.edit_temp_color, null);

		seekBar = (SeekBar) groupDialogView.findViewById(R.id.temperatureBar);
		seekBar.setOnSeekBarChangeListener(this);
		hs.sat = (short) seekBar.getProgress();

		tempEditText = (EditText) groupDialogView
				.findViewById(R.id.temperatureText);
		tempEditText.setVisibility(View.VISIBLE);

		tempEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					int temp = Integer.parseInt((tempEditText.getText()
							.toString()));
					temp = Math.max(temp, 0);
					temp = Math.min(temp, seekBarOffset + seekBar.getMax());
					seekBar.setProgress(temp - seekBarOffset);
					hs.ct = ((1000000 / temp));
					preview();
				}
				return false;
			}

		});

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
		hs.ct = ((1000000 / (seekBarOffset + seekBar.getProgress())));
		tempEditText.setText("" + (seekBarOffset + seekBar.getProgress()));
		preview();
	}

	public void preview() {
		String[] states = { gson.toJson(hs) };
		((MainActivity) getActivity()).testMood(states);

	}

	@Override
	public Intent onCreateColor() {
		Intent i = new Intent();
		i.putExtra("HueState", gson.toJson(hs));
		i.putExtra("Color", 0xffffffff);
		return i;
	}

}
