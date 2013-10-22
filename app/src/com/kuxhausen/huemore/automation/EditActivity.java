package com.kuxhausen.huemore.automation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.SerializedEditorActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;

public class EditActivity extends SerializedEditorActivity implements
		OnClickListener {

	// don't change value
	protected static final String EXTRA_BUNDLE_SERIALIZED_BY_NAME = "com.kuxhausen.huemore.EXTRA_BUNDLE_SERIALIZED_BY_NAME";

	protected static final String PERCENT_BRIGHTNESS_KEY = "com.kuxhausen.huemore.PERCENT_BRIGHTNESS";
	protected static final String PERCENT_BRIGHTNESS_VALUE = "%percentbrightness";
			
	protected static final String TASKER_VARIABLE_TARGETS_KEY = "net.dinglisch.android.tasker.extras.VARIABLE_REPLACE_KEYS";	
	protected static final String TASKER_VARIABLE_TARGETS_VALUE = PERCENT_BRIGHTNESS_KEY+"";
	
	private Button okayButton, cancelButton;

	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		// check full version unlocked
		Bundle b = this.getIntent().getExtras();
		if (b != null
				&& b.containsKey(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE)
				&& b.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE)
						.containsKey(EXTRA_BUNDLE_SERIALIZED_BY_NAME)) {
			setSerializedByName(b.getBundle(
					com.twofortyfouram.locale.Intent.EXTRA_BUNDLE).getString(
					EXTRA_BUNDLE_SERIALIZED_BY_NAME));
		}
		setContentView(R.layout.edit_automation);
		super.onCreate(savedInstanceState);

		okayButton = (Button) this.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);

		cancelButton = (Button) this.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		if(!Utils.hasProVersion(this)) {
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra(InternalArguments.PROMPT_UPGRADE, true);
			startActivity(i);
			setResult(Activity.RESULT_CANCELED);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okay:
			Intent i = new Intent();
			i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
					getSerializedByNamePreview());
			Bundle b = new Bundle();
			b.putString(EXTRA_BUNDLE_SERIALIZED_BY_NAME, getSerializedByName());
			i.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, b);
			i.putExtra(TASKER_VARIABLE_TARGETS_KEY, TASKER_VARIABLE_TARGETS_VALUE);
			i.putExtra(PERCENT_BRIGHTNESS_KEY, PERCENT_BRIGHTNESS_VALUE);
			setResult(Activity.RESULT_OK, i);
			super.finish();
			break;
		case R.id.cancel:
			setResult(Activity.RESULT_CANCELED);
			super.finish();
			break;
		}
	}

}
