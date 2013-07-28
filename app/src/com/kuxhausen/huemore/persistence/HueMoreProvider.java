package com.kuxhausen.huemore.persistence;

import java.util.HashMap;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.R;

public class HueMoreProvider extends ContentProvider {

	DatabaseHelper mOpenHelper;
	Gson gson = new Gson();

	/**
	 * A projection map used to select columns from the database
	 */
	private static HashMap<String, String> sGroupsProjectionMap,
			sMoodsProjectionMap, sGroupBulbsProjectionMap,
			sMoodStatesProjectionMap, sAlarmsProjectionMap;
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
			MOODSTATES = 4, ALARMS = 5;

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
		{
			// Add a pattern that routes URIs terminated with "groups" to a
			// GROUPS
			// operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, "alarms", ALARMS);
			// Creates a new projection map instance. The map returns a column
			// name
			// given a string. The two are usually equal.
			sAlarmsProjectionMap = new HashMap<String, String>();

			// Maps the string "_ID" to the column name "_ID"
			sAlarmsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

			sAlarmsProjectionMap.put(DatabaseDefinitions.AlarmColumns.STATE,
					DatabaseDefinitions.AlarmColumns.STATE);
			sAlarmsProjectionMap.put(
					DatabaseDefinitions.AlarmColumns.INTENT_REQUEST_CODE,
					DatabaseDefinitions.AlarmColumns.INTENT_REQUEST_CODE);
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

		case ALARMS:
			table = (DatabaseDefinitions.AlarmColumns.TABLE_NAME);
			toNotify = DatabaseDefinitions.AlarmColumns.ALARMS_URI;
			break;
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
		case ALARMS:
			qb.setTables(DatabaseDefinitions.AlarmColumns.TABLE_NAME);
			qb.setProjectionMap(sAlarmsProjectionMap);
			table = DatabaseDefinitions.AlarmColumns.TABLE_NAME;
			break;
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
		case ALARMS:
			qb.setTables(DatabaseDefinitions.AlarmColumns.TABLE_NAME);
			qb.setProjectionMap(sAlarmsProjectionMap);
			groupBy = null;
			break;
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
			if(selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))
					||selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))
					||selectionArgs[0].equals(this.getContext().getString(R.string.cap_off))){
				BulbState resultState = new BulbState();
					
				if (selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))) {
					// random only handled here 
					resultState.on = true;
					resultState.effect = "none";
					resultState.hue = (int) (65535 * Math.random());
					resultState.sat = (short) (255 * (Math.random() * 5. + .25));
					
				} else if(selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))){
					resultState.on = true;
					resultState.effect = "none";
				} else if(selectionArgs[0].equals(this.getContext().getString(R.string.cap_off))){
					resultState.on = false;
					resultState.effect = "none";
				}
				String[] moodColumns = { MoodColumns.STATE };
				MatrixCursor mc = new MatrixCursor(moodColumns);
				Object[] tempRow = {gson.toJson(resultState)};
				mc.addRow(tempRow);
				mc.setNotificationUri(getContext().getContentResolver(), uri);
				return mc;
			}
			else {
			qb.setTables(DatabaseDefinitions.MoodColumns.TABLE_NAME);
			qb.setProjectionMap(sMoodsProjectionMap);
			groupBy = null;
			}
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
		Cursor c2 = qb.query(db, // The database to query
				projection, // The columns to return from the query
				selection, // The columns for the where clause
				selectionArgs, // The values for the where clause
				groupBy, // don't group the rows
				null, // don't filter by row groups
				sortOrder // The sort order
				);
		Cursor[] cRay;
		if(sUriMatcher.match(uri) == MOODS){
			String[] columns = { MoodColumns.MOOD, BaseColumns._ID };
			MatrixCursor c1 = new MatrixCursor(columns);
			Object[] tempCol0 = {this.getContext().getString(R.string.cap_off),0};
			c1.addRow(tempCol0);
			Object[] tempCol1 = {this.getContext().getString(R.string.cap_on),0};
			c1.addRow(tempCol1);
			Object[] tempCol2 = {this.getContext().getString(R.string.cap_random),0};
			c1.addRow(tempCol2);
			
			Cursor[] tempC = {c1,c2};
			cRay = tempC;
		}else{
			Cursor[] tempC = {c2};
			cRay = tempC;
		}
		MergeCursor c = new MergeCursor(cRay);
		
		
		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO replace with better version at some point
		// delete(uri,selection,selectionArgs);
		// insert(uri,values);
		// return 0;

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String finalWhere;

		// Does the update based on the incoming URI pattern
		switch (sUriMatcher.match(uri)) {

		// If the incoming URI matches the general notes pattern, does the
		// update based on
		// the incoming data.
		case ALARMS:

			// Does the update and returns the number of rows updated.
			count = db.update(AlarmColumns.TABLE_NAME, // The database table
														// name.
					values, // A map of column names and new values to use.
					selection, // The where clause column names.
					selectionArgs // The where clause column values to select
									// on.
					);
			break;

		// If the incoming URI matches a single note ID, does the update based
		// on the incoming
		// data, but modifies the where clause to restrict it to the particular
		// note ID.
		/*
		 * case NOTE_ID: // From the incoming URI, get the note ID String noteId
		 * = uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION);
		 * 
		 * /* Starts creating the final WHERE clause by restricting it to the
		 * incoming note ID.
		 *//*
			 * finalWhere = NotePad.Notes._ID + // The ID column name " = " + //
			 * test for equality uri.getPathSegments(). // the incoming note ID
			 * get(NotePad.Notes.NOTE_ID_PATH_POSITION) ;
			 * 
			 * // If there were additional selection criteria, append them to
			 * the final WHERE // clause if (where !=null) { finalWhere =
			 * finalWhere + " AND " + where; }
			 * 
			 * 
			 * // Does the update and returns the number of rows updated. count
			 * = db.update( NotePad.Notes.TABLE_NAME, // The database table
			 * name. values, // A map of column names and new values to use.
			 * finalWhere, // The final WHERE clause to use // placeholders for
			 * whereArgs whereArgs // The where clause column values to select
			 * on, or // null if the values are in the where argument. ); break;
			 */
		// If the incoming pattern is invalid, throws an exception.
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		/*
		 * Gets a handle to the content resolver object for the current context,
		 * and notifies it that the incoming URI changed. The object passes this
		 * along to the resolver framework, and observers that have registered
		 * themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);

		// Returns the number of rows updated.
		return count;
	}

}
