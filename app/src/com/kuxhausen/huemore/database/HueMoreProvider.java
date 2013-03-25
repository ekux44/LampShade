package com.kuxhausen.huemore.database;

import java.util.HashMap;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class HueMoreProvider extends ContentProvider {

	DatabaseHelper mOpenHelper;

	/**
	 * A projection map used to select columns from the database
	 */
	private static HashMap<String, String> sGroupsProjectionMap,
			sMoodsProjectionMap, sGroupBulbsProjectionMap,
			sMoodStatesProjectionMap;
	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher sUriMatcher;
	/*
	 * Constants used by the Uri matcher to choose an action based on the
	 * pattern of the incoming URI
	 */
	// The incoming URI matches the Groups URI pattern
	private static final int GROUPS = 1, MOODS = 2, GROUPBULBS = 3,
			MOODSTATES = 4;

	/**
	 * A block that instantiates and sets static objects
	 */
	static {

		/*
		 * Creates and initializes the URI matcher
		 */
		// Create a new instance
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		{
			// Add a pattern that routes URIs terminated with "groups" to a
			// GROUPS
			// operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, "groups", GROUPS);
			// Creates a new projection map instance. The map returns a column
			// name
			// given a string. The two are usually equal.
			sGroupsProjectionMap = new HashMap<String, String>();

			// Maps the string "_ID" to the column name "_ID"
			sGroupsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

			sGroupsProjectionMap.put(DatabaseDefinitions.GroupColumns.GROUP,
					DatabaseDefinitions.GroupColumns.GROUP);
			sGroupsProjectionMap.put(DatabaseDefinitions.GroupColumns.BULB,
					DatabaseDefinitions.GroupColumns.BULB);
			sGroupsProjectionMap.put(
					DatabaseDefinitions.GroupColumns.PRECEDENCE,
					DatabaseDefinitions.GroupColumns.PRECEDENCE);
		}
		{
			// Add a pattern that routes URIs terminated with "moods" to a MOODS
			// operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, "moods", MOODS);
			// Creates a new projection map instance. The map returns a column
			// name
			// given a string. The two are usually equal.
			sMoodsProjectionMap = new HashMap<String, String>();

			// Maps the string "_ID" to the column name "_ID"
			sMoodsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

			sMoodsProjectionMap.put(DatabaseDefinitions.MoodColumns.MOOD,
					DatabaseDefinitions.MoodColumns.MOOD);
			sMoodsProjectionMap.put(DatabaseDefinitions.MoodColumns.STATE,
					DatabaseDefinitions.MoodColumns.STATE);
			sMoodsProjectionMap.put(DatabaseDefinitions.MoodColumns.PRECEDENCE,
					DatabaseDefinitions.MoodColumns.PRECEDENCE);
		}
		{
			// Add a pattern that routes URIs terminated with "groups" to a
			// GROUPS
			// operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, "groupbulbs",
					GROUPBULBS);
			// Creates a new projection map instance. The map returns a column
			// name
			// given a string. The two are usually equal.
			sGroupBulbsProjectionMap = new HashMap<String, String>();

			// Maps the string "_ID" to the column name "_ID"
			sGroupBulbsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

			sGroupBulbsProjectionMap.put(
					DatabaseDefinitions.GroupColumns.GROUP,
					DatabaseDefinitions.GroupColumns.GROUP);
			sGroupBulbsProjectionMap.put(DatabaseDefinitions.GroupColumns.BULB,
					DatabaseDefinitions.GroupColumns.BULB);
			sGroupBulbsProjectionMap.put(
					DatabaseDefinitions.GroupColumns.PRECEDENCE,
					DatabaseDefinitions.GroupColumns.PRECEDENCE);
		}
		{
			// Add a pattern that routes URIs terminated with "moods" to a MOODS
			// operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, "moodstates",
					MOODSTATES);
			// Creates a new projection map instance. The map returns a column
			// name
			// given a string. The two are usually equal.
			sMoodStatesProjectionMap = new HashMap<String, String>();

			// Maps the string "_ID" to the column name "_ID"
			sMoodStatesProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

			sMoodStatesProjectionMap.put(DatabaseDefinitions.MoodColumns.MOOD,
					DatabaseDefinitions.MoodColumns.MOOD);
			sMoodStatesProjectionMap.put(DatabaseDefinitions.MoodColumns.STATE,
					DatabaseDefinitions.MoodColumns.STATE);
			sMoodStatesProjectionMap.put(
					DatabaseDefinitions.MoodColumns.PRECEDENCE,
					DatabaseDefinitions.MoodColumns.PRECEDENCE);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Constructs a new query builder and sets its table name
		String table = null;

		Uri toNotify = uri;// todo restructure so that GROUPS and GROUPBULBS
							// share notifications

		/**
		 * Choose the projection and adjust the "where" clause based on URI
		 * pattern-matching.
		 */
		switch (sUriMatcher.match(uri)) {
		// If the incoming URI is for notes, chooses the Notes projection

		case GROUPBULBS:
			table = (DatabaseDefinitions.GroupColumns.TABLE_NAME);
			toNotify = DatabaseDefinitions.GroupColumns.GROUPS_URI;
			break;
		case MOODSTATES:
			table = (DatabaseDefinitions.MoodColumns.TABLE_NAME);
			toNotify = DatabaseDefinitions.MoodColumns.MOODS_URI;
			break;

		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.delete(table, selection, selectionArgs);

		this.getContext().getContentResolver().notifyChange(uri, null);
		this.getContext().getContentResolver().notifyChange(toNotify, null);

		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = null;

		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		/**
		 * Choose the projection and adjust the "where" clause based on URI
		 * pattern-matching.
		 */
		switch (sUriMatcher.match(uri)) {
		// If the incoming URI is for notes, chooses the Notes projection
		case GROUPS:
			qb.setTables(DatabaseDefinitions.GroupColumns.TABLE_NAME);
			qb.setProjectionMap(sGroupsProjectionMap);
			table = DatabaseDefinitions.GroupColumns.TABLE_NAME;
			break;
		case MOODS:
			qb.setTables(DatabaseDefinitions.MoodColumns.TABLE_NAME);
			qb.setProjectionMap(sMoodsProjectionMap);
			table = DatabaseDefinitions.MoodColumns.TABLE_NAME;
			break;
		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long insertId = db.insert(qb.getTables(), null, values);
		if (insertId == -1) {
			// insert failed, do update
			// db.update("groups", null, cv);

		}

		this.getContext().getContentResolver().notifyChange(uri, null);

		return null;
	}

	@Override
	public boolean onCreate() {
		// Creates a new helper object. Note that the database itself isn't
		// opened until
		// something tries to access it, and it's only created if it doesn't
		// already exist.
		mOpenHelper = new DatabaseHelper(getContext());

		// Assumes that any failures will be reported by a thrown exception.
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String groupBy = null;
		/**
		 * Choose the projection and adjust the "where" clause based on URI
		 * pattern-matching.
		 */
		switch (sUriMatcher.match(uri)) {
		// If the incoming URI is for notes, chooses the Notes projection
		case GROUPS:
			qb.setTables(DatabaseDefinitions.GroupColumns.TABLE_NAME);
			qb.setProjectionMap(sGroupsProjectionMap);
			groupBy = DatabaseDefinitions.GroupColumns.GROUP;
			break;
		case MOODS:
			qb.setTables(DatabaseDefinitions.MoodColumns.TABLE_NAME);
			qb.setProjectionMap(sMoodsProjectionMap);
			groupBy = DatabaseDefinitions.MoodColumns.MOOD;
			break;

		case GROUPBULBS:
			qb.setTables(DatabaseDefinitions.GroupColumns.TABLE_NAME);
			qb.setProjectionMap(sGroupsProjectionMap);
			groupBy = null;
			break;
		case MOODSTATES:
			qb.setTables(DatabaseDefinitions.MoodColumns.TABLE_NAME);
			qb.setProjectionMap(sMoodsProjectionMap);
			groupBy = null;
			break;
		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		/*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = qb.query(db, // The database to query
				projection, // The columns to return from the query
				selection, // The columns for the where clause
				selectionArgs, // The values for the where clause
				groupBy, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);

		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
