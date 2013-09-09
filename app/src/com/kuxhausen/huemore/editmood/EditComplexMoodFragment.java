package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class EditComplexMoodFragment extends Fragment implements OnCreateMoodListener, OnItemClickListener{
	
	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	Gson gson = new Gson();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		moodRowArray = new ArrayList<MoodRow>();
		
		View myView = inflater.inflate(R.layout.edit_complex_mood, null);
		
		rayAdapter = new MoodRowAdapter(this.getActivity(), moodRowArray);
		
		
		GridView gridview = (GridView) myView.findViewById(R.id.gridview);
	    gridview.setAdapter(rayAdapter);

	    gridview.setOnItemClickListener(this);
		
	    addState();
	    addState();
	    addState();
	    addState();
	    addState();
		return myView;
	}

	@Override
	public void onCreateMood(String groupname) {
		// TODO Auto-generated method stub
		
	}
	
	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		moodRowArray.add(mr);
		rayAdapter.add(mr);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
		Bundle args = new Bundle();
		args.putString(InternalArguments.PREVIOUS_STATE,
				gson.toJson(moodRowArray.get(position).hs));
		cpdf.setArguments(args);
		cpdf.show(getFragmentManager(), "dialog");
	}

}
