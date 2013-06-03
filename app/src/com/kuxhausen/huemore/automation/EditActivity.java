package com.kuxhausen.huemore.automation;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.ui.SerializedEditorActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EditActivity extends SerializedEditorActivity implements OnClickListener {

	private static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
	private static final String EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";
	private static final String NFC_ENCODED = "NFC_ENCODED";
	
	private Button okayButton, cancelButton;
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.edit_automation);
		super.onCreate(savedInstanceState);
		
		okayButton = (Button) this.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		
		cancelButton = (Button) this.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
	
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okay:
			Intent i = new Intent();
			i.putExtra(EXTRA_STRING_BLURB, getMessage());
			Bundle b = new Bundle();
			b.putString(NFC_ENCODED, getMessage());
			i.putExtra(EXTRA_BUNDLE, b);
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
