package com.kuxhausen.huemore.timing;

import android.text.format.DateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;

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
		if (aState.isRepeating()) {
			for (int i = 0; i < 7; i++)
				if (aState.getRepeatingDays()[i])
					time = aState.getRepeatingTimes()[i];
		} else {
			time = aState.getTime();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		return DateFormat.getTimeFormat(c).format(cal.getTime());
	}

	public String getSecondaryDescription() {
		String result = aState.group + " \u2192 " + aState.mood;

		if (aState.isRepeating()) {
			result += "   "
					+ NewAlarmDialogFragment.repeatsToString(c,
							aState.getRepeatingDays());
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
			if (scheduledTime.before(Calendar.getInstance())) {
				aState.scheduledForFuture = false;
				return false;
			}
			saveToDB();
		}
		return aState.scheduledForFuture;
	}

	public synchronized void toggle() {

		if (isScheduled()) {
			AlarmReciever.cancelAlarm(c, aState);
		} else {
			AlarmReciever.createAlarms(c, aState);
		}

		aState.scheduledForFuture = !isScheduled();
		
		saveToDB();
	}
	public void delete(){
		if (this.isScheduled())
			this.toggle();
		
		String moodSelect2 = BaseColumns._ID + "=?";
		String[] moodArg2 = { "" + this.getID() };
		c.getContentResolver().delete(AlarmColumns.ALARMS_URI,
				moodSelect2, moodArg2);
	}
	
	public void saveToDB(){
		String rowSelect = BaseColumns._ID + "=?";
		String[] rowArg = { "" + id };

		ContentValues mNewValues = new ContentValues();
		mNewValues.put(AlarmColumns.STATE, gson.toJson(aState));

		c.getContentResolver().update(AlarmColumns.ALARMS_URI, mNewValues,
				rowSelect, rowArg);

	}
}
