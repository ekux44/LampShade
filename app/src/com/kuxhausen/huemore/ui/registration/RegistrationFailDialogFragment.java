package com.kuxhausen.huemore.ui.registration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class RegistrationFailDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton(R.string.auto_discover,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						DiscoverHubDialogFragment dhdf = new DiscoverHubDialogFragment();
						dhdf.show(getFragmentManager(),
								InternalArguments.FRAG_MANAGER_DIALOG_TAG);

						dismiss();
					}
				});
		builder.setNeutralButton(R.string.advanced,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						ManualDiscoverHubDialogFragment mrwhdf = new ManualDiscoverHubDialogFragment();
						mrwhdf.show(getFragmentManager(),
								InternalArguments.FRAG_MANAGER_DIALOG_TAG);
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
