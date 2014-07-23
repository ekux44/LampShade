package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class EditTimeDialogFragment extends DialogFragment implements OnClickListener {

  EditText seconds, minutes;
  private RelativeStartTimeslot listener;

  public void setTimeslotTimeResultListener(RelativeStartTimeslot l) {
    listener = l;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.edit_timeslot_dialog, container, false);

    seconds = (EditText) myView.findViewById(R.id.secondsEditText);
    minutes = (EditText) myView.findViewById(R.id.minutesEditText);

    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
    cancelButton.setOnClickListener(this);
    Button okayButton = (Button) myView.findViewById(R.id.okay);
    okayButton.setOnClickListener(this);

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.DURATION_TIME)) {
      seconds.setText("" + args.getInt(InternalArguments.DURATION_TIME) % 60);
      minutes.setText("" + args.getInt(InternalArguments.DURATION_TIME) / 60);
    } else {
      seconds.setText("" + 0);
      minutes.setText("" + 0);
    }

    this.getDialog().setTitle(getActivity().getString(R.string.timed_mood_start_time));
    return myView;
  }

  private void acceptValues() {
    String s = seconds.getText().toString();
    String m = minutes.getText().toString();
    try {
      listener.setStartTime(((60 * Integer.parseInt(m)) + Integer.parseInt(s)) * 10);
    } catch (Exception e) {
    }
    this.dismiss();
    listener.frag.validate();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.okay:
        acceptValues();
        break;
      case R.id.cancel:
        this.dismiss();
        break;
    }
  }
}
