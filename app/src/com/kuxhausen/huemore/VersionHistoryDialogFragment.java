package com.kuxhausen.huemore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class VersionHistoryDialogFragment extends DialogFragment{

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Major Update History");
		builder.setItems(R.array.major_update_history, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            // The 'which' argument contains the index position
            // of the selected item
        }
		});
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}

}
