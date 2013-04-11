package com.kuxhausen.huemore.ui.registration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.R;

public class RegistrationFailDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton(R.string.auto_discover,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
						rwhdf.show(getFragmentManager(), "dialog");

						dismiss();
					}
				});
		builder.setNeutralButton(R.string.advanced,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						ManualRegisterWithHubDialogFragment mrwhdf = new ManualRegisterWithHubDialogFragment();
						mrwhdf.show(getFragmentManager(), "dialog");
						dismiss();
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						dismiss();
					}
				});
		builder.setMessage(R.string.register_fail);
		return builder.create();
	}
}
