package com.kuxhausen.huemore.persistence;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayingMood;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Mood;

public class LampShadeProvider extends ContentProvider {

  DatabaseHelper mOpenHelper;

  /**
   * Projection maps used to select columns from the database
   */
  private static HashMap<String, String> sGroupsMap, sMoodsMap, sGroupBulbsMap, sAlarmsMap,
      sNetBulbsMap, sNetConnectionsMap, sPlayingMoodMap;
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
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, GroupColumns.PATH_GROUPS, GROUPS);
      sGroupsMap = new HashMap<String, String>();

      sGroupsMap.put(BaseColumns._ID, BaseColumns._ID);
      sGroupsMap.put(GroupColumns.GROUP, GroupColumns.GROUP);
      sGroupsMap.put(GroupColumns.PRECEDENCE, GroupColumns.PRECEDENCE);
      sGroupsMap.put(GroupColumns.BULB_DATABASE_ID, GroupColumns.BULB_DATABASE_ID);

    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, MoodColumns.PATH_MOODS, MOODS);
      sMoodsMap = new HashMap<String, String>();

      sMoodsMap.put(BaseColumns._ID, BaseColumns._ID);
      sMoodsMap.put(MoodColumns.MOOD, MoodColumns.MOOD);
      sMoodsMap.put(MoodColumns.STATE, MoodColumns.STATE);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY,
          DatabaseDefinitions.GroupColumns.PATH_GROUPBULBS, GROUPBULBS);
      sGroupBulbsMap = new HashMap<String, String>();

      sGroupBulbsMap.put(BaseColumns._ID, BaseColumns._ID);
      sGroupBulbsMap.put(GroupColumns.GROUP, GroupColumns.GROUP);
      sGroupBulbsMap.put(GroupColumns.PRECEDENCE, GroupColumns.PRECEDENCE);
      sGroupBulbsMap.put(GroupColumns.BULB_DATABASE_ID, GroupColumns.BULB_DATABASE_ID);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, AlarmColumns.PATH_ALARMS, ALARMS);
      sAlarmsMap = new HashMap<String, String>();

      sAlarmsMap.put(BaseColumns._ID, BaseColumns._ID);
      sAlarmsMap.put(AlarmColumns.STATE, AlarmColumns.STATE);
      sAlarmsMap.put(AlarmColumns.INTENT_REQUEST_CODE, AlarmColumns.INTENT_REQUEST_CODE);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, AlarmColumns.PATH_INDIVIDUAL_ALARM,
          INDIVIDUAL_ALARM);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, NetBulbColumns.PATH, NETBULBS);
      sNetBulbsMap = new HashMap<String, String>();

      sNetBulbsMap.put(BaseColumns._ID, BaseColumns._ID);
      sNetBulbsMap.put(NetBulbColumns.NAME_COLUMN, NetBulbColumns.NAME_COLUMN);
      sNetBulbsMap.put(NetBulbColumns.DEVICE_ID_COLUMN, NetBulbColumns.DEVICE_ID_COLUMN);
      sNetBulbsMap
          .put(NetBulbColumns.CONNECTION_DATABASE_ID, NetBulbColumns.CONNECTION_DATABASE_ID);
      sNetBulbsMap.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.TYPE_COLUMN);
      sNetBulbsMap.put(NetBulbColumns.JSON_COLUMN, NetBulbColumns.JSON_COLUMN);
      sNetBulbsMap
          .put(NetBulbColumns.CURRENT_MAX_BRIGHTNESS, NetBulbColumns.CURRENT_MAX_BRIGHTNESS);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, NetConnectionColumns.PATH, NETCONNECTIONS);
      sNetConnectionsMap = new HashMap<String, String>();

      sNetConnectionsMap.put(BaseColumns._ID, BaseColumns._ID);
      sNetConnectionsMap.put(NetConnectionColumns.NAME_COLUMN, NetConnectionColumns.NAME_COLUMN);
      sNetConnectionsMap.put(NetConnectionColumns.DEVICE_ID_COLUMN,
          NetConnectionColumns.DEVICE_ID_COLUMN);
      sNetConnectionsMap.put(NetConnectionColumns.TYPE_COLUMN, NetConnectionColumns.TYPE_COLUMN);
      sNetConnectionsMap.put(NetConnectionColumns.JSON_COLUMN, NetConnectionColumns.JSON_COLUMN);
    }
    {
      sUriMatcher.addURI(DatabaseDefinitions.AUTHORITY, PlayingMood.PATH, PLAYINGMOOD);
      sPlayingMoodMap = new HashMap<String, String>();

      sPlayingMoodMap.put(BaseColumns._ID, BaseColumns._ID);
      sPlayingMoodMap.put(PlayingMood.COL_GROUP_VALUE, PlayingMood.COL_GROUP_VALUE);
      sPlayingMoodMap.put(PlayingMood.COL_MOOD_NAME, PlayingMood.COL_MOOD_NAME);
      sPlayingMoodMap.put(PlayingMood.COL_MOOD_VALUE, PlayingMood.COL_MOOD_VALUE);
      sPlayingMoodMap.put(PlayingMood.COL_INITIAL_MAX_BRI, PlayingMood.COL_INITIAL_MAX_BRI);
      sPlayingMoodMap.put(PlayingMood.COL_MILI_TIME_STARTED, PlayingMood.COL_MILI_TIME_STARTED);
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
        table = (DatabaseDefinitions.MoodColumns.TABLE_NAME);
        toNotify.add(DatabaseDefinitions.MoodColumns.MOODS_URI);
        break;
      default:
        // If the URI doesn't match any of the known patterns, throw an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int rowsAffected = db.delete(table, selection, selectionArgs);

    for (Uri me : toNotify)
      this.getContext().getContentResolver().notifyChange(me, null);

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
        qb.setProjectionMap(sPlayingMoodMap);
        table = PlayingMood.TABLE_NAME;
        toNotify.add(PlayingMood.URI);
        break;
      case NETCONNECTIONS:
        qb.setTables(NetConnectionColumns.TABLE_NAME);
        qb.setProjectionMap(sNetConnectionsMap);
        table = NetConnectionColumns.TABLE_NAME;
        toNotify.add(NetConnectionColumns.URI);
        break;
      case NETBULBS:
        qb.setTables(NetBulbColumns.TABLE_NAME);
        qb.setProjectionMap(sNetBulbsMap);
        table = NetBulbColumns.TABLE_NAME;
        toNotify.add(NetBulbColumns.URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI); // must notify the all mood that more bulbs exist
        break;
      case ALARMS:
        qb.setTables(DatabaseDefinitions.AlarmColumns.TABLE_NAME);
        qb.setProjectionMap(sAlarmsMap);
        table = DatabaseDefinitions.AlarmColumns.TABLE_NAME;
        toNotify.add(AlarmColumns.ALARMS_URI);
        toNotify.add(AlarmColumns.INDIVIDUAL_ALARM_URI);
        break;
      case GROUPS:
        qb.setTables(DatabaseDefinitions.GroupColumns.TABLE_NAME);
        qb.setProjectionMap(sGroupsMap);
        table = DatabaseDefinitions.GroupColumns.TABLE_NAME;
        toNotify.add(GroupColumns.GROUPS_URI);
        toNotify.add(GroupColumns.GROUPBULBS_URI);
        break;
      case MOODS:
        qb.setTables(DatabaseDefinitions.MoodColumns.TABLE_NAME);
        qb.setProjectionMap(sMoodsMap);
        table = DatabaseDefinitions.MoodColumns.TABLE_NAME;
        toNotify.add(DatabaseDefinitions.MoodColumns.MOODS_URI);
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

    for (Uri me : toNotify)
      this.getContext().getContentResolver().notifyChange(me, null);

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
        qb.setProjectionMap(sPlayingMoodMap);
        groupBy = null;
        break;
      case NETCONNECTIONS:
        qb.setTables(NetConnectionColumns.TABLE_NAME);
        qb.setProjectionMap(sNetConnectionsMap);
        groupBy = null;
        break;
      case NETBULBS:
        qb.setTables(NetBulbColumns.TABLE_NAME);
        qb.setProjectionMap(sNetBulbsMap);
        groupBy = null;
        break;
      case INDIVIDUAL_ALARM:
        qb.appendWhere(AlarmColumns._ID + "=" + uri.getLastPathSegment());
        qb.setTables(AlarmColumns.TABLE_NAME);
        qb.setProjectionMap(sAlarmsMap);
        groupBy = null;
        uri = AlarmColumns.ALARMS_URI;
        break;
      case ALARMS:
        qb.setTables(AlarmColumns.TABLE_NAME);
        qb.setProjectionMap(sAlarmsMap);
        groupBy = null;
        break;
      case GROUPS:
        qb.setTables(GroupColumns.TABLE_NAME);
        qb.setProjectionMap(sGroupsMap);
        groupBy = GroupColumns.GROUP;
        break;
      case GROUPBULBS:
        if ((selection != null)
            && selectionArgs.length > 0
            && (selectionArgs[0].equals(this.getContext().getString(R.string.cap_all)) || selectionArgs[0]
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
        qb.setProjectionMap(sGroupsMap);
        groupBy = null;
        break;
      case MOODS:
        if ((selection != null)
            && selectionArgs.length > 0
            && (selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))
                || selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))
                || selectionArgs[0].equals(this.getContext().getString(R.string.cap_off)) || selectionArgs[0]
                .charAt(0) == ((char) 8))) {

          String mood = null;

          if (selectionArgs[0].equals(this.getContext().getString(R.string.cap_random))
              || selectionArgs[0].equals(((char) 8) + "RANDOM")) {
            mood = LampShadeProvider.getEncodedRandom();
          } else if (selectionArgs[0].equals(this.getContext().getString(R.string.cap_on))
              || selectionArgs[0].equals(((char) 8) + "ON")) {
            mood = LampShadeProvider.getEncodedOn();
          } else if (selectionArgs[0].equals(this.getContext().getString(R.string.cap_off))
              || selectionArgs[0].equals(((char) 8) + "OFF")) {
            mood = LampShadeProvider.getEncodedOff();
          }

          String[] moodColumns = {MoodColumns.STATE};
          MatrixCursor mc = new MatrixCursor(moodColumns);
          Object[] tempRow = {mood};
          mc.addRow(tempRow);
          mc.setNotificationUri(getContext().getContentResolver(), uri);
          return mc;
        } else {
          qb.setTables(MoodColumns.TABLE_NAME);
          qb.setProjectionMap(sMoodsMap);
          groupBy = null;
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

      String[] moodColumns = {MoodColumns.STATE};
      MatrixCursor mc = new MatrixCursor(moodColumns);
      Object[] tempRow = {HueUrlEncoder.encode(m)};
      mc.addRow(tempRow);
      mc.setNotificationUri(getContext().getContentResolver(), uri);

      return mc;
    } else if (sUriMatcher.match(uri) == MOODS && selectionArgs == null) {
      String[] columns = {MoodColumns.MOOD, BaseColumns._ID, MoodColumns.STATE};
      MatrixCursor c1 = new MatrixCursor(columns);
      Object[] tempCol0 = {this.getContext().getString(R.string.cap_off), 0, getEncodedOff()};
      c1.addRow(tempCol0);
      Object[] tempCol1 = {this.getContext().getString(R.string.cap_on), 0, getEncodedOn()};
      c1.addRow(tempCol1);
      Object[] tempCol2 = {this.getContext().getString(R.string.cap_random), 0, getEncodedRandom()};
      c1.addRow(tempCol2);

      Cursor[] tempC = {c1, c2};
      cRay = tempC;
    } else if (sUriMatcher.match(uri) == GROUPS) {
      String[] columns = {GroupColumns.GROUP, BaseColumns._ID};
      MatrixCursor c1 = new MatrixCursor(columns);
      Object[] tempCol0 = {this.getContext().getString(R.string.cap_all), 0};
      c1.addRow(tempCol0);

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

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;

    // Does the update based on the incoming URI pattern
    switch (sUriMatcher.match(uri)) {
      case NETCONNECTIONS:
        count = db.update(NetConnectionColumns.TABLE_NAME, values, selection, selectionArgs);
        break;
      case NETBULBS:
        count = db.update(NetBulbColumns.TABLE_NAME, values, selection, selectionArgs);
        break;
      case ALARMS:
        count = db.update(AlarmColumns.TABLE_NAME, values, selection, selectionArgs);
        break;
      default:
        // If the incoming pattern is invalid, throws an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows updated.
    return count;
  }

  private static String getEncodedOn() {
    BulbState resultState = new BulbState();
    resultState.on = true;
    resultState.effect = "none";
    return HueUrlEncoder.encode(Utils.generateSimpleMood(resultState));
  }

  private static String getEncodedOff() {
    BulbState resultState = new BulbState();
    resultState.on = false;
    resultState.effect = "none";
    return HueUrlEncoder.encode(Utils.generateSimpleMood(resultState));
  }

  /**
   * random only handled here
   */
  private static String getEncodedRandom() {
    BulbState resultState = new BulbState();
    resultState.on = true;
    resultState.effect = "none";

    float[] hsv =
        {(float) (65535 * Math.random() * 360) / 65535, (float) (Math.random() * 5. + .25), 1};
    Float[] input = {hsv[0] / 360f, hsv[1]};
    resultState.xy = Utils.hsTOxy(input);


    return HueUrlEncoder.encode(Utils.generateSimpleMood(resultState));
  }

}
