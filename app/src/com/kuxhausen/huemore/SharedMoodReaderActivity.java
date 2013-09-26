package com.kuxhausen.huemore;

import java.nio.charset.Charset;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.DecodeErrorActivity;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class SharedMoodReaderActivity extends NetworkManagedSherlockFragmentActivity implements OnClickListener {

	Gson gson = new Gson();
	Integer[] bulbS = null;
	Button doneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shared_mood_reader);


	}

	@Override
	public void onResume() {
		super.onResume();

		//todo extract url data
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void initializeActionBar(Boolean value) {
		try {
			this.getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		}
	}
}
