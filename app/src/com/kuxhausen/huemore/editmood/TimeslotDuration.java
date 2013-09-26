package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditTimeslotDialogFragment.TimeslotTimeResult;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class TimeslotDuration implements OnItemSelectedListener, TimeslotTimeResult{
	
	private int duration;
	private int id;
	private Spinner spin;
	private ArrayList<Integer> timeslotValues;
	private SherlockFragment frag;
	
	public TimeslotDuration(SherlockFragment frag, int id){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		spin = (Spinner)inflater.inflate(R.layout.timeslot_spinner, null);
		spin.setId(id);
		spin.setOnItemSelectedListener(this);
		
		timeslotValues = new ArrayList<Integer>();
		int[] values = frag.getActivity().getResources().getIntArray(
				R.array.timeslot_values_array);
		for(int v : values)
			timeslotValues.add(v);
	}
	
	public Spinner getSpinner(){
		return spin;
	}
	
	public int getId(){
		return id;
	}
	
	public void setDuration(int duration){
		if(timeslotValues.indexOf(duration)>-1);
			spin.setSelection(timeslotValues.indexOf(duration));		
		this.duration = duration;
	}
	public int getDuration(){
		return duration;
	}
	
	@Override
	public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
		if(position<timeslotValues.size()-1){
			duration=(timeslotValues.get(position));
		}
		else{
			EditTimeslotDialogFragment etdf = new EditTimeslotDialogFragment();
			etdf.setTimeslotTimeResultListener(this);
			Bundle args = new Bundle();
			args.putInt(InternalArguments.DURATION_TIME, duration/10);
			etdf.setArguments(args);
			etdf.show(frag.getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		}
			
	}

	@Override
	public void onNothingSelected (AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
