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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.state.api.BulbState;

public class NfcReaderActivity extends Activity implements
		OnCheckedChangeListener, OnClickListener {

	Gson gson = new Gson();
	String[] stateS = null;
	Integer[] bulbS = null;
	ToggleButton onButton;
	Button doneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_reader);

		onButton = (ToggleButton) this.findViewById(R.id.onToggleButton);
		onButton.setOnCheckedChangeListener(this);

		doneButton = (Button) this.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(this);

	}

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
				// System.out.println(data);
				data = data.substring(data.indexOf('?') + 1);
				// System.out.println(data);
				Pair<Integer[], BulbState[]> result = HueNfcEncoder
						.decode(data);
				bulbS = result.first;
				stateS = new String[result.second.length];

				for (int i = 0; i < result.second.length; i++) {
					stateS[i] = gson.toJson(result.second[i]);
					System.out.println(bulbS[i]);
				}
				TransmitGroupMood transmitter = new TransmitGroupMood(this,
						bulbS, stateS);
				transmitter.execute();
				if(gson.fromJson(stateS[0],BulbState.class)!=null)
					onButton.setChecked(gson.fromJson(stateS[0],BulbState.class).on);
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			initializeActionBar(true);

		}
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
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		BulbState bs = new BulbState();
		bs.on = isChecked;
		String[] bsRay = new String[] { gson.toJson(bs) };
		TransmitGroupMood tgm = new TransmitGroupMood(this, bulbS, bsRay);
		tgm.execute();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.doneButton:
			onBackPressed();
		}

	}
}
