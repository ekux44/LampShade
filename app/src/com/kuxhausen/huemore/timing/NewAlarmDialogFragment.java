package com.kuxhausen.huemore.timing;

import com.kuxhausen.huemore.*;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class NewAlarmDialogFragment extends DialogFragment implements
OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.edit_alarm_dialog, container, false);
		Bundle args = getArguments();

		this.getDialog().setTitle("New Alarm");
		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		
		return myView;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.okay:
			
			
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}

}
