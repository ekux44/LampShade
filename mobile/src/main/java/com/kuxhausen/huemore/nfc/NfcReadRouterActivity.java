package com.kuxhausen.huemore.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.Helpers;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

public class NfcReadRouterActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Helpers.applyLocalizationPreference(this);

    Context con = getApplicationContext();
    String encodedMood = NfcReaderActivity.getGroupMoodBrightnessFromNdef(this.getIntent());
    Intent srv = new Intent(con, ConnectivityService.class);
    srv.putExtra(InternalArguments.ENCODED_MOOD, encodedMood);
    con.startService(srv);

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(con);
    if (settings.getBoolean(getString(R.string.preference_show_nfc_controls), true)) {
      Intent i = new Intent(this, NfcReaderActivity.class);
      i.putExtra(InternalArguments.ENCODED_MOOD, encodedMood);
      startActivity(i);
    }
    finish();
  }
}
