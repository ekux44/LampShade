package com.kuxhausen.huemore.nfc;

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

public class NfcReaderActivity extends NetworkManagedSherlockFragmentActivity implements
		OnCheckedChangeListener, OnClickListener {

	Integer[] bulbS;
	Mood m;
	Integer brightness;
	ToggleButton onButton;
	Button doneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_reader);

		onButton = (ToggleButton) this.findViewById(R.id.onToggleButton);

		doneButton = (Button) this.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				// msgs[0].

				byte[] payload = msgs[0].getRecords()[0].getPayload();
				byte identifierCode = payload[0];

				// String prefix = (0x01)+""; // you need to implement this one
				// String url = prefix +
				// new String(payload, 1, payload.length -1,
				// Charset.forName("US-ASCII"));

				String data = new String(payload, 1, payload.length - 1,
						Charset.forName("US-ASCII"));
				data = data.substring(data.indexOf('?') + 1);

				try {
					Pair<Integer[], Pair<Mood, Integer>> result = HueUrlEncoder.decode(data);
					bulbS = result.first;
					m = result.second.first;
					brightness = result.second.second;
					
					onButton.setOnCheckedChangeListener(null);
					Utils.transmit(this, m, bulbS, null, brightness);
					boolean on = false;
					if(m.events[0].state.on!=null && m.events[0].state.on)
						on=true;
					onButton.setChecked(on);
					onButton.setOnCheckedChangeListener(this);
				} catch (InvalidEncodingException e) {
					Intent i = new Intent(this,DecodeErrorActivity.class);
					i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, false);
					startActivity(i);
					this.finish();
				} catch (FutureEncodingException e) {
					Intent i = new Intent(this,DecodeErrorActivity.class);
					i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, true);
					startActivity(i);
					this.finish();
				}
			}
		}
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		BulbState bs = new BulbState();
		bs.on = isChecked;
		Mood m = Utils.generateSimpleMood(bs);		
		Utils.transmit(this, m, bulbS, null, brightness);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.doneButton:
			onBackPressed();
		}

	}
}
