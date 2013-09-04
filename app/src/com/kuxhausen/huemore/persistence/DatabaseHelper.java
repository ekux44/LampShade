package com.kuxhausen.huemore.persistence;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 2;
	Gson gson = new Gson();

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
				+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");

		db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
				+ GroupColumns.GROUP + " TEXT," + GroupColumns.PRECEDENCE
				+ " INTEGER," + GroupColumns.BULB + " INTEGER" + ");");
		
		this.onUpgrade(db, 1, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion){
		case 1:
			ContentValues cv = new ContentValues();
			
			/** update 2.4/2.5/switch to serialized b64 **/
			
			String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
			Cursor cursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
			
			HashMap<String,ArrayList<String>> moodStateMap = new HashMap<String,ArrayList<String>>();
			
			while (cursor.moveToNext()) {
				String mood = cursor.getString(0);
				String state = cursor.getString(1);
				if(mood!=null && state!=null && !mood.equals("") && !state.equals("") && !state.equals("{}")){
					ArrayList<String> states;
					if(moodStateMap.containsKey(mood))
						states = moodStateMap.get(mood);
					else
						states =  new ArrayList<String>();
					states.add(state);
					moodStateMap.put(mood, states);
				}
			}
			db.execSQL("DROP TABLE " + MoodColumns.TABLE_NAME);
			
			db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
					+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");

			db.execSQL("CREATE TABLE IF NOT EXISTS " + AlarmColumns.TABLE_NAME + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
					+ AlarmColumns.STATE + " TEXT,"
					+ AlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");
			
			String[] gSelectionArgs = { "ALL", ((char) 8) + "ALL" };
			db.delete(GroupColumns.TABLE_NAME,
					DatabaseDefinitions.GroupColumns.GROUP + "=? or "
							+ DatabaseDefinitions.GroupColumns.GROUP + "=?",
					gSelectionArgs);

			
			//remove standard moods that are no longer correct
			String[] moodsToRemove = {"OFF", "Reading", "Relax", "Concentrate",
					"Energize", "Red", "Orange", "Blue", "Romantic",
					"Rainbow", ((char) 8) + "OFF", ((char) 8) + "ON", ((char) 8) + "RANDOM"};
			
			for(String removeKey : moodsToRemove){
				moodStateMap.remove(removeKey);
			}
			
			
			String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea", "Deep Sea", "Deep Sea", "Fruit", "Fruit", "Fruit"};
			int[] simpleSat = {144, 211 ,49, 232, 253, 230, 253, 244, 254, 173};
			int[] simpleHue = {15331, 13122, 33863, 34495, 45489, 1111, 45489, 15483, 25593, 64684};
			
			for(int i = 0; i< simpleNames.length; i++){
				BulbState hs = new BulbState();
				hs.sat=(short)simpleSat[i];
				hs.hue=simpleHue[i];
				hs.on=true;
				hs.effect="none";
			
				ArrayList<String> states;
				if(moodStateMap.containsKey(simpleNames[i]))
					states = moodStateMap.get(simpleNames[i]);
				else
					states =  new ArrayList<String>();
				states.add(gson.toJson(hs));
				moodStateMap.put(simpleNames[i], states);
			}
			
			for(String key : moodStateMap.keySet()){
				
				ArrayList<String> stateJson = moodStateMap.get(key);
				//bug fix in case there are any empty bulbstates in the old system
				for(int i = 0; i<stateJson.size(); i++){
					if(stateJson.get(i)==null || gson.fromJson(stateJson.get(i),BulbState.class)==null)
						stateJson.remove(i);
				}
				
				Event[] events = new Event[stateJson.size()];
				for(int i = 0; i< stateJson.size(); i++){
					Event e = new Event();
					e.state = gson.fromJson(stateJson.get(i), BulbState.class);
					e.time=0;
					e.channel=i;
					events[i]=e;
				}
				Mood m = new Mood();
				m.usesTiming=false;
				m.timeAddressingRepeatPolicy=false;
				m.numChannels = stateJson.size();
				m.events = events;
				
				cv.put(MoodColumns.MOOD, key);
				cv.put(MoodColumns.STATE, HueUrlEncoder.encode(m));
				db.insert(MoodColumns.TABLE_NAME, null, cv);
			}
			
			//Construct animated fruit mood
			{
			BulbState bs1 = new BulbState();
			bs1.on = true;
			bs1.transitiontime = 50;
			bs1.sat = 244;
			bs1.hue = 15483;
			
			BulbState bs2 = new BulbState();
			bs2.on = true;
			bs2.transitiontime = 50;
			bs2.sat = 254;
			bs2.hue= 25593;
			
			BulbState bs3 = new BulbState();
			bs3.on = true;
			bs3.transitiontime = 50;
			bs3.sat = 173;
			bs3.hue= 64684;
			
			Event e1 = new Event(bs1, 0, 0);
			Event e2 = new Event(bs2, 1, 0);
			Event e3 = new Event(bs3, 2, 0);
			Event e4 = new Event(bs2, 0, 100);
			Event e5 = new Event(bs3, 1, 100);
			Event e6 = new Event(bs1, 2, 100);
			Event e7 = new Event(bs3, 0, 200);
			Event e8 = new Event(bs1, 1, 200);
			Event e9 = new Event(bs2, 2, 200);
			Event[] events = {e1,e2,e3,e4,e5,e6,e7,e8,e9};
			
			Mood m = new Mood();
			m.usesTiming = true;
			m.timeAddressingRepeatPolicy = false;
			m.numChannels = 3;
			m.setInfiniteLooping(true);
			m.events = events;
			
			cv.put(MoodColumns.MOOD, "Fruity");
			cv.put(MoodColumns.STATE, HueUrlEncoder.encode(m));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			}
			//Construct timed sunset mood
			{
			BulbState bs1 = new BulbState();
			bs1.on = true;
			bs1.transitiontime = 100;
			bs1.sat = 211;
			bs1.hue = 13122;
			
			BulbState bs2 = new BulbState();
			bs2.on = true;
			bs2.transitiontime = 600;
			bs2.sat = 200;
			bs2.hue= 8027;
			
			BulbState bs3 = new BulbState();
			bs3.on = false;
			bs3.transitiontime = 600;
			
			Event e1 = new Event(bs1, 0, 0);
			Event e2 = new Event(bs2, 0, 100);
			Event e3 = new Event(bs3, 0, 700);
			Event[] events = {e1,e2,e3};
			
			Mood m = new Mood();
			m.usesTiming = true;
			m.timeAddressingRepeatPolicy = false;
			m.numChannels = 1;
			m.setInfiniteLooping(false);
			m.events = events;
			
			cv.put(MoodColumns.MOOD, "Sunset");
			cv.put(MoodColumns.STATE, HueUrlEncoder.encode(m));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			}
			
		}

	}
}
