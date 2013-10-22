package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class EditTimeOfDayDialogFragment extends SherlockDialogFragment implements OnClickListener {

	public interface TimeOfDayResult{
		public abstract void setTimeOfDay(int duration);
	}
	
	TimePicker picker;
	private TimeOfDayResult listener;
	
	public void setTimeOfDayResultListener(TimeOfDayResult l){
		listener = l;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.edit_timeslot_time_of_day, container, false);
	
		picker = (TimePicker)myView.findViewById(R.id.timePicker);
		
		
		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		
		this.getDialog().setTitle(
				getActivity().getString(R.string.daily_start_time));
		return myView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okay:
			listener.setTimeOfDay(0);
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
