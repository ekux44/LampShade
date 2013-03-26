package com.kuxhausen.huemore.ui.registration;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.string;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RegistrationSuccessDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
					}
				});
		builder.setMessage(R.string.register_success);
		return builder.create();
	}
}
