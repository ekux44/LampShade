package com.kuxhausen.huemore.net.hue.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.HubData;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class ConfigureHubDialogFragment extends DialogFragment {

  private HubConnection mPriorConnection;
  private Context mContext;
  private EditText mLocalAddress;
  private EditText mRemoteAddress;

  public void setPriorConnection(HubConnection hubConnect) {
    mPriorConnection = hubConnect;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mContext = this.getActivity();
    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View registerWithHubView = inflater.inflate(R.layout.configure_hub, null);

    mLocalAddress = (EditText) registerWithHubView.findViewById(R.id.localEditText);
    mRemoteAddress = (EditText) registerWithHubView.findViewById(R.id.remoteEditText);

    if (mPriorConnection != null) {
      String local = mPriorConnection.getHubData().localHubAddress;
      if (local != null) {
        mLocalAddress.setText(local);
      }
      String remote = mPriorConnection.getHubData().portForwardedAddress;
      if (remote != null) {
        mRemoteAddress.setText(remote);
      }
    }

    builder.setView(registerWithHubView);
    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        HubData hd = new HubData();
        String localAddress = mLocalAddress.getText().toString();
        String remoteAddress = mRemoteAddress.getText().toString();
        
        if (!localAddress.matches("(?i:https?:).*")) {
          localAddress = "http://" + localAddress;
        }
        
        if (!remoteAddress.matches("(?i:https?:).*")) {
          remoteAddress = "http://" + remoteAddress;
        }
        
        hd.localHubAddress = localAddress;
        hd.portForwardedAddress = remoteAddress;

        if (mPriorConnection != null) {
          hd.hashedUsername = mPriorConnection.getHubData().hashedUsername;

          mPriorConnection.updateConfiguration(hd);
          Toast t =
              Toast.makeText(mContext, R.string.toast_hue_connection_updated, Toast.LENGTH_SHORT);
          t.show();

        } else {
          RegisterWithHubDialogFragment registerFrag = new RegisterWithHubDialogFragment();
          registerFrag.setHubData(hd);
          registerFrag.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        }

        dismiss();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        dismiss();
      }
    });

    return builder.create();
  }
}
