package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.kuxhausen.huemore.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 4;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
				+ MoodColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
				+ " TEXT," + MoodColumns.PRECEDENCE + " INTEGER,"
				+ MoodColumns.STATE + " TEXT" + ");");

		db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
				+ GroupColumns._ID + " INTEGER PRIMARY KEY,"
				+ GroupColumns.GROUP + " TEXT," + GroupColumns.PRECEDENCE
				+ " INTEGER," + GroupColumns.BULB + " INTEGER" + ");");
	}

	public void initialPopulate() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		Gson gson = new Gson();
		HueState hs = new HueState();

		cv.put(MoodColumns.MOOD, "Reading");
		hs.ct = (1000000 / 2700);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Concentrate");
		hs.ct = (1000000 / 3400);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Relax");
		hs.ct = (1000000 / 2500);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Energize");
		hs.ct = (1000000 / 6000);
		hs.on = true;
		cv.put(MoodColumns.STATE, gson.toJson(hs));
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		for (int i = 0; i < 50; i++) {
			cv.clear();
			cv.put(GroupColumns.GROUP, "All");
			cv.put(GroupColumns.BULB, i);
			cv.put(GroupColumns.PRECEDENCE, i);
			db.insert(GroupColumns.TABLE_NAME, null, cv);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("asdf", "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}
}
