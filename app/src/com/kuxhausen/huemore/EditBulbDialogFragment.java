package com.kuxhausen.huemore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditBulbDialogFragment extends DialogFragment {

	EditText nameEditText;
	int bulbNumber;
	Gson gson = new Gson();
	BulbsFragment bulbF;

	private GodObject parrentActivity;

	public void setBulbsFragment(BulbsFragment bf) {
		bulbF = bf;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			parrentActivity = (GodObject) activity;
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
		if (args != null && args.containsKey(InternalArguments.BULB_NAME)) {
			String groupName = args.getString(InternalArguments.BULB_NAME);
			bulbNumber = args.getInt(InternalArguments.BULB_NUMBER);
			nameEditText.setText(groupName);
		}
		
		BulbState bs = new BulbState();
		bs.alert = "lselect";
		bs.on = true;
		Mood m = Utils.generateSimpleMood(bs);
		
		Integer[] bulbS = { bulbNumber };
		NetworkMethods.PreformTransmitGroupMood(parrentActivity.getRequestQueue(), parrentActivity, parrentActivity, bulbS, m);
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						BulbAttributes bAttrs = new BulbAttributes();
						bAttrs.name = nameEditText.getText().toString();
						
						NetworkMethods.PreformSetBulbAttributes(parrentActivity.getRequestQueue(), parrentActivity, parrentActivity, bulbNumber, bAttrs);
						
						BulbState bs = new BulbState();
						bs.alert = "none";

						Mood m = Utils.generateSimpleMood(bs);
						
						Integer[] bulbS = { bulbNumber };
						NetworkMethods.PreformTransmitGroupMood(parrentActivity.getRequestQueue(), parrentActivity, parrentActivity, bulbS, m);
						
						bulbF.refreshList();
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
