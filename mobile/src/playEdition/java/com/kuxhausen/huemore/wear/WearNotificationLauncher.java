package com.kuxhausen.huemore.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WearNotificationLauncher extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ConnectivityManager
        conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = conMan.getActiveNetworkInfo();
    if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      //TODO show wear notification
    } else {
      //TODO remove wear notification
    }
  }
}
