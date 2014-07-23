package com.kuxhausen.huemore.onboarding;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.kuxhausen.huemore.net.NewConnectionFragment;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

public class WelcomeDialogFragment extends DialogFragment {


  public void onCreate(Bundle b) {
    super.onCreate(b);

    // Remember that this page has been shown so as not to show it again
    SharedPreferences settings =
        PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    Editor edit = settings.edit();
    edit.putBoolean(PreferenceKeys.DONE_WITH_WELCOME_DIALOG, true);
    edit.commit();

    // Show the hub discovery dialog
    NewConnectionFragment dhdf = new NewConnectionFragment();
    dhdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

    this.dismiss();

  }

}
