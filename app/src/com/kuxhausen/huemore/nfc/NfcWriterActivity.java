package com.kuxhausen.huemore.nfc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.kuxhausen.huemore.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.nfc.Tag;
import android.nfc.FormatException;

public class NfcWriterActivity extends Activity implements OnClickListener {
	private Button sendButton;
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	IntentFilter writeTagFilters[];
	boolean writeMode;
	Tag myTag;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_writer);
		context = this;
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		sendButton = (Button) this.findViewById(R.id.writeToTagButton);
		sendButton.setOnClickListener(this);

		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.writeToTagButton:
			try {
				if (myTag == null) {
					Toast.makeText(context,
							context.getString(R.string.nfc_tag_not_detected),
							Toast.LENGTH_SHORT).show();
				} else {
					write(getMessage(), myTag);
					Toast.makeText(context,
							context.getString(R.string.nfc_tag_write_success),
							Toast.LENGTH_SHORT).show();
				}
			} catch (IOException e) {
				Toast.makeText(context,
						context.getString(R.string.nfc_tag_write_fail),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			} catch (FormatException e) {
				Toast.makeText(context,
						context.getString(R.string.nfc_tag_write_fail),
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			break;
		}
	}

	private String getMessage() {
		String url = "kuxhausen.com/HueMore/nfc";
		String jSon = "{\"hue\": 50000,\"on\": true,\"bri\": 200}";
		return url+jSon;
	}

	private void write(String text, Tag tag) throws IOException,
			FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}

	private NdefRecord createRecord(String text)
			throws UnsupportedEncodingException {
		byte[] uriField = text.getBytes(Charset.forName("US-ASCII"));
		byte[] payload = new byte[uriField.length + 1]; // add 1 for the URI
														// Prefix
		payload[0] = 0x01; // prefixes http://www. to the URI
		System.arraycopy(uriField, 0, payload, 1, uriField.length); // appends
																	// URI to
																	// payload
		NdefRecord rtdUriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_URI, new byte[0], payload);

		return rtdUriRecord;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Toast.makeText(
					this,
					this.getString(R.string.nfc_tag_detected)
							+ myTag.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume() {
		super.onResume();
		WriteModeOn();
	}

	private void WriteModeOn() {
		writeMode = true;
		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					writeTagFilters, null);
	}

	private void WriteModeOff() {
		writeMode = false;
		if (nfcAdapter != null)
			nfcAdapter.disableForegroundDispatch(this);
	}

}
