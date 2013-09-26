package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.kuxhausen.huemore.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class TimeslotDuration implements OnItemSelectedListener {
	
	private int duration;
	private int id;
	private Spinner spin;
	ArrayList<Integer> timeslotValues;
	
	public TimeslotDuration(Activity activity, int id){
		LayoutInflater inflater = activity.getLayoutInflater();
		spin = (Spinner)inflater.inflate(R.layout.timeslot_spinner, null);
		spin.setId(id);
		spin.setOnItemSelectedListener(this);
		
		timeslotValues = new ArrayList<Integer>();
		int[] values = activity.getResources().getIntArray(
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
			//TODO launch custom time dialog
		}
			
	}

	@Override
	public void onNothingSelected (AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
