package com.kuxhausen.huemore.registration;

import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class ConnectionStatusDialogFragment extends DialogFragment{
	
	GodObject parrentActivity;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		//View statusView = inflater.inflate(R.layout.edit_group_dialog,
		//		null);
		builder.setTitle(R.string.action_hub_status);
		
		parrentActivity = (GodObject)this.getActivity();
		
		if(parrentActivity.hasHubConnection()){
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(parrentActivity);
			String bridge = settings.getString(PreferencesKeys.BRIDGE_IP_ADDRESS,
					"");
			
			
			builder.setMessage(this.getString(R.string.hub_connection)+": "+bridge);
			
			builder.setPositiveButton(R.string.accept,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			builder.setNegativeButton(R.string.reset_hub_connection,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							DiscoverHubDialogFragment dhdf1 = new DiscoverHubDialogFragment();
							dhdf1.show(parrentActivity.getSupportFragmentManager(),
									InternalArguments.FRAG_MANAGER_DIALOG_TAG);
						}
					});
		}else{
			builder.setMessage(this.getString(R.string.hub_connection_error));
			
			builder.setPositiveButton(R.string.reset_hub_connection,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							DiscoverHubDialogFragment dhdf1 = new DiscoverHubDialogFragment();
							dhdf1.show(parrentActivity.getSupportFragmentManager(),
									InternalArguments.FRAG_MANAGER_DIALOG_TAG);
						}
					});
			
			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
						}
					});
		}
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
