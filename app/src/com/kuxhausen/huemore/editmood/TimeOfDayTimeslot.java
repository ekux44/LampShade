package com.kuxhausen.huemore.editmood;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;

public class TimeOfDayTimeslot implements TimeslotDuration, OnClickListener{

	Calendar cal;
	private SherlockFragment frag;
	private Button t;
	
	public TimeOfDayTimeslot(SherlockFragment frag, int id){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (Button)inflater.inflate(R.layout.timeslot_date, null);
		t.setOnClickListener(this);
		cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
	}

	
	public String getTime() {
		return DateFormat.getTimeFormat(frag.getActivity()).format(cal.getTime());
	}
	
	@Override
	public View getView() {
		t.setText(getTime());
		
		return t;
	}

	@Override
	public void setDuration(int duration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setTime(int hour, int minute){
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
	}


	@Override
	public void onClick(View v) {
		TimePickerFragment etdf = new TimePickerFragment();
		etdf.t = this;
		//etdf.setTimeOfDayResultListener(this);
		etdf.show(frag.getFragmentManager(),InternalArguments.FRAG_MANAGER_DIALOG_TAG);
	}

	public static class TimePickerFragment extends SherlockDialogFragment implements TimePickerDialog.OnTimeSetListener {

		TimeOfDayTimeslot t; 
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			int hour = t.cal.get(Calendar.HOUR_OF_DAY);
			int minute = t.cal.get(Calendar.MINUTE);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			t.setTime(hourOfDay, minute);
		}
	}
}
