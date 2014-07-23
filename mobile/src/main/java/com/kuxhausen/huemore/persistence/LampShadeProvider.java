package com.kuxhausen.huemore.persistence;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.Definitions.PlayingMood;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;

public class LampShadeProvider extends ContentProvider {

  DatabaseHelper mOpenHelper;

  /**
   * A UriMatcher instance
   */
  private static final UriMatcher sUriMatcher;
  /**
   * Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
   */
  private static final int GROUPS = 1, MOODS = 2, GROUPBULBS = 3, ALARMS = 4, INDIVIDUAL_ALARM = 5,
      NETBULBS = 6, NETCONNECTIONS = 7, PLAYINGMOOD = 8;

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
      sUriMatcher.addURI(Definitions.AUTHORITY, MoodColumns.PATH_MOODS, MOODS);
      sUriMatcher.addURI(Definitions.AUTHORITY,
                         Definitions.GroupColumns.PATH_GROUPBULBS, GROUPBULBS);
      sUriMatcher.addURI(Definitions.AUTHORITY, AlarmColumns.PATH_ALARMS, ALARMS);
      sUriMatcher.addURI(Definitions.AUTHORITY, AlarmColumns.PATH_INDIVIDUAL_ALARM,
                         INDIVIDUAL_ALARM);
      sUriMatcher.addURI(Definitions.AUTHORITY, NetBulbColumns.PATH, NETBULBS);
      sUriMatcher.addURI(Definitions.AUTHORITY, NetConnectionColumns.PATH, NETCONNECTIONS);
      sUriMatcher.addURI(Definitions.AUTHORITY, PlayingMood.PATH, PLAYINGMOOD);
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
        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI);
        break;
      case NETBULBS:
        table = NetBulbColumns.TABLE_NAME;
        toNotify.add(NetBulbColumns.URI);
        break;
      case ALARMS:
        table = (AlarmColumns.TABLE_NAME);
        toNotify.add(AlarmColumns.ALARMS_URI);
        toNotify.add(AlarmColumns.INDIVIDUAL_ALARM_URI);
        break;
      case GROUPBULBS:
        table = (GroupColumns.TABLE_NAME);
        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI);
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

    String table = null;

    // Constructs a new query builder and sets its table name
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    switch (sUriMatcher.match(uri)) {
      case PLAYINGMOOD:
        qb.setTables(PlayingMood.TABLE_NAME);
        table = PlayingMood.TABLE_NAME;
        toNotify.add(PlayingMood.URI);
        break;
      case NETCONNECTIONS:
        qb.setTables(NetConnectionColumns.TABLE_NAME);
        table = NetConnectionColumns.TABLE_NAME;
        toNotify.add(NetConnectionColumns.URI);

        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI); // must notify the all mood that more bulbs exist
        break;
      case NETBULBS:
        qb.setTables(NetBulbColumns.TABLE_NAME);
        table = NetBulbColumns.TABLE_NAME;
        toNotify.add(NetBulbColumns.URI);
        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI); // must notify the all mood that more bulbs exist
        break;
      case ALARMS:
        qb.setTables(Definitions.AlarmColumns.TABLE_NAME);
        table = Definitions.AlarmColumns.TABLE_NAME;
        toNotify.add(AlarmColumns.ALARMS_URI);
        toNotify.add(AlarmColumns.INDIVIDUAL_ALARM_URI);
        break;
      case GROUPS:
        qb.setTables(Definitions.GroupColumns.TABLE_NAME);
        table = Definitions.GroupColumns.TABLE_NAME;
        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI);
        break;
      case MOODS:
        qb.setTables(Definitions.MoodColumns.TABLE_NAME);
        table = Definitions.MoodColumns.TABLE_NAME;
        toNotify.add(Definitions.MoodColumns.MOODS_URI);
        break;
      default:
        // If the URI doesn't match any of the known patterns, throw an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    long insertId = db.insert(qb.getTables(), null, values);
    if (insertId == -1) {
      // insert failed, do update
      // db.update("groups", null, cv);
      // TODO
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
      case INDIVIDUAL_ALARM:
        qb.appendWhere(AlarmColumns._ID + "=" + uri.getLastPathSegment());
        qb.setTables(AlarmColumns.TABLE_NAME);
        groupBy = null;
        uri = AlarmColumns.ALARMS_URI;
        break;
      case ALARMS:
        qb.setTables(AlarmColumns.TABLE_NAME);
        groupBy = null;
        break;
      case GROUPS:
        qb.setTables(GroupColumns.TABLE_NAME);
        groupBy = GroupColumns.GROUP;
        break;
      case GROUPBULBS:
        if ((selection != null)
            && selectionArgs.length > 0
            && (selectionArgs[0].equals(this.getContext().getString(R.string.cap_all))
                || selectionArgs[0]
                       .charAt(0) == ((char) 8))) {

          qb.setTables(NetBulbColumns.TABLE_NAME);
          String[] groupColumns = {NetBulbColumns._ID + " AS " + GroupColumns.BULB_DATABASE_ID};

          Cursor c = qb.query(db, groupColumns, // using our own projection for 'All' mood as it's
                              // hitting a different database.
                              null, null, groupBy, null, sortOrder);

          c.setNotificationUri(getContext().getContentResolver(), uri);
          return c;
        }
        qb.setTables(GroupColumns.TABLE_NAME);
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

      Mood m = Utils.generateSimpleMood(resultState);

      String[] moodColumns = {MoodColumns.COL_MOOD_VALUE};
      MatrixCursor mc = new MatrixCursor(moodColumns);
      Object[] tempRow = {HueUrlEncoder.encode(m)};
      mc.addRow(tempRow);
      mc.setNotificationUri(getContext().getContentResolver(), uri);

      return mc;
    } else if (sUriMatcher.match(uri) == GROUPS) {
      String[] columns = {GroupColumns.GROUP, BaseColumns._ID};
      MatrixCursor c1 = new MatrixCursor(columns);

      SQLiteQueryBuilder querryBulbs = new SQLiteQueryBuilder();
      querryBulbs.setTables(NetBulbColumns.TABLE_NAME);
      String[] groupColumns = {NetBulbColumns._ID};
      Cursor c = querryBulbs.query(db, groupColumns, null, null, null, null, null);
      // only show All group if there are any NetBulbs
      if (c.getCount() > 0) {
        Object[] tempCol0 = {this.getContext().getString(R.string.cap_all), 0};
        c1.addRow(tempCol0);
      }
      Cursor[] tempC = {c1, c2};
      cRay = tempC;
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
          toNotify.add(GroupColumns.GROUPS_URI);
          toNotify
              .add(GroupColumns.GROUPBULBS_URI); // must notify the all mood that more bulbs exist
        }
        break;
      case ALARMS:
        count = db.update(AlarmColumns.TABLE_NAME, values, selection, selectionArgs);
        toNotify.add(AlarmColumns.ALARMS_URI);
        toNotify.add(AlarmColumns.INDIVIDUAL_ALARM_URI);
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
