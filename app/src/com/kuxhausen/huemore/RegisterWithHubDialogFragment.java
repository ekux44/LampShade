package com.kuxhausen.huemore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class RegisterWithHubDialogFragment extends DialogFragment {

	public static final long length_in_milliseconds = 15000;
	public static final long period_in_milliseconds = 1000;
	public ProgressBar progressBar;
	public CountDownTimer countDownTimer;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View registerWithHubView = inflater.inflate(R.layout.register_with_hub,
				null);
		builder.setView(registerWithHubView);
		progressBar = (ProgressBar) registerWithHubView.findViewById(R.id.timerProgressBar);
		
		countDownTimer = new CountDownTimer(length_in_milliseconds,period_in_milliseconds) {
	        private boolean warned = false;
	        @Override
	        public void onTick(long millisUntilFinished) {
	           progressBar.setProgress( (int) (((length_in_milliseconds-millisUntilFinished)*100.0)/length_in_milliseconds));
	        }

	        @Override
	        public void onFinish() {
	            // do whatever when the bar is full
	        }
	    };
	    countDownTimer.start();
		
		// Create the AlertDialog object and return it
		return builder.create();
	}
}