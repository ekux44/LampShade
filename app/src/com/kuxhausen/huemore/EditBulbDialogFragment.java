package com.kuxhausen.huemore;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.SetBulbAttributes;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.Bulb;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditBulbDialogFragment extends DialogFragment {

	EditText nameEditText;
	int bulbNumber;
	Gson gson = new Gson();
	BulbsFragment bulbF;
	
	public void setBulbsFragment(BulbsFragment bf){
		bulbF=bf;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View editBulbDialogView = inflater.inflate(R.layout.edit_bulb_dialog,
				null);
		
		builder.setView(editBulbDialogView);

		nameEditText = (EditText) editBulbDialogView.findViewById(R.id.editText1);

		Bundle args = this.getArguments();
		if(args!=null && args.containsKey(InternalArguments.BULB_NAME)){
			String groupName = args.getString(InternalArguments.BULB_NAME);
			bulbNumber = args.getInt(InternalArguments.BULB_NUMBER);
			nameEditText.setText(groupName);
		}

		BulbState bs = new BulbState();
		bs.alert = "lselect";
		bs.on = true;
		String[] moodS = {gson.toJson(bs)};
		Integer[] bulbS = {bulbNumber};
		TransmitGroupMood tgm = new TransmitGroupMood(getActivity(), bulbS, moodS);
		tgm.execute();
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						BulbAttributes bAttrs = new BulbAttributes();
						bAttrs.name = nameEditText.getText().toString();
						SetBulbAttributes sba = new SetBulbAttributes(getActivity(), bulbNumber, bAttrs);
						sba.execute();
						
						BulbState bs = new BulbState();
						bs.alert = "none";
						String[] moodS = {gson.toJson(bs)};
						Integer[] bulbS = {bulbNumber};
						TransmitGroupMood tgm = new TransmitGroupMood(getActivity(), bulbS, moodS);
						tgm.execute();
						
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
