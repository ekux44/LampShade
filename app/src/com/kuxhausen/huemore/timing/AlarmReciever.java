package com.kuxhausen.huemore.timing;

import java.util.ArrayList;
import java.util.Calendar;

import com.google.gson.Gson;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.BulbState;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmReciever extends BroadcastReceiver {

	private final static String ALARM_DETAILS = "alarmDetailsBundle";

	public static boolean createAlarm(Context context, String alarmState,
			Calendar cal, int requestCode) {

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReciever.class);
		intent.putExtra(ALARM_DETAILS, alarmState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				pendingIntent);
		return false;
	}

	public static boolean cancelAlarm(Context context, String alarmState, Calendar cal, int requestCode) {

		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReciever.class);
		intent.putExtra(ALARM_DETAILS, alarmState);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.cancel(pendingIntent);
		return false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Gson gson = new Gson();
		AlarmState as = gson.fromJson(intent.getExtras().getString(ALARM_DETAILS),AlarmState.class);
		
		// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = {as.group};
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
		String[] mWereClause = { as.mood};
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
			bs.transitiontime=transitiontime;
			moodS[i] = gson.toJson(bs);// put back into json string for Transmit
										// Group Mood
		}

		TransmitGroupMood tgm = new TransmitGroupMood(context, bulbS, moodS);
		tgm.execute();

		Toast.makeText(context, "HueMore Alarm "+as.group+" "+as.mood+" went off", Toast.LENGTH_SHORT)
				.show();
	}

}
