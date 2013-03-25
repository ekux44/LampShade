package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.GroupSelectorDialogFragment.OnGroupSelectedListener;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.state.HueState;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class NewMoodDialogFragment extends DialogFragment implements
		OnClickListener, OnKeyListener, OnGroupSelectedListener {

	ListView bulbsListView;
	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	EditText nameEditText;
	Integer[] bulbS;
	Gson gson = new Gson();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		moodRowArray = new ArrayList<MoodRow>();
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater
				.inflate(R.layout.edit_mood_dialog, null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		rayAdapter = new MoodRowAdapter(this.getActivity(), moodRowArray);
		bulbsListView.setAdapter(rayAdapter);
		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);

		Button enablePreview = (Button) groupDialogView
				.findViewById(R.id.previewButton);
		enablePreview.setOnClickListener(this);

		Button addColor = (Button) groupDialogView.findViewById(R.id.addColor);
		addColor.setOnClickListener(this);

		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String groupname = nameEditText.getText().toString();
						for (int i = 0; i < moodRowArray.size(); i++) {
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
									DatabaseDefinitions.MoodColumns.MOOD,
									groupname);
							mNewValues.put(
									DatabaseDefinitions.MoodColumns.STATE,
									gson.toJson(moodRowArray.get(i).hs));
							mNewValues.put(
									DatabaseDefinitions.MoodColumns.PRECEDENCE,
									i);

							mNewUri = getActivity()
									.getContentResolver()
									.insert(DatabaseDefinitions.MoodColumns.MOODS_URI,
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

	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		HueState example = new HueState();
		example.hue = 25000;
		example.sat = 254;
		example.bri = 128;
		example.on = true;
		mr.hs = example;
		moodRowArray.add(mr);
		rayAdapter.add(mr);
		ColorPickerDialogFragment cpdf = new ColorPickerDialogFragment();
		cpdf.setPreviewGroups(bulbS);
		cpdf.setTargetFragment(this, rayAdapter.getPosition(mr));
		cpdf.show(getFragmentManager(), "dialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		rayAdapter.getItem(requestCode).color = resultCode;
		rayAdapter.notifyDataSetChanged();
		rayAdapter.getItem(requestCode).hs = gson.fromJson(
				data.getStringExtra("HueState"), HueState.class);

		String[] states = new String[moodRowArray.size()];
		for (int i = 0; i < moodRowArray.size(); i++) {
			states[i] = gson.toJson(moodRowArray.get(i).hs);
		}
		((MainActivity) getActivity()).testMood(bulbS, states);

	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.FLAG_EDITOR_ACTION:
				addState();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addColor:
			addState();
			break;
		case R.id.previewButton:
			GroupSelectorDialogFragment gsdf = new GroupSelectorDialogFragment();
			gsdf.setOnGroupSelectedListener(this);
			gsdf.show(getFragmentManager(), "dialog");
			break;
		}
	}

	@Override
	public void groupSelected(String group) {
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { group };
		Cursor cursor = getActivity().getContentResolver().query(
				DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use the
																	// default
																	// content
																	// URI
																	// for the
																	// provider.
				groupColumns, // Return the note ID and title for each note.
				GroupColumns.GROUP + "=?", // selection clause
				gWhereClause, // selection clause args
				null // Use the default sort order.
				);

		ArrayList<Integer> groupStates = new ArrayList<Integer>();
		while (cursor.moveToNext()) {

			groupStates.add(cursor.getInt(0));
		}
		bulbS = groupStates.toArray(new Integer[groupStates.size()]);
	}
}
