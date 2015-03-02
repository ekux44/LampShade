package com.kuxhausen.huemore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;

public class DebugDialogFragment extends DialogFragment implements View.OnClickListener {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View debugDialogView = inflater.inflate(R.layout.debug_settings_dialog, null);

    builder.setView(debugDialogView);

    debugDialogView.findViewById(R.id.debug_add_fake_bulb).setOnClickListener(this);

    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });
    return builder.create();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.debug_add_fake_bulb:
        ContentValues netBulbValues = new ContentValues();
        netBulbValues.put(NetBulbColumns.NAME_COLUMN, "Debug" + Math.random());
        netBulbValues
            .put(NetBulbColumns.DEVICE_ID_COLUMN, ((Double) Math.random()).toString());
        netBulbValues.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NetBulbType.DEBUG);
        netBulbValues.putNull(NetBulbColumns.CONNECTION_DATABASE_ID);

        getActivity().getContentResolver().insert(NetBulbColumns.URI, netBulbValues);
        break;
    }
  }
}
