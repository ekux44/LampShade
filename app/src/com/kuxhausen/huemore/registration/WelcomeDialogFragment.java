package com.kuxhausen.huemore.registration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class WelcomeDialogFragment extends DialogFragment implements
OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.welcome_dialog_fragment, container, false);
		
		Button getStartedButton = (Button) myView.findViewById(R.id.getStartedButton);
		getStartedButton.setOnClickListener(this);
		
		Button getHueButton = (Button) myView.findViewById(R.id.getHueButton);
		getHueButton.setOnClickListener(this);
		
		this.getDialog().setTitle(R.string.action_welcome);
		
		return myView;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.getStartedButton:
			
			// Show the hub discovery dialog
			DiscoverHubDialogFragment dhdf = new DiscoverHubDialogFragment();
			dhdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);

			// Remember that this page has been shown so as not to show it again
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this.getActivity());
			Editor edit = settings.edit();
			edit.putBoolean(PreferenceKeys.DONE_WITH_WELCOME_DIALOG, true);
			edit.commit();
			
			
			this.dismiss();
			break;
		case R.id.getHueButton:
			Intent i = new Intent(Intent.ACTION_VIEW);
			String url = "http://www.amazon.com/gp/product/B00BSN8DN4/ref=as_li_qf_sp_asin_il_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B00BSN8DN4&linkCode=as2&tag=lampshio-20";
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		}
		
	}

}
