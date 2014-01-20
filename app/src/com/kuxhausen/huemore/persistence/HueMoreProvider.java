package com.kuxhausen.huemore.persistence;

import java.util.HashMap;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.R;

public class HueMoreProvider extends ContentProvider {

	DatabaseHelper mOpenHelper;

	/**
	 * A projection map used to select columns from the database
	 */
	private static HashMap<String, String> sGroupsProjectionMap,
			sMoodsProjectionMap, sGroupBulbsProjectionMap, sAlarmsProjectionMap;
	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher sUriMatcher;
	/*
	 * Constants used by the Uri matcher to choose an action based on the
	 * pattern of the incoming URI
	 */
	// The incoming URI matches the Groups URI pattern
	private static final int GROUPS = 1, MOODS = 2, GROUPBULBS = 3, ALARMS = 4, INDIVIDUAL_ALARM = 5;

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
			// GROUPS operation
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, DatabaseDefinitions.GroupColumns.PATH_GROUPS, GROUPS);
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
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, DatabaseDefinitions.MoodColumns.PATH_MOODS, MOODS);
			sMoodsProjectionMap = new HashMap<String, String>();

			sMoodsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
			sMoodsProjectionMap.put(DatabaseDefinitions.MoodColumns.MOOD,
					DatabaseDefinitions.MoodColumns.MOOD);
			sMoodsProjectionMap.put(DatabaseDefinitions.MoodColumns.STATE,
					DatabaseDefinitions.MoodColumns.STATE);
		}
		{
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, DatabaseDefinitions.GroupColumns.PATH_GROUPBULBS, GROUPBULBS);
			sGroupBulbsProjectionMap = new HashMap<String, String>();

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
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, DatabaseDefinitions.AlarmColumns.PATH_ALARMS, ALARMS);
			sAlarmsProjectionMap = new HashMap<String, String>();

			sAlarmsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
			sAlarmsProjectionMap.put(DatabaseDefinitions.AlarmColumns.STATE,
					DatabaseDefinitions.AlarmColumns.STATE);
			sAlarmsProjectionMap.put(
					DatabaseDefinitions.AlarmColumns.INTENT_REQUEST_CODE,
					DatabaseDefinitions.AlarmColumns.INTENT_REQUEST_CODE);
		}
		{
			sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, DatabaseDefinitions.AlarmColumns.PATH_INDIVIDUAL_ALARM, INDIVIDUAL_ALARM);
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

		case ALARMS:
			table = (DatabaseDefinitions.AlarmColumns.TABLE_NAME);
			toNotify = DatabaseDefinitions.AlarmColumns.ALARMS_URI;
			break;
		case GROUPBULBS:
			table = (DatabaseDefinitions.GroupColumns.TABLE_NAME);
			toNotify = DatabaseDefinitions.GroupColumns.GROUPS_URI;
			break;
		case MOODS:
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

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long insertId = db.insert(qb.getTables(), null, values);
		if (insertId == -1) {
			// insert failed, do update
			// db.update("groups", null, cv);
			//TODO
		}

		this.getContext().getContentResolver().notifyChange(uri, null);

		return ContentUris.withAppendedId(uri, insertId);
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
		case INDIVIDUAL_ALARM:
			qb.appendWhere(DatabaseDefinitions.AlarmColumns._ID + "=" + uri.getLastPathSegment());
			qb.setTables(DatabaseDefinitions.AlarmColumns.TABLE_NAME);
			qb.setProjectionMap(sAlarmsProjectionMap);
			groupBy = null;
			uri = DatabaseDefinitions.AlarmColumns.ALARMS_URI;
			break;
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
		case GROUPBULBS:
			if((selection!=null) && selectionArgs.length>0 && (
					selectionArgs[0].equals(this.getContext().getString(R.string.cap_all)) 
					|| selectionArgs[0].charAt(0) == ((char) 8))){
				
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
				int numBulbs = settings.getInt(PreferenceKeys.NUMBER_OF_CONNECTED_BULBS, 1);
				
				//TODO dynamically handle columns to return
				//String[] groupColumns = { GroupColumns.GROUP, GroupColumns.BULB };
				String[] groupColumns = { GroupColumns.BULB };
				MatrixCursor mc = new MatrixCursor(groupColumns);
				
				for(int i = 0; i< numBulbs; i++){
					Object[] tempRow = {i +1};
					mc.addRow(tempRow);
				}
				
				mc.setNotificationUri(getContext().getContentResolver(), uri);
				return mc;
			}
			qb.setTables(DatabaseDefinitions.GroupColumns.TABLE_NAME);
			qb.setProjectionMap(sGroupsProjectionMap);
			groupBy = null;
			break;
		case MOODS:
			if((selection!=null) && selectionArgs.length>0 && (
					selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))
					|| selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))
					|| selectionArgs[0].equals(this.getContext().getString(R.string.cap_off))
					|| selectionArgs[0].charAt(0) == ((char) 8))){
				BulbState resultState = new BulbState();
					
				if (selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))
						|| selectionArgs[0].equals(((char) 8) + "RANDOM")) {
					// random only handled here 
					resultState.on = true;
					resultState.effect = "none";
					resultState.hue = (int) (65535 * Math.random());
					resultState.sat = (short) (255 * (Math.random() * 5. + .25));
					
				} else if(selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))
						|| selectionArgs[0].equals(((char) 8) + "ON")) {
					resultState.on = true;
					resultState.effect = "none";
				} else if(selectionArgs[0].equals(this.getContext().getString(R.string.cap_off))
						|| selectionArgs[0].equals(((char) 8) + "OFF")) {
					resultState.on = false;
					resultState.effect = "none";
				}
				Mood m = Utils.generateSimpleMood(resultState);
				
				String[] moodColumns = { MoodColumns.STATE };
				MatrixCursor mc = new MatrixCursor(moodColumns);
				Object[] tempRow = {HueUrlEncoder.encode(m)};
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
		Cursor c2 = qb.query(db,
				projection, // The columns to return from the query
				selection, // The columns for the where clause
				selectionArgs, // The values for the where clause
				groupBy, // don't group the rows
				null, // don't filter by row groups
				sortOrder 
				);
		
		Cursor[] cRay;
		if(sUriMatcher.match(uri) == MOODS && c2.getCount()<1){
			//If mood doesn't exist in db, return a blank mood
			BulbState resultState = new BulbState();
			
			Mood m = Utils.generateSimpleMood(resultState);
			
			String[] moodColumns = { MoodColumns.STATE };
			MatrixCursor mc = new MatrixCursor(moodColumns);
			Object[] tempRow = {HueUrlEncoder.encode(m)};
			mc.addRow(tempRow);
			mc.setNotificationUri(getContext().getContentResolver(), uri);
			
			return mc;
		}
		else if(sUriMatcher.match(uri) == MOODS && selectionArgs==null){
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
		}else if(sUriMatcher.match(uri) == GROUPS){
			String[] columns = { GroupColumns.GROUP, BaseColumns._ID };
			MatrixCursor c1 = new MatrixCursor(columns);
			Object[] tempCol0 = {this.getContext().getString(R.string.cap_all),0};
			c1.addRow(tempCol0);
			
			Cursor[] tempC = {c1,c2};
			cRay = tempC;
		}else{
			Cursor[] tempC = {c2};
			cRay = tempC;
		}
		MergeCursor c = new MergeCursor(cRay);
		
		
		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes. apparently the merge cursor doesn't forward notifications, so notify individually too!
		c.setNotificationUri(getContext().getContentResolver(), uri);
		c2.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;

		// Does the update based on the incoming URI pattern
		switch (sUriMatcher.match(uri)) {
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
