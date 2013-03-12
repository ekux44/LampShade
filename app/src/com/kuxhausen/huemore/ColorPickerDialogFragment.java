package com.kuxhausen.huemore;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class ColorPickerDialogFragment extends DialogFragment {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;


    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        mInitialColor = 0;
        
        
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                //mListener.colorChanged(color);
                dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_color_dialog,
				null);
		builder.setView(groupDialogView);
        
        //builder.setView(new ColorPickerView(getActivity(), l, mInitialColor));
        builder.setTitle("pick a color");
        
        
        
        // Create the AlertDialog object and return it
     	return builder.create();
    }

	

}
