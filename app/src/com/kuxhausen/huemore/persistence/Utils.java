package com.kuxhausen.huemore.persistence;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

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
	
	public static Mood generateSimpleMood(BulbState bs){
		//boilerplate
		Event e = new Event();
		e.channel=0;
		e.time=0;
		e.state=bs;
		Event[] eRay = {e};
		//more boilerplate
		Mood m = new Mood();
		m.numChannels=1;
		m.usesTiming = false;
		m.events = eRay;
		
		return m;
	}
}
