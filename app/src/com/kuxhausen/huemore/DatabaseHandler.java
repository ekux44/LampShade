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
	private static final int DATABASE_VERSION = 2;
	private static final String MOOD_TABLE_NAME = "moods";
	private static final String GROUP_TABLE_NAME = "groups";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MOOD_TABLE_NAME + " (" + MoodColumns._ID
				+ " INTEGER PRIMARY KEY," + MoodColumns.MOOD + " TEXT,"
				+ MoodColumns.PRECEDENCE + " INTEGER," + MoodColumns.STATE
				+ " TEXT" + ");");

		db.execSQL("CREATE TABLE " + GROUP_TABLE_NAME + " (" + GroupColumns._ID
				+ " INTEGER PRIMARY KEY," + GroupColumns.GROUP + " TEXT,"
				+ GroupColumns.BULB + " TEXT" + ");");
	}

	public void initialPopulate() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(MoodColumns.MOOD, "Reading");
		
		db.insert(MOOD_TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Concentrate");
		
		db.insert(MOOD_TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Relax");
		db.insert(MOOD_TABLE_NAME, null, cv);

		cv.clear();
		cv.put(MoodColumns.MOOD, "Energize");
		db.insert(MOOD_TABLE_NAME, null, cv);

		cv.clear();
		cv.put(GroupColumns.GROUP, "ALL");
		db.insert(GROUP_TABLE_NAME, null, cv);

	}

	public void addGroup(String groupname, String[] bulbs) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();

		for (String bulb : bulbs) {
			cv.clear();
			cv.put(GroupColumns.GROUP, groupname);
			cv.put(GroupColumns.BULB, bulb);
			db.insert(GROUP_TABLE_NAME, null, cv);
		}
	}

	public Cursor getMoodCursor() {
		SQLiteDatabase db = this.getWritableDatabase();

		String[] columns = { MoodColumns.MOOD, MoodColumns._ID };
		Cursor data = db.query(MOOD_TABLE_NAME, columns, null, null,
				MoodColumns.MOOD, null, null);

		return data;
	}

	public Cursor getGroupCursor() {
		SQLiteDatabase db = this.getWritableDatabase();

		String[] columns = { GroupColumns.GROUP, GroupColumns._ID };
		Cursor data = db.query(GROUP_TABLE_NAME, columns, null, null,
				GroupColumns.GROUP, null, null);

		return data;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("asdf", "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS notes");
		onCreate(db);
	}
}
