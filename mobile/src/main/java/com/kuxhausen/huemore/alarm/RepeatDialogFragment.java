package com.kuxhausen.huemore.alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class RepeatDialogFragment extends DialogFragment implements
                                                         DialogInterface.OnMultiChoiceClickListener {

  private boolean[] mCheckedItems = new boolean[7];
  private OnRepeatSelectedListener mResultListener;

  public interface OnRepeatSelectedListener {

    public void onRepeatSelected(DaysOfWeek repeats);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.DAYS_OF_WEEK_AS_BYTE)) {
      DaysOfWeek prior = new DaysOfWeek(args.getByte(InternalArguments.DAYS_OF_WEEK_AS_BYTE));
      for (int i = 0; i < 7; i++) {
        mCheckedItems[i] = prior.isDaySet(i + 1);
      }
    }

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setMultiChoiceItems(R.array.repeat_days, mCheckedItems, this);

    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        DaysOfWeek days = new DaysOfWeek();
        for (int i = 0; i < 7; i++) {
          days.setDay(i + 1, mCheckedItems[i]);
        }
        mResultListener.onRepeatSelected(days);
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
    mCheckedItems[which] = isChecked;
  }

  public void setResultListener(OnRepeatSelectedListener listener) {
    mResultListener = listener;
  }

}
