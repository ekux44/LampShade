package com.kuxhausen.huemore.nfc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.state.BulbState;

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
import android.os.Parcelable;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.nfc.Tag;
import android.nfc.FormatException;

public class NfcReaderActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc_reader);
		

	}
	public void onResume() {
	    super.onResume();
	    
	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
	        Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        if (rawMsgs != null) {
	            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
	            for (int i = 0; i < rawMsgs.length; i++) {
	                msgs[i] = (NdefMessage) rawMsgs[i];
	            }
	            //msgs[0].
	            
	            byte[] payload = msgs[0].getRecords()[0].getPayload();
	            byte identifierCode = payload[0];

	           // String prefix = (0x01)+""; // you need to implement this one
	           // String url = prefix +
	           //     new String(payload, 1, payload.length -1, Charset.forName("US-ASCII"));

	           String data = new String(payload, 1, payload.length -1, Charset.forName("US-ASCII")) ;
	           //System.out.println(data);
	           data = data.substring(data.indexOf('?')+1);
	           //System.out.println(data);
	           Pair<Integer[], BulbState[]> result = HueNfcEncoder.decode(data);
	           Gson gson = new Gson();
	           String[] bulbS = new String[result.second.length];
	           
	           for(int i = 0; i<result.second.length; i++){
	        	   bulbS[i]=gson.toJson(result.second[i]);
	        	   System.out.println(bulbS[i]);
	           }
	           TransmitGroupMood transmitter = new TransmitGroupMood(this,result.first, bulbS);
	           transmitter.execute();
	        }
	    }
	}
}
