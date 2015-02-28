package com.kuxhausen.huemore.persistence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.DatabaseGroup;

public class LocalizationReceiver extends BroadcastReceiver {

  /**
   * Renames the ALL mood when locale language changes
   */
  @Override
  public void onReceive(final Context context, Intent intent) {
    DatabaseGroup group = DatabaseGroup.loadAllGroup(context);
    group.setName(context.getResources().getString(R.string.cap_all), context);
  }
}
