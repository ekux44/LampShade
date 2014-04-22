package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class SettingsActivity extends SherlockActivity implements OnClickListener {

	SharedPreferences mSettings;
	private CheckBox mEnableNfcReadPage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		this.getSupportActionBar().setTitle(R.string.action_settings);

		Button rateButton = (Button) this.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(this);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        
		mEnableNfcReadPage = (CheckBox)this.findViewById(R.id.showNfcReadPageCheckBox);
		if(mSettings.getBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, true)){
			mEnableNfcReadPage.setChecked(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rateButton:
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
					.parse("market://details?id=" + "com.kuxhausen.huemore")));
			break;
		}
	}

	@Override
	public void onStop(){
		super.onStop();
		
		Editor edit = mSettings.edit();
		edit.putBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, mEnableNfcReadPage.isChecked());
		edit.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this,MainActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
