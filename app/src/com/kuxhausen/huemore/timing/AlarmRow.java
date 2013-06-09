package com.kuxhausen.huemore.timing;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.ExternalArguments;

public class AlarmRow {

	private AlarmState aState;
	private Context c;
	private int id;
	Gson gson = new Gson();

	public AlarmRow(Context context, AlarmState as, int db_ID) {
		c = context;
		aState = as;
		id = db_ID;
	}

	public AlarmState getAlarmState() {
		return aState;
	}

	public int getID() {
		return id;
	}

	public String getTime() {
		/** only hour and minute are valid **/
		long time = 0;
		if(aState.isRepeating()){
			for(int i = 0; i< 7; i++)
				if(aState.getRepeatingDays()[i])
					time = aState.getRepeatingTimes()[i];
		}else{
			time = aState.getTime();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		// remember transition starts ahead to culminate at the specified time
		cal.add(Calendar.SECOND, aState.transitiontime / 10);
		return DateFormat.getTimeInstance().format(cal.getTime());

	}

	public String getSecondaryDescription() {
		String result = aState.group + " -> " + aState.mood;

		if (aState.isRepeating()) {
			result += "   "+ NewAlarmDialogFragment.repeatsToString(c, aState.getRepeatingDays());
		}
		return result;
	}

	public boolean isScheduled() {
		if (aState.scheduledForFuture == null)
			aState.scheduledForFuture = false;

		
		// if it's a non repeating alarm in the past, mark as unchecked
		if (!aState.isRepeating()) {
			Calendar scheduledTime = Calendar.getInstance();
			scheduledTime.setTimeInMillis(aState.getTime());
			if (scheduledTime.before(Calendar.getInstance())){
				aState.scheduledForFuture = false;
				return false;
			}
			// TODO save back to database, maybe move this logic out
		}
		return aState.scheduledForFuture;
	}

	public void toggle() {

		if (isScheduled()) {
			AlarmReciever.cancelAlarm(c, aState);
		} else {
			AlarmReciever.createAlarms(c, aState);
		}

		aState.scheduledForFuture = !isScheduled();
		// save change to db
		String rowSelect = BaseColumns._ID + "=?";
		String[] rowArg = { "" + id };

		ContentValues mNewValues = new ContentValues();
		mNewValues.put(AlarmColumns.STATE, gson.toJson(aState));

		c.getContentResolver().update(AlarmColumns.ALARMS_URI, mNewValues,
				rowSelect, rowArg);

	}
}
