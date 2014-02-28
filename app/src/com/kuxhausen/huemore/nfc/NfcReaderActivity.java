package com.kuxhausen.huemore.nfc;

import java.nio.charset.Charset;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
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
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class NfcReaderActivity extends NetworkManagedSherlockFragmentActivity implements OnCheckedChangeListener, OnClickListener {

	private Integer[] mBulbs;
	private Mood mood;
	private Integer mBrightness;
	private ToggleButton mOnButton;
	private Button mDoneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_reader);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mOnButton = (ToggleButton) this.findViewById(R.id.onToggleButton);

		mDoneButton = (Button) this.findViewById(R.id.doneButton);
		mDoneButton.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			Pair<Integer[], Pair<Mood, Integer>> result = NfcReaderActivity.getGroupMoodBrightnessFromNdef(this, getIntent());
			if(result==null){
				this.finish();
				return;
			}
			mBulbs = result.first;
			mood = result.second.first;
			mBrightness = result.second.second;
			
			mOnButton.setOnCheckedChangeListener(null);
			Utils.transmit(this, mood, mBulbs, null, null, mBrightness);
			boolean on = false;
			if(mood.events[0].state.on!=null && mood.events[0].state.on)
				on=true;
			mOnButton.setChecked(on);
			mOnButton.setOnCheckedChangeListener(this);
		}
	}
	
	public static Pair<Integer[],Pair<Mood,Integer>> getGroupMoodBrightnessFromNdef(Context c, Intent input){
		Parcelable[] rawMsgs = input.getParcelableArrayExtra(
				NfcAdapter.EXTRA_NDEF_MESSAGES);
		if (rawMsgs != null) {
			NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
			for (int i = 0; i < rawMsgs.length; i++) {
				msgs[i] = (NdefMessage) rawMsgs[i];
			}
			
			byte[] payload = msgs[0].getRecords()[0].getPayload();
			
			String data = new String(payload, 1, payload.length - 1,
					Charset.forName("US-ASCII"));
			data = data.substring(data.indexOf('?') + 1);

			try {
				return HueUrlEncoder.decode(data);
			} catch (InvalidEncodingException e) {
				Intent i = new Intent(c,DecodeErrorActivity.class);
				i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, false);
				c.startActivity(i);
			} catch (FutureEncodingException e) {
				Intent i = new Intent(c,DecodeErrorActivity.class);
				i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, true);
				c.startActivity(i);
			}
		}
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this,MainActivity.class));
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
		Utils.transmit(this, m, mBulbs, null, null, mBrightness);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.doneButton:
			onBackPressed();
		}

	}
}
