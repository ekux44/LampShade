package com.kuxhausen.huemore.persistence;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.Definitions.PlayingMood;
import com.kuxhausen.huemore.persistence.migrations.DatabaseHelper;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.HashMap;

public class LampShadeProvider extends ContentProvider {

  DatabaseHelper mOpenHelper;

  /**
   * A UriMatcher instance
   */
  private static final UriMatcher sUriMatcher;
  /**
   * Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
   */
  private static final int GROUPS = 1, GROUPBULBS = 2, MOODS = 3, ALARMS = 4,
      NETBULBS = 5, NETCONNECTIONS = 6, PLAYINGMOOD = 7;

  /**
   * projection mapping between content provider value names and sql column names to enable
   * unambiguous joins in alarm query
   */
  private static HashMap<String, String> sAlarmQueryProjectionMap;

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
      sUriMatcher.addURI(Definitions.AUTHORITY, GroupColumns.PATH_GROUPS, GROUPS);
      sUriMatcher.addURI(Definitions.AUTHORITY, GroupBulbColumns.PATH_GROUPBULBS, GROUPBULBS);
      sUriMatcher.addURI(Definitions.AUTHORITY, MoodColumns.PATH_MOODS, MOODS);
      sUriMatcher.addURI(Definitions.AUTHORITY, AlarmColumns.PATH_ALARMS, ALARMS);
      sUriMatcher.addURI(Definitions.AUTHORITY, NetBulbColumns.PATH, NETBULBS);
      sUriMatcher.addURI(Definitions.AUTHORITY, NetConnectionColumns.PATH, NETCONNECTIONS);
      sUriMatcher.addURI(Definitions.AUTHORITY, PlayingMood.PATH, PLAYINGMOOD);

      sAlarmQueryProjectionMap = new HashMap<String, String>();
      sAlarmQueryProjectionMap
          .put(AlarmColumns._ID, AlarmColumns.TABLE_NAME + "." + AlarmColumns._ID);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_GROUP_ID, AlarmColumns.COL_GROUP_ID);
      sAlarmQueryProjectionMap.put(GroupColumns.COL_GROUP_NAME, GroupColumns.COL_GROUP_NAME);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_MOOD_ID, AlarmColumns.COL_MOOD_ID);
      sAlarmQueryProjectionMap.put(MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_NAME);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_BRIGHTNESS, AlarmColumns.COL_BRIGHTNESS);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_IS_ENABLED, AlarmColumns.COL_IS_ENABLED);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_REPEAT_DAYS, AlarmColumns.COL_REPEAT_DAYS);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_YEAR, AlarmColumns.COL_YEAR);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_MONTH, AlarmColumns.COL_MONTH);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_DAY_OF_MONTH, AlarmColumns.COL_DAY_OF_MONTH);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_HOUR_OF_DAY, AlarmColumns.COL_HOUR_OF_DAY);
      sAlarmQueryProjectionMap.put(AlarmColumns.COL_MINUTE, AlarmColumns.COL_MINUTE);
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    ArrayList<Uri> toNotify = new ArrayList<Uri>();

    String table = null;

    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    switch (sUriMatcher.match(uri)) {
      case PLAYINGMOOD:
        table = PlayingMood.TABLE_NAME;
        toNotify.add(PlayingMood.URI);
        break;
      case NETCONNECTIONS:
        table = NetConnectionColumns.TABLE_NAME;
        toNotify.add(NetConnectionColumns.URI);
        toNotify.add(NetBulbColumns.URI);
        toNotify.add(GroupColumns.URI);
        toNotify.add(GroupBulbColumns.URI);
        break;
      case NETBULBS:
        table = NetBulbColumns.TABLE_NAME;
        toNotify.add(NetBulbColumns.URI);
        break;
      case ALARMS:
        table = (AlarmColumns.TABLE_NAME);
        toNotify.add(AlarmColumns.ALARMS_URI);
        break;
      case GROUPS:
        table = (GroupColumns.TABLE_NAME);
        toNotify.add(GroupColumns.URI);
        toNotify.add(GroupBulbColumns.URI);
        break;
      case GROUPBULBS:
        table = (GroupBulbColumns.TABLE_NAME);
        toNotify.add(GroupBulbColumns.URI);
        toNotify.add(GroupColumns.URI);
        break;
      case MOODS:
        table = (Definitions.MoodColumns.TABLE_NAME);
        toNotify.add(Definitions.MoodColumns.MOODS_URI);
        break;
      default:
        // If the URI doesn't match any of the known patterns, throw an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int rowsAffected = db.delete(table, selection, selectionArgs);

    for (Uri me : toNotify) {
      this.getContext().getContentResolver().notifyChange(me, null);
    }

    return rowsAffected;
  }

  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    ArrayList<Uri> toNotify = new ArrayList<Uri>();

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    // Constructs a new query builder and sets its table name
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    switch (sUriMatcher.match(uri)) {
      case PLAYINGMOOD:
        qb.setTables(PlayingMood.TABLE_NAME);
        toNotify.add(PlayingMood.URI);
        break;
      case NETCONNECTIONS:
        qb.setTables(NetConnectionColumns.TABLE_NAME);
        toNotify.add(NetConnectionColumns.URI);
        break;
      case NETBULBS:
        qb.setTables(NetBulbColumns.TABLE_NAME);
        toNotify.add(NetBulbColumns.URI);
        toNotify.add(GroupColumns.URI);
        toNotify.add(GroupBulbColumns.URI); // must notify the all mood that more bulbs exist
        break;
      case ALARMS:
        qb.setTables(AlarmColumns.TABLE_NAME);
        toNotify.add(AlarmColumns.ALARMS_URI);
        break;
      case GROUPS:
        qb.setTables(GroupColumns.TABLE_NAME);
        toNotify.add(GroupColumns.URI);
        toNotify.add(GroupBulbColumns.URI);
        break;
      case GROUPBULBS:
        qb.setTables(GroupBulbColumns.TABLE_NAME);
        toNotify.add(GroupBulbColumns.URI);
        toNotify.add(GroupColumns.URI);
        break;
      case MOODS:
        qb.setTables(Definitions.MoodColumns.TABLE_NAME);
        toNotify.add(Definitions.MoodColumns.MOODS_URI);
        break;
      default:
        // If the URI doesn't match any of the known patterns, throw an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    long insertId = db.insert(qb.getTables(), null, values);
    if (insertId == -1) {
      // insert failed
      // TODO handle better
    }

    if (sUriMatcher.match(uri) == NETBULBS) {
      //insert the NetBulb into the all mood
      String[] gColumns = {GroupColumns._ID};
      String gWhere = GroupColumns.COL_GROUP_FLAGS + "=?";
      String[] gWhereArgs = {"" + GroupColumns.FLAG_ALL};
      Cursor gCursor = db.query(GroupColumns.TABLE_NAME, gColumns, gWhere, gWhereArgs, null, null,
                                null);
      gCursor.moveToFirst();
      long allGroupId = gCursor.getLong(0);

      long netBulbsCount = DatabaseUtils.queryNumEntries(db, NetBulbColumns.TABLE_NAME);

      ContentValues gbValues = new ContentValues();
      gbValues.put(GroupBulbColumns.COL_GROUP_ID, allGroupId);
      gbValues.put(GroupBulbColumns.COL_BULB_PRECEDENCE, netBulbsCount - 1);
      gbValues.put(GroupBulbColumns.COL_NET_BULB_ID, insertId);
      db.insert(GroupBulbColumns.TABLE_NAME, null, gbValues);
      gCursor.close();
    }

    for (Uri me : toNotify) {
      this.getContext().getContentResolver().notifyChange(me, null);
    }

    return ContentUris.withAppendedId(uri, insertId);
  }

  @Override
  public boolean onCreate() {
    // Creates a new helper object. Note that the database itself isn't opened until something tries
    // to access it, and it's only created if it doesn't already exist.
    mOpenHelper = new DatabaseHelper(getContext());

    // Assumes that any failures will be reported by a thrown exception.
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {

    // Opens the database object in "read" mode, since no writes need to be done.
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();

    // Constructs a new query builder and sets its table name
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    String groupBy = null;
    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    switch (sUriMatcher.match(uri)) {
      case PLAYINGMOOD:
        qb.setTables(PlayingMood.TABLE_NAME);
        groupBy = null;
        break;
      case NETCONNECTIONS:
        qb.setTables(NetConnectionColumns.TABLE_NAME);
        groupBy = null;
        break;
      case NETBULBS:
        qb.setTables(NetBulbColumns.TABLE_NAME);
        groupBy = null;
        break;
      case ALARMS:
        qb.setProjectionMap(sAlarmQueryProjectionMap);
        qb.setTables(AlarmColumns.TABLE_NAME
                     + " JOIN "
                     + GroupColumns.TABLE_NAME
                     + " ON (" + AlarmColumns.COL_GROUP_ID + " = " + GroupColumns.TABLE_NAME + "."
                     + GroupColumns._ID + ")"
                     + " JOIN "
                     + MoodColumns.TABLE_NAME
                     + " ON (" + AlarmColumns.COL_MOOD_ID + " = " + MoodColumns.TABLE_NAME + "."
                     + MoodColumns._ID + ")");
        groupBy = null;
        if (sortOrder == null || sortOrder.equals("")) {
          sortOrder =
              AlarmColumns.COL_HOUR_OF_DAY + " ASC, " + AlarmColumns.COL_MINUTE + " ASC";
        }
        break;
      case GROUPS:
        qb.setTables(GroupColumns.TABLE_NAME);
        groupBy = null;
        if (sortOrder == null || sortOrder.equals("")) {
          sortOrder =
              GroupColumns.COL_GROUP_PRIORITY + " DESC," + GroupColumns.COL_GROUP_LOWERCASE_NAME
              + " COLLATE UNICODE";
        }
        break;
      case GROUPBULBS:
        qb.setTables(GroupBulbColumns.TABLE_NAME);
        groupBy = null;
        break;
      case MOODS:
        qb.setTables(MoodColumns.TABLE_NAME);
        groupBy = null;
        if (sortOrder == null || sortOrder.equals("")) {
          sortOrder =
              MoodColumns.COL_MOOD_PRIORITY + " DESC," + MoodColumns.COL_MOOD_LOWERCASE_NAME
              + " COLLATE UNICODE";
        }
        break;
      default:
        // If the URI doesn't match any of the known patterns, throw an
        // exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    /*
     * Performs the query. If no problems occur trying to read the database, then a Cursor object is
     * returned; otherwise, the cursor variable contains null. If no records were selected, then the
     * Cursor object is empty, and Cursor.getCount() returns 0.
     */
    Cursor c2 = qb.query(db, projection, // The columns to return from the query
                         selection, // The columns for the where clause
                         selectionArgs, // The values for the where clause
                         groupBy, // don't group the rows
                         null, // don't filter by row groups
                         sortOrder);

    Cursor[] cRay;
    if (sUriMatcher.match(uri) == MOODS && c2.getCount() < 1) {
      // If mood doesn't exist in db, return a blank mood
      BulbState resultState = new BulbState();

      Mood m = new Mood.Builder(resultState).build();

      String[] moodColumns = {MoodColumns.COL_MOOD_VALUE};
      MatrixCursor mc = new MatrixCursor(moodColumns);
      Object[] tempRow = {HueUrlEncoder.encode(m)};
      mc.addRow(tempRow);
      mc.setNotificationUri(getContext().getContentResolver(), uri);

      return mc;
    } else {
      Cursor[] tempC = {c2};
      cRay = tempC;
    }
    MergeCursor c = new MergeCursor(cRay);

    // Tells the Cursor what URI to watch, so it knows when its source data
    // changes. apparently the merge cursor doesn't forward notifications, so notify individually
    // too!
    c.setNotificationUri(getContext().getContentResolver(), uri);
    c2.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    ArrayList<Uri> toNotify = new ArrayList<Uri>();

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;

    // Does the update based on the incoming URI pattern
    switch (sUriMatcher.match(uri)) {
      case MOODS:
        count = db.update(MoodColumns.TABLE_NAME, values, selection, selectionArgs);
        if (values.size() == 1 && values.containsKey(MoodColumns.COL_MOOD_PRIORITY)) {
          //If only the mood priority changed,
          //Don't notify because there's no animation for list reordering yet
        } else {
          toNotify.add(MoodColumns.MOODS_URI);
        }
        break;
      case NETCONNECTIONS:
        count = db.update(NetConnectionColumns.TABLE_NAME, values, selection, selectionArgs);
        toNotify.add(NetConnectionColumns.URI);
        break;
      case NETBULBS:
        count = db.update(NetBulbColumns.TABLE_NAME, values, selection, selectionArgs);
        if (values.size() == 1 && values.containsKey(NetBulbColumns.CURRENT_MAX_BRIGHTNESS)) {
          //If only the group max brightness changed,
          //Don't notify. This should be moved to a separate table at some point.
        } else {
          toNotify.add(NetBulbColumns.URI);
        }
        break;
      case GROUPS:
        count = db.update(GroupColumns.TABLE_NAME, values, selection, selectionArgs);
        toNotify.add(GroupColumns.URI);
        break;
      case GROUPBULBS:
        count = db.update(GroupBulbColumns.TABLE_NAME, values, selection, selectionArgs);
        toNotify.add(GroupBulbColumns.URI);
        break;
      case ALARMS:
        count = db.update(AlarmColumns.TABLE_NAME, values, selection, selectionArgs);
        toNotify.add(AlarmColumns.ALARMS_URI);
        break;
      default:
        // If the incoming pattern is invalid, throws an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    for (Uri me : toNotify) {
      this.getContext().getContentResolver().notifyChange(me, null);
    }

    // Returns the number of rows updated.
    return count;
  }
}
