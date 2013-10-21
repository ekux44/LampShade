package com.kuxhausen.huemore;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
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

import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity.OnServiceConnectedListener;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.api.Bulb;

public class EditGroupDialogFragment extends DialogFragment implements
		OnBulbListReturnedListener, OnServiceConnectedListener {

	ArrayList<String> bulbNameList;
	ListView bulbsListView;
	ArrayAdapter<String> rayAdapter;
	EditText nameEditText;
	Bulb[] bulbArray;
	HashMap<String, Integer> nameToBulb;
	Boolean[] preChecked;
	String initialName;

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
		bulbNameList = new ArrayList<String>();
		nameToBulb = new HashMap<String, Integer>();

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_group_dialog,
				null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		bulbsListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		rayAdapter = new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_list_item_multiple_choice, bulbNameList);
		bulbsListView.setAdapter(rayAdapter);
		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);

		parrentActivity.registerOnServiceConnectedListener(this);

		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.GROUP_NAME)) {
			String groupName = args.getString(InternalArguments.GROUP_NAME);

			// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = { groupName };
			Cursor groupCursor = this.getActivity().getContentResolver()
					.query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use
																			// the
																			// default
																			// content
																			// URI
																			// for
																			// the
																			// provider.
							groupColumns, // Return the note ID and title for
											// each note.
							GroupColumns.GROUP + "=?", // selection clause
							gWhereClause, // selection clause args
							null // Use the default sort order.
					);

			ArrayList<Integer> groupStates = new ArrayList<Integer>();
			while (groupCursor.moveToNext()) {
				groupStates.add(groupCursor.getInt(0));
			}
			Integer[] bulbS = groupStates.toArray(new Integer[groupStates
					.size()]);
			preChecked = new Boolean[50];
			for (int checkedSpot : bulbS) {
				preChecked[checkedSpot - 1] = true;// have to account by the off
													// by one in bulb Numbers
			}
			nameEditText.setText(groupName);
			initialName = groupName;
		}

		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						// if there was a previous mood we're editing, remove it
						if (initialName != null) {
							String groupSelect = GroupColumns.GROUP + "=?";
							String[] groupArg = { initialName };
							getActivity()
									.getContentResolver()
									.delete(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
											groupSelect, groupArg);
						}

						ArrayList<Integer> checkedBulbs = new ArrayList<Integer>();
						SparseBooleanArray set = bulbsListView
								.getCheckedItemPositions();
						for (int i = 0; i < rayAdapter.getCount(); i++) {
							if (set.get(i)) {
								checkedBulbs.add(nameToBulb.get((rayAdapter
										.getItem(i))));
							}
						}

						String groupName = nameEditText.getText().toString();
						if(groupName==null || groupName.length()<1){
							SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
							int unnamedNumber = 1+settings.getInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, 0);
							Editor edit = settings.edit();
							edit.putInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, unnamedNumber);
							edit.commit();
							groupName = parrentActivity.getResources().getString(R.string.unnamed_group)+" "+unnamedNumber;
						}
						
						for (int i = 0; i < checkedBulbs.size(); i++) {
							
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
									groupName);
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.BULB,
									checkedBulbs.get(i));
							mNewValues
									.put(DatabaseDefinitions.GroupColumns.PRECEDENCE,
											i);

							getActivity().getContentResolver()
									.insert(DatabaseDefinitions.GroupColumns.GROUPS_URI,
											mNewValues // the values to insert
									);

						}

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

	@Override
	public void onListReturned(Bulb[] result) {
		if (result == null)
			return;
		bulbArray = result;

		for (int i = 0; i < bulbArray.length; i++) {
			// bulbNameList.add(bulb.name);
			Bulb bulb = bulbArray[i];
			bulb.number = i + 1;
			nameToBulb.put(bulb.name, bulb.number);
			rayAdapter.add(bulb.name);
			if (preChecked != null && preChecked[i] != null
					&& preChecked[i] == true)
				bulbsListView.setItemChecked(i, true);

		}

	}

	@Override
	public void onServiceConnected() {
		NetworkMethods.PreformGetBulbList(parrentActivity.getService(), this);
	}

}
