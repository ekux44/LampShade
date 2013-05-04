package com.kuxhausen.huemore.timing;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.network.SynchronousTransmitGroupMood;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.api.BulbState;

public class AlarmReciever extends BroadcastReceiver {

	private final static String ALARM_DETAILS = "alarmDetailsBundle";
	Gson gson = new Gson();
	
	public static AlarmState createAlarms(Context context, String group,
			String mood, int transitiontime, int brightness, Boolean[] repeats,
			int currentHour, int currentMin) {
		AlarmState as = new AlarmState();
		as.group = group;
		as.mood = mood;
		as.transitiontime = transitiontime;
		as.brightness = brightness;
		as.repeats = repeats;
		as.scheduledForFuture = true;

		Calendar projectedTime = Calendar.getInstance();
		projectedTime.setLenient(true);
		projectedTime.set(Calendar.HOUR_OF_DAY, currentHour);
		projectedTime.set(Calendar.MINUTE, currentMin);
		projectedTime.set(Calendar.SECOND, 0);
		// ensure transition starts ahead of time to culminate at the specified
		// time
		projectedTime.add(Calendar.SECOND, -transitiontime / 10);

		return createAlarms(context, as, projectedTime);

	}

	public static AlarmState createAlarms(Context context, AlarmState as,
			Calendar timeAdjustedCal) {
		boolean none = false;
		for (boolean bool : as.repeats) {
			none |= bool;
		}
		none = !none;
		if (none) {
			while (timeAdjustedCal.before(Calendar.getInstance()))
				// make sure this hour & minute is in the future
				timeAdjustedCal.set(Calendar.DATE,
						timeAdjustedCal.get(Calendar.DATE) + 1);
			as.scheduledTimes = new Long[1];
			as.scheduledTimes[0] = timeAdjustedCal.getTimeInMillis();
		} else {
			as.scheduledTimes = new Long[7];
			for (int i = 0; i < 7; i++) {
				if (as.repeats[i]) {
					Calendar copyForDayOfWeek = Calendar.getInstance();
					copyForDayOfWeek.setLenient(true);
					copyForDayOfWeek.setTimeInMillis(timeAdjustedCal
							.getTimeInMillis());
					switch (i) {
					case 0:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.SUNDAY);
						break;
					case 1:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.MONDAY);
						break;
					case 2:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.TUESDAY);
						break;
					case 3:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.WEDNESDAY);
						break;
					case 4:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.THURSDAY);
						break;
					case 5:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.FRIDAY);
						break;
					case 6:
						copyForDayOfWeek.set(Calendar.DAY_OF_WEEK,
								Calendar.SATURDAY);
						break;
					}
					if (copyForDayOfWeek.before(Calendar.getInstance()))// if in
																		// past,
																		// choose
																		// that
																		// day
																		// next
																		// week
						copyForDayOfWeek.set(Calendar.DATE,
								copyForDayOfWeek.get(Calendar.DATE) + 7);
					as.scheduledTimes[i] = copyForDayOfWeek.getTimeInMillis();
				}
			}
		}
		Calendar soonestTime = null;
		for (Long t : as.scheduledTimes) {
			if (t != null) {
				Calendar setTime = Calendar.getInstance();
				setTime.setTimeInMillis(t);
				if (as.scheduledTimes.length == 7) {// repeating weekly alarm
					Log.e("asdf", "repeatingAlarm");
					AlarmReciever.createRepeatingAlarm(context, as,
							setTime.getTimeInMillis());
				} else {
					Log.e("asdf", "oneOffAlarm");
					AlarmReciever.createAlarm(context, as,
							setTime.getTimeInMillis());
				}
				if (soonestTime == null || setTime.before(soonestTime))
					soonestTime = setTime;
			}
		}
		Toast.makeText(
				context,
				context.getString(R.string.next_scheduled_intro)
						+ " "
						+ DateUtils.getRelativeTimeSpanString(soonestTime
								.getTimeInMillis()), Toast.LENGTH_SHORT).show();
		return as;
	}

	public static void createAlarm(Context context, AlarmState alarmState,
			Long timeInMillis) {
		Gson gson = new Gson();
		String aState = gson.toJson(alarmState);

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReciever.class);
		intent.putExtra(ALARM_DETAILS, aState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				generateRequestCode(aState), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
	}

	public static void createRepeatingAlarm(Context context,
			AlarmState alarmState, Long timeInMillis) {

		Gson gson = new Gson();
		String aState = gson.toJson(alarmState);

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReciever.class);
		intent.putExtra(ALARM_DETAILS, aState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				generateRequestCode(aState), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
				AlarmManager.INTERVAL_DAY * 7, pendingIntent);
	}

	public static void cancelAlarm(Context context, AlarmState alarmState) {
		Gson gson = new Gson();
		String aState = gson.toJson(alarmState);

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReciever.class);
		intent.putExtra(ALARM_DETAILS, aState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				generateRequestCode(aState), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.cancel(pendingIntent);
	}

	public static int generateRequestCode(String aState) {
		Gson gson = new Gson();
		AlarmState as = gson.fromJson(aState, AlarmState.class);
		int code = 0;
		int bit = 1;
		for (boolean b : as.repeats) {
			if (b)
				code += bit;
			bit *= 2;
		}
		return code;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmState as = gson.fromJson(
				intent.getExtras().getString(ALARM_DETAILS), AlarmState.class);

		// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { as.group };
		Cursor groupCursor = context.getContentResolver().query(
				DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use the
																	// default
																	// content
																	// URI
																	// for the
																	// provider.
				groupColumns, // Return the note ID and title for each note.
				GroupColumns.GROUP + "=?", // selection clause
				gWhereClause, // selection clause args
				null // Use the default sort order.
				);

		ArrayList<Integer> groupStates = new ArrayList<Integer>();
		while (groupCursor.moveToNext()) {
			groupStates.add(groupCursor.getInt(0));
		}
		Integer[] bulbS = groupStates.toArray(new Integer[groupStates.size()]);

		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { as.mood };
		Cursor moodCursor = context.getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use the
																// default
																// content URI
																// for the
																// provider.
				moodColumns, // Return the note ID and title for each note.
				MoodColumns.MOOD + "=?", // selection clause
				mWereClause, // election clause args
				null // Use the default sort order.
				);

		ArrayList<String> moodStates = new ArrayList<String>();
		while (moodCursor.moveToNext()) {
			moodStates.add(moodCursor.getString(0));
		}
		String[] moodS = moodStates.toArray(new String[moodStates.size()]);

		int brightness = as.brightness;
		int transitiontime = as.transitiontime;
		for (int i = 0; i < moodS.length; i++) {
			BulbState bs = gson.fromJson(moodS[i], BulbState.class);
			bs.bri = brightness;
			bs.transitiontime = transitiontime;
			moodS[i] = gson.toJson(bs);// put back into json string for Transmit
										// Group Mood
		}

		SynchronousTransmitGroupMood trasmitter = new SynchronousTransmitGroupMood();
		trasmitter.execute(context, bulbS, moodS);
	}
	
	
}
