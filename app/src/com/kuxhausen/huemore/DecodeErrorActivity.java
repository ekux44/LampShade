package com.kuxhausen.huemore;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class DecodeErrorActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle b){
		setContentView(R.layout.decoder_error_activity);
		boolean bool = getIntent().getExtras().getBoolean(InternalArguments.DECODER_ERROR_UPGRADE);
		if(bool){
			
		} else{
			
		}
	}
}
