package com.kuxhausen.huemore.editmood;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TimeOfDayTimeslot implements TimeslotDuration{

	public static int hour;
	private SherlockFragment frag;
	private TextView t;
	public int myHour;
	
	public TimeOfDayTimeslot(SherlockFragment frag, int id){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (TextView)inflater.inflate(R.layout.timeslot_date, null);
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

}
