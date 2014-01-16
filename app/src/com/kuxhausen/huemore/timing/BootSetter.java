package com.kuxhausen.huemore.timing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class BootSetter extends BroadcastReceiver {

	Gson gson = new Gson();

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if (settings.contains(PreferenceKeys.FIRST_RUN)) {
			String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
			Cursor cursor = context.getContentResolver().query(
					AlarmColumns.ALARMS_URI, columns, null, null, null);
	
			cursor.moveToPosition(-1);// not the same as move to first!
			while (cursor.moveToNext()) {
				AlarmRow ar = new AlarmRow(context, gson.fromJson(cursor.getString(0), AlarmState.class), cursor.getInt(1));
				if(ar.getAlarmState().scheduledForFuture){
					AlarmReciever.updateAlarmTimes(context, ar.getAlarmState());
					AlarmReciever.createAlarms(context, ar.getAlarmState());
					ar.saveToDB();
				}
				
			}
		}
	}

}