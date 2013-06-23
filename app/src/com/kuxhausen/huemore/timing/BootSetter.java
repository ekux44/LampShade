package com.kuxhausen.huemore.timing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;

public class BootSetter extends BroadcastReceiver {

	Gson gson = new Gson();

	@Override
	public void onReceive(Context context, Intent intent) {
		String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
		Cursor cursor = context.getContentResolver().query(
				AlarmColumns.ALARMS_URI, columns, null, null, null);

		cursor.moveToPosition(-1);// not the same as move to first!
		while (cursor.moveToNext()) {
			AlarmState as = gson
					.fromJson(cursor.getString(0), AlarmState.class);

			AlarmReciever.createAlarms(context, as);
		}

	}

}