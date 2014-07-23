package com.kuxhausen.huemore.timing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.R;

public class RepeatDialogFragment extends DialogFragment implements
                                                         DialogInterface.OnMultiChoiceClickListener {

  private boolean[] checkedItems = new boolean[7];
  OnRepeatSelectedListener resultListener;

  public interface OnRepeatSelectedListener {

    public void onRepeatSelected(boolean[] repeats);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setMultiChoiceItems(R.array.repeat_days, checkedItems, this);

    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        resultListener.onRepeatSelected(checkedItems);

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
  public void onClick(DialogInterface dialog, int which, boolean isChecked) {
    checkedItems[which] = isChecked;
  }

}
