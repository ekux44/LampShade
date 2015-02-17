package com.kuxhausen.huemore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class DecodeErrorActivity extends ActionBarActivity implements OnClickListener {

  TextView messageText;
  Button continueButton;
  boolean decoderErrorUpgrade;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Helpers.applyLocalizationPreference(this);

    setContentView(R.layout.decoder_error_activity);
    this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    messageText = (TextView) findViewById(R.id.messageTextView);
    continueButton = (Button) findViewById(R.id.continueButton);
    continueButton.setOnClickListener(this);

    decoderErrorUpgrade =
        getIntent().getExtras().getBoolean(InternalArguments.DECODER_ERROR_UPGRADE);
    if (decoderErrorUpgrade) {
      messageText.setText(R.string.update_required);
    } else {
      messageText.setText(R.string.corrupted_mood);
    }
  }

  @Override
  public void onClick(View v) {
    if (decoderErrorUpgrade) {
      this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                                                                  + "com.kuxhausen.huemore")));
    }
    this.finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        this.startActivity(new Intent(this, MainFragment.class));
        return true;
    }
    return false;
  }
}
