package com.kuxhausen.huemore.ui.registration;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.HubSearch;
import com.kuxhausen.huemore.network.HubSearch.OnHubFoundListener;
import com.kuxhausen.huemore.network.Register;
import com.kuxhausen.huemore.network.Register.OnRegisterListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.Bridge;

public class DiscoverHubDialogFragment extends DialogFragment implements
		OnHubFoundListener {

	public ProgressBar progressBar;
	public HubSearch hubSearch;
	Gson gson = new Gson();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View discoverHubView = inflater.inflate(R.layout.discover_hub,
				null);
		builder.setView(discoverHubView);
		progressBar = (ProgressBar) discoverHubView
				.findViewById(R.id.progressBar1);
		hubSearch = new HubSearch(this.getActivity(), this);
		hubSearch.execute();
		
		
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		onDestroyView();
	}


	@Override
	public void onHubFoundResult(Bridge[] bridges) {
		if(bridges!=null && bridges.length>0){
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			Bundle args = new Bundle();
			//args.putString(InternalArguments.IP, gson.toJson(bridges));
			//TODO make other part gson ready
			rwhdf.setArguments(args);
			rwhdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

			dismiss();

		}else{
			//TODO
		}
	}

}