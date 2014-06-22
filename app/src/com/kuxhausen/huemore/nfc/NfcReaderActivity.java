package com.kuxhausen.huemore.nfc;

import java.nio.charset.Charset;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
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

import com.kuxhausen.huemore.MainFragment;
import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

public class NfcReaderActivity extends NetworkManagedActivity implements OnCheckedChangeListener,
    OnClickListener {

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

    Bundle b = this.getIntent().getExtras();
    if (b != null && b.containsKey(InternalArguments.ENCODED_MOOD)) {
      Pair<Integer[], Pair<Mood, Integer>> result;
      try {
        String encodedMood = b.getString(InternalArguments.ENCODED_MOOD);
        result = HueUrlEncoder.decode(encodedMood);
        mBulbs = result.first;
        mood = result.second.first;
        mBrightness = result.second.second;

        mOnButton.setOnCheckedChangeListener(null);

        Intent intent = new Intent(this, ConnectivityService.class);
        intent.putExtra(InternalArguments.ENCODED_MOOD, encodedMood);
        this.startService(intent);

        boolean on = false;
        if (mood.events[0].state.on != null && mood.events[0].state.on)
          on = true;
        mOnButton.setChecked(on);
        mOnButton.setOnCheckedChangeListener(this);

      } catch (InvalidEncodingException e) {
        this.finish();
      } catch (FutureEncodingException e) {
        this.finish();
      }
    }
  }

  public static String getGroupMoodBrightnessFromNdef(Intent input) {
    Parcelable[] rawMsgs = input.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    if (rawMsgs != null) {
      NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
      for (int i = 0; i < rawMsgs.length; i++) {
        msgs[i] = (NdefMessage) rawMsgs[i];
      }

      byte[] payload = msgs[0].getRecords()[0].getPayload();

      String data = new String(payload, 1, payload.length - 1, Charset.forName("US-ASCII"));
      data = data.substring(data.indexOf('?') + 1);

      return data;
    }
    return "";
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.startActivity(new Intent(this, MainFragment.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    BulbState bs = new BulbState();
    bs.on = isChecked;

    ConnectivityService service = this.getService();
    if (service != null) {
      DeviceManager dm = service.getDeviceManager();
      Group g = Group.loadFromLegacyData(mBulbs, null, this);
      for (Long bulbId : g.getNetworkBulbDatabaseIds()) {
        dm.getNetworkBulb(bulbId).setState(bs);
      }
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.doneButton:
        onBackPressed();
    }

  }
}
