package com.kuxhausen.huemore;

import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class DecodeErrorActivity extends SherlockActivity {

	TextView messageText;
	
	@Override
	public void onCreate(Bundle b){
		setContentView(R.layout.decoder_error_activity);
		
		messageText = (TextView)findViewById(R.id.messageTextView);
		
		boolean bool = getIntent().getExtras().getBoolean(InternalArguments.DECODER_ERROR_UPGRADE);
		if(bool){
			messageText.setText(R.string.update_required);
		} else{
			messageText.setText(R.string.corrupted_mood);
		}
	}
}
