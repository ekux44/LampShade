package com.kuxhausen.huemore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class BrokenLinkDialogFragment extends DialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View updateDialogView = inflater.inflate(R.layout.broken_mood_link_dialog, null);

    builder.setView(updateDialogView);

    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {

      }
    });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
