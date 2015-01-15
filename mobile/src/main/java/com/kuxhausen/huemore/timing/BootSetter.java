package com.kuxhausen.huemore.timing;

import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.DeprecatedAlarmState;

public class BootSetter extends BroadcastReceiver {

  Gson gson = new Gson();

  @Override
  public void onReceive(Context context, Intent intent) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    if (settings.contains(PreferenceKeys.FIRST_RUN)) {
      String[] columns = {Definitions.DeprecatedAlarmColumns.STATE, BaseColumns._ID};
      Cursor cursor =
          context.getContentResolver().query(Definitions.DeprecatedAlarmColumns.ALARMS_URI, columns, null, null, null);

      cursor.moveToPosition(-1);// not the same as move to first!
      while (cursor.moveToNext()) {
        DatabaseAlarm ar =
            new DatabaseAlarm(context, gson.fromJson(cursor.getString(0), DeprecatedAlarmState.class),
                              cursor.getInt(1));
        if (ar.getAlarmState().isScheduled()) {
          AlarmReciever.createAlarms(context, ar);
        }

      }

      // clear out any playing moods stopped at shutdown
      context.getContentResolver().delete(Definitions.PlayingMood.URI, null, null);
    }
  }

}
