package com.kuxhausen.huemore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditBulbDialogFragment extends DialogFragment {

	EditText nameEditText;
	NetworkBulb netBulb;

	private NetworkManagedSherlockFragmentActivity parrentActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			parrentActivity = (NetworkManagedSherlockFragmentActivity) activity;
		} catch (ClassCastException e) {
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View editBulbDialogView = inflater.inflate(R.layout.edit_bulb_dialog,
				null);

		builder.setView(editBulbDialogView);

		nameEditText = (EditText) editBulbDialogView
				.findViewById(R.id.editText1);

		Bundle args = this.getArguments();
		Long netBulbBaseId = args.getLong(InternalArguments.NET_BULB_DATABASE_ID);
		
		netBulb = parrentActivity.getService().getDeviceManager().getNetworkBulb(netBulbBaseId);
		
		BulbState bs = new BulbState();
		bs.alert = "lselect";
		bs.on = true;
		
		netBulb.setState(bs);
		
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						netBulb.rename(nameEditText.getText().toString());
						
						BulbState bs = new BulbState();
						bs.alert = "lselect";
						bs.on = true;
						
						netBulb.setState(bs);
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}

}
