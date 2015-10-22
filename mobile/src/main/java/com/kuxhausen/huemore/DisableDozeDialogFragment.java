package com.kuxhausen.huemore;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class DisableDozeDialogFragment extends DialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(R.string.doze_message);
    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        startActivity(getDozeOptOutSystemIntent());
      }
    });
    return builder.create();
  }

  /**
   * @return true if on M+ device and not currently exempted from Doze mode
   */
  public static boolean needsDozeOptOut(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      return !pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
    }
    return false;
  }

  public static void showDozeOptOutIfNeeded(AppCompatActivity activity) {
    if (needsDozeOptOut(activity)) {
      DisableDozeDialogFragment frag = new DisableDozeDialogFragment();
      frag.show(activity.getSupportFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
    }
  }

  /**
   * Generates the intent that shows Android 6.0+ users into a settings page where they can exempt
   * this app from doze mode.
   */
  @TargetApi(23)
  public static Intent getDozeOptOutSystemIntent() {
    String action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
    Uri.Builder builder = new Uri.Builder();
    builder.scheme("package");
    builder.opaquePart(BuildConfig.APPLICATION_ID);
    return new Intent(action, builder.build());
  }
}
