package com.kuxhausen.huemore.editmood;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditOffsetDialogFragment.TimeslotTimeResult;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RelativeStartTimeslot implements TimeslotStartTime, OnClickListener, TimeslotTimeResult{

	final static int MAX_MOOD_EVENT_TIME = (((999)*60)+59)*10;
	/* in deci seconds */
	int moodEventTime;
	private EditMoodStateGridFragment frag;
	private Button t;
	private int position;
	private boolean hangingLoopMode;
	
	public RelativeStartTimeslot(EditMoodStateGridFragment frag, int id, int pos, boolean hangingLoop){
		this.frag = frag;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		t = (Button)inflater.inflate(R.layout.timeslot_date, null);
		t.setOnClickListener(this);
		
		moodEventTime = 0;
		position = pos;
		hangingLoopMode = hangingLoop;
		setStartTime(0);
	}

	
	public String getTime() {
		return getMinutes()+"m:"+getSeconds()+"s";
	}
	
	@Override
	public View getView() {
		t.setText(getTime());	
		return t;
	}

	@Override
	public void setStartTime(int offsetWithinDayInDeciSeconds) {
		if(hangingLoopMode && frag.isResumed()){
			position = frag.timedTimeslotDuration.size();
		}
		moodEventTime = Math.max(frag.computeMinimumValue(position),Math.min(MAX_MOOD_EVENT_TIME,offsetWithinDayInDeciSeconds));
		t.setText(getTime());
	}

	@Override
	public int getStartTime() {		
		return moodEventTime;
	}
	
	private int getSeconds(){
		return (moodEventTime/10)%60;
	}
	
	private int getMinutes(){
		return moodEventTime/600;
	}
	
	@Override
	public void onClick(View v) {
		EditOffsetDialogFragment etdf = new EditOffsetDialogFragment();
		etdf.setTimeslotTimeResultListener(this);
		Bundle args = new Bundle();
		args.putInt(InternalArguments.DURATION_TIME, moodEventTime/10);
		etdf.setArguments(args);
		etdf.show(frag.getFragmentManager(),
				InternalArguments.FRAG_MANAGER_DIALOG_TAG);
	}
}
