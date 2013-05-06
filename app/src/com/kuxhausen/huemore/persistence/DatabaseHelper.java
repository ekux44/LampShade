package com.kuxhausen.huemore.persistence;

import android.content.ContentValues;
import android.content.Context;
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
	}

	public void initialPopulate() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		BulbState hs = new BulbState();

		cv.clear();
		cv.put(MoodColumns.MOOD, "Red");
		Double[] redPair = { 0.6472, 0.3316 };
		hs.xy = redPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Orange");
		Double[] orangePair = { 0.5663, 0.3978 };
		hs.xy = orangePair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Blue");
		Double[] bluePair = { 0.1721, 0.0500 };
		hs.xy = bluePair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Romantic");
		Double[] pinkPair = { 0.3385, 0.1680 };
		hs.xy = pinkPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = redPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Rainbow");
		hs.xy = orangePair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = pinkPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		Double[] greenPair = { 0.4020, 0.5041 };
		hs.xy = greenPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = bluePair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
		hs.xy = redPair;
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

	}

	public void updatedPopulate() {

		SQLiteDatabase db = this.getWritableDatabase();

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

		cv.put(MoodColumns.MOOD, PreferencesKeys.OFF);
		hs.on = false;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

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

		for (int i = 1; i <= PreferencesKeys.ALWAYS_FREE_BULBS; i++) {
			cv.clear();
			cv.put(GroupColumns.GROUP, PreferencesKeys.ALL);
			cv.put(GroupColumns.BULB, i);
			cv.put(GroupColumns.PRECEDENCE, i);
			db.insert(GroupColumns.TABLE_NAME, null, cv);
		}

	}

	public void updatedTwoPointOh() {
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL("CREATE TABLE " + AlarmColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
				+ AlarmColumns.STATE + " TEXT,"
				+ AlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");
	}
	public void updatedTwoPointOne() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		BulbState hs = new BulbState();
		
		
		cv.put(MoodColumns.MOOD, PreferencesKeys.ON);
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
	}
	public void updatedTwoPointOnePointOne() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		BulbState hs = new BulbState();
		
		cv.put(MoodColumns.MOOD, PreferencesKeys.RANDOM);
		hs.on = true;
		hs.effect = "none";
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);
	}
	
	public void addBulbs(int first, int last) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();

		for (int i = first; i <= last; i++) {
			cv.clear();
			cv.put(GroupColumns.GROUP, PreferencesKeys.ALL);
			cv.put(GroupColumns.BULB, i);
			cv.put(GroupColumns.PRECEDENCE, i);
			db.insert(GroupColumns.TABLE_NAME, null, cv);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}
}
