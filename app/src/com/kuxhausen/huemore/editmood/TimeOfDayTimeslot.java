package com.kuxhausen.huemore.editmood;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditTimeOfDayDialogFragment.TimeOfDayResult;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;


import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TimeOfDayTimeslot implements TimeslotDuration, OnClickListener, TimeOfDayResult{

	public static int hour;
	private SherlockFragment frag;
	private Button t;
	public int myHour;
	
	public TimeOfDayTimeslot(SherlockFragment frag, int id){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (Button)inflater.inflate(R.layout.timeslot_date, null);
		t.setOnClickListener(this);
		myHour = hour++;
	}

	
	public String getTime() {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, myHour);
		cal.set(Calendar.MINUTE, 0);
		
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


	@Override
	public void onClick(View v) {
		EditTimeOfDayDialogFragment etdf = new EditTimeOfDayDialogFragment();
		etdf.setTimeOfDayResultListener(this);
		etdf.show(frag.getFragmentManager(),InternalArguments.FRAG_MANAGER_DIALOG_TAG);
	}


	@Override
	public void setTimeOfDay(int duration) {
		// TODO Auto-generated method stub
		
	}

}
