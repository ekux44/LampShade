package com.kuxhausen.huemore.registration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.NetworkMethods;

public class RegistrationSuccessDialogFragment extends DialogFragment {
	NetworkManagedSherlockFragmentActivity ma;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ma = (NetworkManagedSherlockFragmentActivity) this.getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						if (ma.getBulbListenerFragment() != null) {
							
							NetworkMethods.PreformGetBulbList(ma.getService(), ma.getBulbListenerFragment());
						}
					}
				});
		builder.setMessage(R.string.register_success);
		return builder.create();
	}
}
