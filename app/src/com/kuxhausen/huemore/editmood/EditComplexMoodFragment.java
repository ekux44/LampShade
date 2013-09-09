package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbState;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class EditComplexMoodFragment extends Fragment implements OnCreateMoodListener, OnItemClickListener, OnClickListener{
	
	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	Gson gson = new Gson();
	Button addChannel, addTimeslot;
	GridView gridview;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.edit_complex_mood, null);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			
			addChannel = (Button) myView.findViewById(R.id.addChannelButton);
			//int h = addChannel.getHeight();
			//int w = addChannel.getWidth();
			//addChannel.setRotation(270);
			//addChannel.setHeight(w);
			//addChannel.setWidth(h);
			addChannel.setOnClickListener(this);
			
			addTimeslot = (Button) myView.findViewById(R.id.addTimeslotButton);
			addTimeslot.setOnClickListener(this);
			
			moodRowArray = new ArrayList<MoodRow>();
			
			rayAdapter = new MoodRowAdapter(this.getActivity(), moodRowArray);
			
			
			gridview = (GridView) myView.findViewById(R.id.gridview);
		    gridview.setAdapter(rayAdapter);
	
		    gridview.setOnItemClickListener(this);
			
		    addState();
		    addState();
		    addState();
		    addState();
		}
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
	private void addState(int i) {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		moodRowArray.add(i, mr);
		rayAdapter.insert(mr, i);
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

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.addChannelButton:
			int width = gridview.getNumColumns();
			gridview.setNumColumns(1+width);
			for(int i = rayAdapter.getCount(); i>0; i-=width){
				addState(i);
			}
			break;
		case R.id.addTimeslotButton:
			for(int i = gridview.getNumColumns(); i>0; i--){
				addState();
			}
			break;
		}
	}

}
