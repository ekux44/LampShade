package com.kuxhausen.huemore.timing;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;

public class BootSetter extends BroadcastReceiver {

	Context context;
	Gson gson = new Gson();

	@Override
	public void onReceive(Context c, Intent intent) {
		// Do your stuff
		context = c;
		/*
		 * Initializes the CursorLoader. The GROUPS_LOADER value is eventually
		 * passed to onCreateLoader().
		 */
		// getLoaderManager().initLoader(ALARMS_LOADER, null, this);
		String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
		Cursor cursor = c.getContentResolver().query(AlarmColumns.ALARMS_URI, // Use
																				// the
																				// default
																				// content
																				// URI
																				// for
																				// the
																				// provider.
				columns, // Return the note ID and title for each note.
				null, // No where clause, return all records.
				null, // No where clause, therefore no where column values.
				null // Use the default sort order.
				);

		cursor.moveToPosition(-1);//not the same as move to first!
		while (cursor.moveToNext()) {
			AlarmState as = gson
					.fromJson(cursor.getString(0), AlarmState.class);

			Long time = null;
			findFirst: for (Long t : as.scheduledTimes) {
				if (t != null) {
					time = t;
					break findFirst;
				}
			}
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(time);
			AlarmReciever.createAlarms(context, as, cal);
		}

	}

}