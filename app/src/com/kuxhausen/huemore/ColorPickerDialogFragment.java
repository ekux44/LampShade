package com.kuxhausen.huemore;

import com.google.gson.Gson;
import com.kuxhausen.huemore.state.HueState;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerDialogFragment extends DialogFragment implements OnSeekBarChangeListener {

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
    
    public void onSuccessExit(int color, int hues){
    	hs.hue = hues;
    	Intent i = new Intent();
    	i.putExtra("HueState", gson.toJson(hs));
    	getTargetFragment().onActivityResult(getTargetRequestCode(),color, i);
    }
    
    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hs = new HueState();
        hs.on=true;
        hs.bri= 128;
        mInitialColor = 0;

        
        mListener = new OnColorChangedListener() {
            public void colorChanged(int color, int hues) {
            	Log.e("asdf","onColorChanged");
            	hs.hue = hues;
            	preview();
            }
        };

        
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_color_dialog,
				null);
		cpv =((ColorPickerView)groupDialogView.findViewById(R.id.colorWheel));
		cpv.setOnColorChangedListener(mListener);
		builder.setView(groupDialogView);
        
		seekBar = (SeekBar)groupDialogView.findViewById(R.id.saturationBar);
		seekBar.setOnSeekBarChangeListener(this);
		hs.sat = (short)seekBar.getProgress();
		
        //builder.setView(new ColorPickerView(getActivity(), l, mInitialColor));
        builder.setTitle("Pick a Color");
        builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onSuccessExit(cpv.getColor(), cpv.getHue());
						
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
        
        
        // Create the AlertDialog object and return it
     	return builder.create();
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
		hs.sat = (short)seekBar.getProgress();
		preview();		
	}


	public void setPreviewGroups(Integer[] bulbs) {
		bulbS = bulbs;
	}

	public void preview() {
		String[] states = {gson.toJson(hs)};
		((MainActivity)getActivity()).testMood(bulbS,states);
		
	}

}
