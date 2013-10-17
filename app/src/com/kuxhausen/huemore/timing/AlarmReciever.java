package com.kuxhausen.huemore.timing;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

public class AlarmReciever extends WakefulBroadcastReceiver {

	Gson gson = new Gson();

	/**
	 * when this method is called, AlarmState as must have the correct hour and
	 * minute for each time the alarm is to be scheduled for
	 **/
	public static AlarmState createAlarms(Context context, AlarmState as) {
		Calendar soonestTime = null;

		if (!as.isRepeating()) {
			Calendar timeAdjustedCal = Calendar.getInstance();
			timeAdjustedCal.setTimeInMillis(as.getTime());
			timeAdjustedCal.setLenient(true);

			while (timeAdjustedCal.before(Calendar.getInstance())) {
				// make sure this hour & minute is in the future
				timeAdjustedCal.add(Calendar.DATE, 1);
			}
			as.setTime(timeAdjustedCal.getTimeInMillis());

			AlarmReciever.scheduleAlarm(context, as,
					timeAdjustedCal.getTimeInMillis());
			soonestTime = timeAdjustedCal;
		} else {
			Calendar rightNow = Calendar.getInstance();
			long[] scheduledTimes = new long[7];
			for (int i = 0; i < 7; i++) {
				if (as.getRepeatingDays()[i]) {
					Calendar existingTimeCal = Calendar.getInstance();
					existingTimeCal.setTimeInMillis(as.getRepeatingTimes()[i]);

					Calendar copyForDayOfWeek = Calendar.getInstance();
					copyForDayOfWeek.setLenient(true);
					copyForDayOfWeek.set(Calendar.HOUR_OF_DAY,
							existingTimeCal.get(Calendar.HOUR_OF_DAY));
					copyForDayOfWeek.set(Calendar.MINUTE,
							existingTimeCal.get(Calendar.MINUTE));
					copyForDayOfWeek.set(Calendar.SECOND,
							existingTimeCal.get(Calendar.SECOND));

					/**
					 * 7+ desired day of week (+1 to convert to java calendar
					 * number) - current day of week %7
					 **/
					copyForDayOfWeek
							.add(Calendar.DATE, (7 + (1 + i) - rightNow
									.get(Calendar.DAY_OF_WEEK)) % 7);

					while (copyForDayOfWeek.before(Calendar.getInstance())) {
						// if in past, choose that day next week
						copyForDayOfWeek.add(Calendar.DATE, 7);
					}
					scheduledTimes[i] = copyForDayOfWeek.getTimeInMillis();
				}
			}
			as.setRepeatingTimes(scheduledTimes);

			for (int i = 0; i < 7; i++) {
				long t = as.getRepeatingTimes()[i];
				if (as.getRepeatingDays()[i]) {
					AlarmReciever.scheduleWeeklyAlarm(context, as, t, i + 1);

					Calendar setTime = Calendar.getInstance();
					setTime.setTimeInMillis(t);
					if (soonestTime == null || setTime.before(soonestTime))
						soonestTime = setTime;
				}
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

	private static void scheduleAlarm(Context context, AlarmState alarmState,
			Long timeInMillis) {

		PendingIntent pIntent = calculatePendingIntent(context, alarmState, 0);
		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMillis, pIntent);
	}

	private static void scheduleWeeklyAlarm(Context context,
			AlarmState alarmState, Long timeInMillis, int dayOfWeek) {

		PendingIntent pIntent = calculatePendingIntent(context, alarmState,
				dayOfWeek);
		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
				AlarmManager.INTERVAL_DAY * 7, pIntent);
	}

	public static void cancelAlarm(Context context, AlarmState alarmState) {
		for (int i = 0; i < 8; i++) {
			PendingIntent pIntent = calculatePendingIntent(context, alarmState,
					i);
			AlarmManager alarmMgr = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmMgr.cancel(pIntent);
		}
	}

	/** day of week Sunday = 1, Saturday = 7, 0=not repeating so we don't care **/
	private static PendingIntent calculatePendingIntent(Context context,
			AlarmState alarmState, int dayOfWeek) {
		Gson gson = new Gson();
		String aState = gson.toJson(alarmState);

		Intent intent = new Intent(context, AlarmReciever.class);
		intent.setAction("com.kuxhausen.huemore." +dayOfWeek+"."+ aState);
		intent.putExtra(InternalArguments.ALARM_DETAILS, aState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && intent.getAction().matches("com\\.kuxhausen\\.huemore\\.\\d\\..*")) {
			AlarmState as = gson.fromJson(
					intent.getExtras().getString(
							InternalArguments.ALARM_DETAILS), AlarmState.class);

			// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = { as.group };
			Cursor groupCursor = context.getContentResolver()
					.query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
							groupColumns, GroupColumns.GROUP + "=?",
							gWhereClause, null);

			ArrayList<Integer> groupStates = new ArrayList<Integer>();
			while (groupCursor.moveToNext()) {
				groupStates.add(groupCursor.getInt(0));
			}
			Integer[] bulbS = groupStates.toArray(new Integer[groupStates
					.size()]);

			Mood m = Utils.getMoodFromDatabase(as.mood, context);	

			Intent trasmitter = new Intent(context, MoodExecuterService.class);
			trasmitter.putExtra(InternalArguments.ENCODED_MOOD, HueUrlEncoder.encode(m,bulbS, as.brightness));
			trasmitter.putExtra(InternalArguments.MOOD_NAME, as.mood);
			startWakefulService(context, trasmitter);

		}
	}
}
