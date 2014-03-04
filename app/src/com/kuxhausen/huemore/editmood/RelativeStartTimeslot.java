package com.kuxhausen.huemore.editmood;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RelativeStartTimeslot implements OnClickListener{

	final static int MAX_MOOD_EVENT_TIME = (((999)*60)+59)*10;
	
	/* in deci seconds */
	int moodEventTime;
	EditMoodStateGridFragment frag;
	private Button t;
	private int mPosition;
	
	public RelativeStartTimeslot(EditMoodStateGridFragment frag, int pos){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (Button)inflater.inflate(R.layout.timeslot_date, null);
		t.setOnClickListener(this);
		
		moodEventTime = 0;
		mPosition = pos;
		setStartTime(0);
	}

	public void validate(){
		setStartTime(moodEventTime);
	}
	
	public String getTime() {
		return getMinutes()+"m:"+getSeconds()+"s";
	}
	
	public View getView(int position) {
		mPosition = position;
		validate();
		t.setText(getTime());	
		return t;
	}

	public void setStartTime(int offsetWithinDayInDeciSeconds) {
		moodEventTime = Math.max(frag.computeMinimumValue(mPosition),Math.min(MAX_MOOD_EVENT_TIME,offsetWithinDayInDeciSeconds));
		t.setText(getTime());
	}

	public int getStartTime() {		
		return moodEventTime;
	}
	
	private int getSeconds(){
		return (moodEventTime/10)%60;
	}
	
	private int getMinutes(){
		return moodEventTime/600;
	}
	
	public void onClick(View v) {
		EditTimeDialogFragment etdf = new EditTimeDialogFragment();
		etdf.setTimeslotTimeResultListener(this);
		Bundle args = new Bundle();
		args.putInt(InternalArguments.DURATION_TIME, moodEventTime/10);
		etdf.setArguments(args);
		etdf.show(frag.getFragmentManager(),
				InternalArguments.FRAG_MANAGER_DIALOG_TAG);
	}
}
