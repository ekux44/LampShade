package com.kuxhausen.huemore.persistence;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.Mood;

public class Utils {

	public static Mood getMoodFromDatabase(String moodName, Context ctx){
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWhereClause = { moodName };
		Cursor moodCursor = ctx.getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODSTATES_URI, 
				moodColumns,
				MoodColumns.MOOD + "=?",
				mWhereClause,
				null
				);
		moodCursor.moveToFirst();
		return HueUrlEncoder.decode(moodCursor.getString(0)).second;
	}
}
