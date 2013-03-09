package com.kuxhausen.huemore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kuxhausen.huemore.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 3;

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
				+ " INTEGER," + GroupColumns.BULB + " TEXT" + ");");
	}

	public void initialPopulate() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(MoodColumns.MOOD, "Reading");

		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Concentrate");

		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Relax");
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Energize");
		db.insert(MoodColumns.TABLE_NAME, null, cv);

		cv.clear();
		cv.put(GroupColumns.GROUP, "ALL");
		db.insert(MoodColumns.TABLE_NAME, null, cv);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("asdf", "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}
}
