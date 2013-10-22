package com.kuxhausen.huemore.editmood;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockDialogFragment;
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

	private Calendar cal;
	private EditAdvancedMoodFragment frag;
	private Button t;
	
	public TimeOfDayTimeslot(EditAdvancedMoodFragment frag, int id, int position){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (Button)inflater.inflate(R.layout.timeslot_date, null);
		t.setOnClickListener(this);
		
		Calendar previousTimeslotCal = frag.computeMinimumValue(position);
		cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 1+previousTimeslotCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, previousTimeslotCal.get(Calendar.MINUTE));
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
	public void setDuration(int offsetWithinDayInDeciSeconds) {		
		Calendar startOfDay = Calendar.getInstance();
		startOfDay.set(Calendar.HOUR_OF_DAY, 0);
		startOfDay.set(Calendar.SECOND, 0);
		startOfDay.set(Calendar.MILLISECOND, 0);
		
		cal.setTimeInMillis( startOfDay.getTimeInMillis() + (offsetWithinDayInDeciSeconds*100l));
	}

	@Override
	public int getDuration() {
		Calendar startOfDay = Calendar.getInstance();
		startOfDay.set(Calendar.HOUR_OF_DAY, 0);
		startOfDay.set(Calendar.SECOND, 0);
		startOfDay.set(Calendar.MILLISECOND, 0);
		Long offsetWithinTheDayInMilis = cal.getTimeInMillis() - startOfDay.getTimeInMillis();
		
		return (int) (offsetWithinTheDayInMilis/100);
	}
	
	public Calendar getCal(){
		return cal;
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
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.MILLISECOND, 0);
			
			Calendar previousTimeslotCal = t.frag.computeMinimumValue(t.frag.timeslotDuration.indexOf(t));
			if(previousTimeslotCal.before(c))
				t.cal=c;
			else{
				t.cal.set(Calendar.HOUR_OF_DAY, previousTimeslotCal.get(Calendar.HOUR_OF_DAY));
				t.cal.set(Calendar.MINUTE, 1+previousTimeslotCal.get(Calendar.MINUTE));
				t.cal.set(Calendar.MILLISECOND, 0);
			}
			t.frag.redrawGrid();
		}
	}
}
