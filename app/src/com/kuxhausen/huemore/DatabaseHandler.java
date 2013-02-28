package com.kuxhausen.huemore;

import com.kuxhausen.huemore.DatabaseDefinitions.MoodColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper{
	
	 private static final String DATABASE_NAME = "huemore.db";
	 private static final int DATABASE_VERSION = 1;
	 private static final String MOOD_TABLE_NAME = "moods";
	 private static final String GROUP_TABLE_NAME = "groups";
	
	public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	
    	db.execSQL("CREATE TABLE " + MOOD_TABLE_NAME + " ("
                + MoodColumns._ID + " INTEGER PRIMARY KEY,"
                + MoodColumns.MOOD + " TEXT,"
                + MoodColumns.PRECEDENCE + " INTEGER,"
                + MoodColumns.ON + " TEXT,"
                + MoodColumns.BRI + " INTEGER,"
                + MoodColumns.HUE + " INTEGER,"
                + MoodColumns.SAT + " INTEGER,"
                + MoodColumns.X + " REAL,"
                + MoodColumns.Y + " REAL,"
                + MoodColumns.CT + " INTEGER,"
                + MoodColumns.ALERT + " TEXT,"
                + MoodColumns.EFFECT + " TEXT"
                + ");");
    }	
    
    public void initialPopulate(){
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
    
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("asdf", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }
    
    public Cursor getMoodCursor(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	String[] columns = {MoodColumns.MOOD, MoodColumns._ID};
        Cursor data = db.query("moods", columns, 
            null, null, MoodColumns.MOOD, null, null);
        
        return data;
    }
}
