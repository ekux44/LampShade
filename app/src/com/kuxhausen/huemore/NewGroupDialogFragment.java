package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class NewGroupDialogFragment extends DialogFragment{

	public static String[] dummyArrayItems = { "1", "2", "3", "4", "5" };
	ListView bulbsListView;
	ArrayAdapter<String> rayAdapter;
	EditText nameEditText;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_group_dialog,
				null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		bulbsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		rayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_multiple_choice,
				dummyArrayItems);
		bulbsListView.setAdapter(rayAdapter);
		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);

		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						ArrayList<String> checkedBulbs = new ArrayList<String>();
						SparseBooleanArray set = bulbsListView
								.getCheckedItemPositions();
						for (int i = 0; i < rayAdapter.getCount(); i++) {
							if (set.get(i)) {
								checkedBulbs.add(rayAdapter.getItem(i));
							}
						}

						String groupname = nameEditText.getText().toString();

						for (int i = 0; i < checkedBulbs.size(); i++) {
							// Defines a new Uri object that receives the result
							// of the insertion
							Uri mNewUri;

							// Defines an object to contain the new values to
							// insert
							ContentValues mNewValues = new ContentValues();

							/*
							 * Sets the values of each column and inserts the
							 * word. The arguments to the "put" method are
							 * "column name" and "value"
							 */
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.GROUP,
									groupname);
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.BULB,
									Integer.parseInt(checkedBulbs.get(i)));
							mNewValues
									.put(DatabaseDefinitions.GroupColumns.PRECEDENCE,
											i);

							mNewUri = getActivity()
									.getContentResolver()
									.insert(DatabaseDefinitions.GroupColumns.GROUPS_URI,
											mNewValues // the values to insert
									);
							Log.i("contentAdded", "groupname:" + groupname
									+ " bulb:" + checkedBulbs.get(i));
						}

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
