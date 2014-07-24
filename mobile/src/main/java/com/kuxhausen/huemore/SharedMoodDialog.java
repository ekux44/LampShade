package com.kuxhausen.huemore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.Mood;

public class SharedMoodDialog extends DialogFragment {

  private EditText mNameField;
  private Mood mMood;
  private NetworkManagedActivity mActivity;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception.
    try {
      mActivity = (NetworkManagedActivity) activity;
    } catch (ClassCastException e) {
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View bodyView = inflater.inflate(R.layout.shared_mood_dialog, null);
    mNameField = (EditText) bodyView.findViewById(R.id.moodNameEditText);

    builder.setView(bodyView);
    builder.setTitle(R.string.dialog_title_import_mood);

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.ENCODED_MOOD)) {
      String encodedMood = args.getString(InternalArguments.ENCODED_MOOD);
      try {
        mMood = HueUrlEncoder.decode(encodedMood).second.first;
      } catch (InvalidEncodingException e) {
        BrokenLinkDialogFragment bldf = new BrokenLinkDialogFragment();
        bldf.show(mActivity.getSupportFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
      } catch (FutureEncodingException e) {
        PromptUpdateDialogFragment pudf = new PromptUpdateDialogFragment();
        pudf.show(mActivity.getSupportFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

      }
    }
    if (mMood == null) {
      this.dismiss();
    }

    builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        String moodName = mNameField.getText().toString();
        // delete any old mood with same name //todo warn users
        String moodSelect = MoodColumns.COL_MOOD_NAME + "=?";
        String[] moodArg = {moodName};
        mActivity.getContentResolver().delete(MoodColumns.MOODS_URI, moodSelect,
                                              moodArg);

        ContentValues mNewValues = new ContentValues();
        mNewValues.put(MoodColumns.COL_MOOD_NAME, moodName);
        mNewValues.put(MoodColumns.COL_MOOD_LOWERCASE_NAME, moodName.toLowerCase().trim());
        mNewValues.put(MoodColumns.COL_MOOD_VALUE, HueUrlEncoder.encode(mMood));
        mNewValues.put(MoodColumns.COL_MOOD_PRIORITY, MoodRow.UNSTARRED_PRIORITY);

        mActivity.getContentResolver().insert(MoodColumns.MOODS_URI, mNewValues);
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
}
