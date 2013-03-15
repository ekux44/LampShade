package com.kuxhausen.huemore;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private ColorPickerView cpv;

    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        mInitialColor = 0;
        
        
        mListener = new OnColorChangedListener() {
            public void colorChanged(int color) {
            	getTargetFragment().onActivityResult(getTargetRequestCode(),color, null);
                //dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_color_dialog,
				null);
		cpv =((ColorPickerView)groupDialogView.findViewById(R.id.colorWheel));
		cpv.setOnColorChangedListener(mListener);
		builder.setView(groupDialogView);
        
        //builder.setView(new ColorPickerView(getActivity(), l, mInitialColor));
        builder.setTitle("Pick a Color");
        builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mListener.colorChanged(cpv.getColor());
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

	

}
