package com.kuxhausen.huemore.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class NfcReadRouterActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context con = getApplicationContext();
		String encodedMood = NfcReaderActivity.getGroupMoodBrightnessFromNdef(this.getIntent());
		Intent srv = new Intent(con, MoodExecuterService.class);
		srv.putExtra(InternalArguments.ENCODED_MOOD, encodedMood);
		con.startService(srv);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
		if(settings.getBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, true)){
			Intent i = new Intent(this, NfcReaderActivity.class);
			i.putExtra(InternalArguments.ENCODED_MOOD, encodedMood);
			startActivity(i);
		}
		finish();
	}
}
