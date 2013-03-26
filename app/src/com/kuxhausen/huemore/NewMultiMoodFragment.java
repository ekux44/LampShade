package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.NewMoodPagerDialogFragment.OnCreateMoodListener;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class NewMultiMoodFragment extends ListFragment implements OnClickListener,
		OnCreateMoodListener {

	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	EditText nameEditText;
	Gson gson = new Gson();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		moodRowArray = new ArrayList<MoodRow>();

		View groupView = inflater.inflate(R.layout.edit_multi_mood, null);
		rayAdapter = new MoodRowAdapter(this.getActivity(), moodRowArray);
		setListAdapter(rayAdapter);

		nameEditText = (EditText) groupView.findViewById(R.id.editText1);

		Button addColor = (Button) groupView.findViewById(R.id.addColor);
		addColor.setOnClickListener(this);

		return groupView;
	}

	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		HueState example = new HueState();
		example.hue = 25000;
		example.sat = 254;
		example.on = true;
		mr.hs = example;
		moodRowArray.add(mr);
		rayAdapter.add(mr);
		NewColorPagerDialogFragment cpdf = new NewColorPagerDialogFragment();
		// cpdf.setPreviewGroups(bulbS);//TODO fix
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
		((MainActivity) getActivity()).testMood(states);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addColor:
			addState();
			break;
		}
	}

	@Override
	public void onCreateMood() {

		String groupname = nameEditText.getText().toString();
		for (int i = 0; i < moodRowArray.size(); i++) {
			// Defines a new Uri object that receives the result
			// of the insertion
			Uri mNewUri;

			// Defines an object to contain the new values to
			// insert
			ContentValues mNewValues = new ContentValues();

			/*
			 * Sets the values of each column and inserts the word. The
			 * arguments to the "put" method are "column name" and "value"
			 */
			mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, groupname);
			mNewValues.put(DatabaseDefinitions.MoodColumns.STATE,
					gson.toJson(moodRowArray.get(i).hs));
			mNewValues.put(DatabaseDefinitions.MoodColumns.PRECEDENCE, i);

			mNewUri = getActivity().getContentResolver().insert(
					DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues // the
																			// values
																			// to
																			// insert
					);
		}
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		NewColorPagerDialogFragment cpdf = new NewColorPagerDialogFragment();
		Bundle args = new Bundle();
		args.putString("PreviousState", gson.toJson(moodRowArray.get(position).hs));
		cpdf.setArguments(args);
		rayAdapter.remove(moodRowArray.get(position));
		moodRowArray.remove(moodRowArray.get(position));

	}
}
