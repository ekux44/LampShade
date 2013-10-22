package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class EditOffsetDialogFragment extends SherlockDialogFragment implements OnClickListener {

	public interface TimeslotTimeResult{
		public abstract void setDuration(int duration);
	}
	
	EditText seconds;
	private TimeslotTimeResult listener;
	
	public void setTimeslotTimeResultListener(TimeslotTimeResult l){
		listener = l;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.edit_timeslot_dialog, container, false);
	
		seconds = (EditText)myView.findViewById(R.id.secondsEditText);
		
		
		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		
		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.DURATION_TIME)) {
			seconds.setText(""+args.getInt(InternalArguments.DURATION_TIME));
		}
		
		this.getDialog().setTitle(
				getActivity().getString(R.string.grid_col_title_timeslot));
		return myView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okay:
			String s = seconds.getText().toString();
			try{	
				int transitionTime = Integer.parseInt(s) * 10;
				transitionTime = Math.min(transitionTime, 36000);
				listener.setDuration(transitionTime);
			} catch (Exception e){
			}
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
