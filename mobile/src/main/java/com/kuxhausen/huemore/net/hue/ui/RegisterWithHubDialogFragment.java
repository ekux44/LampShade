package com.kuxhausen.huemore.net.hue.ui;

import com.google.gson.Gson;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.hue.HubData;
import com.kuxhausen.huemore.net.hue.api.Bridge;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.net.hue.api.RegistrationResponse;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class RegisterWithHubDialogFragment extends DialogFragment {

  public final long length_in_milliseconds = 30000;
  public final long period_in_milliseconds = 1000;
  public ProgressBar progressBar;
  public CountDownTimer countDownTimer;
  Bridge[] bridges;
  Gson gson = new Gson();
  RequestQueue rq;
  HubData mHubData;

  public void setHubData(HubData hd) {
    mHubData = hd;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    rq = Volley.newRequestQueue(this.getActivity());

    if (this.getArguments() != null) {
      bridges =
          gson.fromJson(this.getArguments().getString(InternalArguments.BRIDGES), Bridge[].class);
    } else if (mHubData != null) {
      Bridge[] fakes = new Bridge[2];
      fakes[0] = new Bridge();
      fakes[0].internalipaddress = mHubData.localHubAddress;
      fakes[1] = new Bridge();
      fakes[1].internalipaddress = mHubData.portForwardedAddress;

      bridges = fakes;

    } else {
      this.dismiss();
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View registerWithHubView = inflater.inflate(R.layout.register_with_hub, null);
    builder.setView(registerWithHubView);
    progressBar = (ProgressBar) registerWithHubView.findViewById(R.id.timerProgressBar);

    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });

    countDownTimer = new CountDownTimer(length_in_milliseconds, period_in_milliseconds) {
      private boolean warned = false;

      @TargetApi(Build.VERSION_CODES.HONEYCOMB)
      @Override
      public void onTick(long millisUntilFinished) {
        if (isAdded()) {
          progressBar
              .setProgress((int) (((length_in_milliseconds - millisUntilFinished) * 100.0)
                                  / length_in_milliseconds));
          NetworkMethods.register(rq, getListeners(), bridges, getDeviceType());
        }
      }

      @Override
      public void onFinish() {
        if (isAdded()) {
          // try one last time
          NetworkMethods.register(rq, getListeners(), bridges, getDeviceType());

          // launch the failed registration dialog
          RegistrationFailDialogFragment rfdf = new RegistrationFailDialogFragment();
          rfdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

          dismiss();
        }
      }
    };
    countDownTimer.start();
    // Create the AlertDialog object and return it
    return builder.create();
  }

  protected Listener<RegistrationResponse[]>[] getListeners() {
    Listener<RegistrationResponse[]>[] listeners = new Listener[bridges.length];
    for (int i = 0; i < bridges.length; i++) {
      if (bridges[i] != null && bridges[i].internalipaddress != null) {
        listeners[i] = new RegistrationListener(bridges[i].internalipaddress);
      }
    }
    return listeners;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    countDownTimer.cancel();
    onDestroyView();
  }

  public String getDeviceType() {
    return getString(R.string.app_name);
  }

  class RegistrationListener implements Listener<RegistrationResponse[]> {

    public String bridgeIP;

    public RegistrationListener(String ip) {
      bridgeIP = ip;
    }

    @Override
    public void onResponse(RegistrationResponse[] response) {
      if (response[0].success != null) {
        countDownTimer.cancel();

        if (getFragmentManager() != null) {
          // Show the success dialog
          RegistrationSuccessDialogFragment rsdf = new RegistrationSuccessDialogFragment();
          rsdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

          // Add connection to the database
          if (mHubData == null) {
            mHubData = new HubData();
          }
          if (mHubData.portForwardedAddress != null
              && bridgeIP.equals(mHubData.portForwardedAddress)) {
            // don't need to adjust addresses
          } else {
            mHubData.localHubAddress = bridgeIP;
          }
          mHubData.hashedUsername = response[0].success.getUsername();
          ContentValues cv = new ContentValues();

          cv.put(Definitions.NetConnectionColumns.TYPE_COLUMN,
                 Definitions.NetBulbColumns.NetBulbType.PHILIPS_HUE);
          cv.put(Definitions.NetConnectionColumns.JSON_COLUMN, gson.toJson(mHubData));
          cv.put(Definitions.NetConnectionColumns.NAME_COLUMN, "?");

          RegisterWithHubDialogFragment.this.getActivity().getContentResolver()
              .insert(Definitions.NetConnectionColumns.URI, cv);

          // done with registration dialog
          dismiss();
        }
      }
    }
  }
}
