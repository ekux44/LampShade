package com.kuxhausen.huemore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

public class PromptUpdateDialogFragment extends DialogFragment implements OnCheckedChangeListener {

  CheckBox optOut;

  private NetworkManagedActivity parrentActivity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception.
    try {
      parrentActivity = (NetworkManagedActivity) activity;
    } catch (ClassCastException e) {
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View updateDialogView = inflater.inflate(R.layout.update_reminder_dialog, null);

    optOut = (CheckBox) updateDialogView.findViewById(R.id.updateOptOutCheckBox);
    optOut.setOnCheckedChangeListener(this);

    builder.setView(updateDialogView);

    builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        parrentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
            .parse("market://details?id=" + "com.kuxhausen.huemore")));
      }
    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });
    // Create the AlertDialog object and return it
    return builder.create();
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
    Editor edit = settings.edit();
    if (isChecked) {
      edit.putBoolean(PreferenceKeys.UPDATE_OPT_OUT, true);
    } else {
      edit.putBoolean(PreferenceKeys.UPDATE_OPT_OUT, false);
    }
    edit.commit();
  }
}
