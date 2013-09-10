package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditLoopMoodFragment extends SherlockListFragment implements
		OnClickListener, OnCreateMoodListener, OnMenuItemClickListener {

	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	Gson gson = new Gson();

	Spinner transitionSpinner;
	int[] transitionValues;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		moodRowArray = new ArrayList<MoodRow>();

		rayAdapter = new MoodRowAdapter(this.getActivity(), moodRowArray, null);
		setListAdapter(rayAdapter);

		View myView = inflater.inflate(R.layout.edit_loop_mood, null);
		
		
		transitionSpinner = (Spinner) myView
				.findViewById(R.id.transitionSpinner);
		// Create an ArrayAdapter using the string array and a default
		// spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.transition_names_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		transitionSpinner.setAdapter(adapter);

		transitionValues = getActivity().getResources().getIntArray(
				R.array.transition_values_array);
		
		
		Button addColor = (Button) myView.findViewById(R.id.addColor);
		addColor.setOnClickListener(this);

		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			Mood mood = Utils.getMoodFromDatabase(args.getString(InternalArguments.MOOD_NAME), this.getActivity());
			for (Event e : mood.events) {
				MoodRow mr = new MoodRow();
				mr.hs = e.state;
				if (e.state.ct != null) {
					mr.color = Color.WHITE;
				} else {
					float[] hsv = { (e.state.hue * 360) / 65535, e.state.sat / 255f, 1 };
					mr.color = Color.HSVToColor(hsv);

				}
				moodRowArray.add(mr);
				rayAdapter.add(mr);
			}
		}
		return myView;
	}
	
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(getListView());
	}

	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		moodRowArray.add(mr);
		rayAdapter.add(mr);
		EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
		// cpdf.setPreviewGroups(bulbS);//TODO fix
		cpdf.setTargetFragment(this, rayAdapter.getPosition(mr));
		cpdf.show(getFragmentManager(),
				InternalArguments.FRAG_MANAGER_DIALOG_TAG);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		rayAdapter.getItem(requestCode).color = resultCode;
		rayAdapter.notifyDataSetChanged();
		rayAdapter.getItem(requestCode).hs = gson.fromJson(
				data.getStringExtra(InternalArguments.HUE_STATE),
				BulbState.class);

		String[] states = new String[moodRowArray.size()];
		for (int i = 0; i < moodRowArray.size(); i++) {
			states[i] = gson.toJson(moodRowArray.get(i).hs);
		}
		
		Utils.transmit(this.getActivity(), InternalArguments.ENCODED_TRANSIENT_MOOD, getMood(), ((GodObject)this.getActivity()).getBulbs(), null);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addColor:
			addState();
			break;
		}
	}

	private Mood getMood(){
		Integer transitionTime = null;
		if (transitionSpinner != null)
			transitionTime = transitionValues[transitionSpinner
					.getSelectedItemPosition()];
		
		Mood m = new Mood();
		m.usesTiming = true;
		m.numChannels = 1;
		m.timeAddressingRepeatPolicy = false;
		Event[] eRay = new Event[m.numChannels];
		for(int i = 0; i<eRay.length; i++){
			Event e = new Event();
			e.channel = 0;
			e.time = i*transitionTime;
			e.state = moodRowArray.get(i).hs;
			eRay[i] = e;
		}
		m.events = eRay;
		return m;
	}
	
	@Override
	public void onCreateMood(String groupname) {
		
		
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, groupname);
		mNewValues.put(DatabaseDefinitions.MoodColumns.STATE, HueUrlEncoder.encode(getMood()));
		
		getActivity().getContentResolver().insert(
				DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
		Bundle args = new Bundle();
		args.putString(InternalArguments.PREVIOUS_STATE,
				gson.toJson(moodRowArray.get(position).hs));
		cpdf.setArguments(args);
		cpdf.show(getFragmentManager(), "dialog");
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);

		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
		
		for(int i = 0; i< menu.size(); i++)
			menu.getItem(i).setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    int position = info.position;
	    
		switch (item.getItemId()) {

		case R.id.contextstatemenu_delete:
			rayAdapter.remove(moodRowArray.get(position));
			moodRowArray.remove(moodRowArray.get(position));
			return true;
		case R.id.contextstatemenu_edit:
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			Bundle args = new Bundle();
			args.putString(InternalArguments.PREVIOUS_STATE,
					gson.toJson(moodRowArray.get(position).hs));
			cpdf.setArguments(args);
			cpdf.show(getFragmentManager(), "dialog");
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
