package com.kuxhausen.huemore.persistence;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbState;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 1;
	Gson gson = new Gson();

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
				+ " TEXT," + MoodColumns.PRECEDENCE + " INTEGER,"
				+ MoodColumns.STATE + " TEXT" + ");");

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
			/**updatedPopulate()**/
			String[] mSelectionArgs = { "OFF", "Reading", "Relax", "Concentrate",
					"Energize" };
			db.delete(MoodColumns.TABLE_NAME, DatabaseDefinitions.MoodColumns.MOOD
					+ "=? or " + DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=?", mSelectionArgs);
			String[] gSelectionArgs = { "ALL",
					DatabaseDefinitions.PreferencesKeys.ALL };
			db.delete(GroupColumns.TABLE_NAME,
					DatabaseDefinitions.GroupColumns.GROUP + "=? or "
							+ DatabaseDefinitions.GroupColumns.GROUP + "=?",
					gSelectionArgs);

			ContentValues cv = new ContentValues();
			BulbState hs = new BulbState();

			cv.clear();
			cv.put(MoodColumns.MOOD, "Reading");
			hs.sat = (144);
			hs.hue = (15331);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);

			cv.clear();
			cv.put(MoodColumns.MOOD, "Energize");
			hs.sat = (232);
			hs.hue = (34495);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);

			cv.clear();
			cv.put(MoodColumns.MOOD, "Relax");
			hs.sat = (211);
			hs.hue = (13122);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);

			cv.clear();
			cv.put(MoodColumns.MOOD, "Concentrate");
			hs.sat = (49);
			hs.hue = (33863);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);

			cv.clear();
			cv.put(MoodColumns.MOOD, "Sunset");
			hs.sat = (200);
			hs.hue = (8027);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			cv.clear();
			cv.put(MoodColumns.MOOD, "Sunset");
			hs.sat = (202);
			hs.hue = (12327);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			
			/**updatedTwoPointOh**/
			db.execSQL("CREATE TABLE " + AlarmColumns.TABLE_NAME + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
					+ AlarmColumns.STATE + " TEXT,"
					+ AlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");
			
			/** updatedTwoPointOnePointOne() **/
			String[] temp = { "Red", "Orange", "Blue", "Romantic",
			"Rainbow" };
			mSelectionArgs = temp;
			db.delete(MoodColumns.TABLE_NAME, DatabaseDefinitions.MoodColumns.MOOD
					+ "=? or " + DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=?", mSelectionArgs);
		
			
			cv.clear();
			cv.put(MoodColumns.MOOD, "Deep Sea");
			hs.sat = (253);
			hs.hue = (45489);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			cv.clear();
			cv.put(MoodColumns.MOOD, "Deep Sea");
			hs.sat = (230);
			hs.hue = (1111);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			cv.clear();
			cv.put(MoodColumns.MOOD, "Deep Sea");
			hs.sat = (253);
			hs.hue = (45489);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
		
			cv.clear();
			cv.put(MoodColumns.MOOD, "Fruit");
			hs.sat = (244);
			hs.hue = (15483);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			cv.clear();
			cv.put(MoodColumns.MOOD, "Fruit");
			hs.sat = (254);
			hs.hue = (25593);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);
			cv.put(MoodColumns.MOOD, "Fruit");
			hs.sat = (173);
			hs.hue = (64684);
			hs.on = true;
			hs.effect = "none";
			cv.put(MoodColumns.STATE, gson.toJson(hs));
			db.insert(MoodColumns.TABLE_NAME, null, cv);

			/**updatedTwoPointFour**/
			String[] temp2 = {((char) 8) + "OFF", ((char) 8) + "ON", ((char) 8) + "RANDOM"};
			mSelectionArgs = temp2;
			db.delete(MoodColumns.TABLE_NAME, DatabaseDefinitions.MoodColumns.MOOD
					+ "=? or " + DatabaseDefinitions.MoodColumns.MOOD + "=? or "
					+ DatabaseDefinitions.MoodColumns.MOOD + "=?", mSelectionArgs);
			
			/*
			String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
			Cursor cursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
			
			ArrayList<String> moodStates = new ArrayList<String>();
			while (cursor.moveToNext()) {
				moodStates.add(cursor.getString(0));
			}
			String[] moodS = moodStates.toArray(new String[moodStates.size()]);
			*/
		}

	}
}
