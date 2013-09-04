package com.kuxhausen.huemore.registration;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.HubSearch;
import com.kuxhausen.huemore.network.HubSearch.OnHubFoundListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
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
		View discoverHubView = inflater.inflate(R.layout.discover_hub, null);
		builder.setView(discoverHubView);
		progressBar = (ProgressBar) discoverHubView
				.findViewById(R.id.progressBar1);

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		
		
		
		startDiscovery();
		Log.e("asdf", "hubSearchStarted");

		// Create the AlertDialog object and return it
		return builder.create();
	}

	@TargetApi(11)
	public void startDiscovery() {
		hubSearch = new HubSearch(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			hubSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			hubSearch.execute();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		onDestroyView();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (hubSearch != null)
			hubSearch.cancel(false);
	}

	@Override
	public void onHubFoundResult(Bridge[] bridges) {
		Log.e("asdf", "onHubFoundResult");
		if (bridges != null && bridges.length > 0) {
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			Bundle args = new Bundle();
			args.putString(InternalArguments.BRIDGES, gson.toJson(bridges));
			Log.e("asdf", gson.toJson(bridges));
			rwhdf.setArguments(args);
			rwhdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		} else {
			RegistrationFailDialogFragment rfdf = new RegistrationFailDialogFragment();
			rfdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		}
		dismiss();
	}

}