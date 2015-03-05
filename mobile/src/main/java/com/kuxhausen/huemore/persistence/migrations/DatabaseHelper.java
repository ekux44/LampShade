package com.kuxhausen.huemore.persistence.migrations;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Pair;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.alarm.DaysOfWeek;
import com.kuxhausen.huemore.net.hue.HueBulbData;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.migrations.DeprecatedDefinitions.DeprecatedGroupColumns;
import com.kuxhausen.huemore.persistence.migrations.DeprecatedDefinitions.DeprecatedAlarmColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.Definitions.PlayingMood;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "huemore.db";
  private static final int DATABASE_VERSION = 12;
  Gson gson = new Gson();
  private Context mContext;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    mContext = context;
  }

  @Override
  public void onOpen(SQLiteDatabase db) {
    super.onOpen(db);
    if (!db.isReadOnly()) {
      // Enable foreign key constraints
      db.execSQL("PRAGMA foreign_keys=ON;");
    }
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " (" + BaseColumns._ID
               + " INTEGER PRIMARY KEY," + MoodColumns.COL_MOOD_NAME + " TEXT,"
               + MoodColumns.COL_MOOD_VALUE
               + " TEXT"
               + ");");

    db.execSQL("CREATE TABLE " + DeprecatedGroupColumns.TABLE_NAME + " (" + BaseColumns._ID
               + " INTEGER PRIMARY KEY," + DeprecatedGroupColumns.GROUP + " TEXT,"
               + DeprecatedGroupColumns.PRECEDENCE
               + " INTEGER," + "Dbulb" + " INTEGER" + ");");

    this.onUpgrade(db, 1, DATABASE_VERSION);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    switch (oldVersion) {
      case 1: {
        ContentValues cv = new ContentValues();

        /** update 2.4/2.5/switch to serialized b64 **/

        String[] moodColumns = {MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_VALUE};
        Cursor cursor =
            db.query(Definitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
                     null, null);

        HashMap<String, ArrayList<String>> moodStateMap = new HashMap<String, ArrayList<String>>();

        while (cursor.moveToNext()) {
          String mood = cursor.getString(0);
          String state = cursor.getString(1);
          if (mood != null && state != null && !mood.equals("") && !state.equals("")
              && !state.equals("{}")) {
            ArrayList<String> states;
            if (moodStateMap.containsKey(mood)) {
              states = moodStateMap.get(mood);
            } else {
              states = new ArrayList<String>();
            }
            states.add(state);
            moodStateMap.put(mood, states);
          }
        }
        db.execSQL("DROP TABLE " + MoodColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + MoodColumns.COL_MOOD_NAME + " TEXT,"
                   + MoodColumns.COL_MOOD_VALUE
                   + " TEXT"
                   + ");");

        // remove standard moods that are no longer correct
        String[] moodsToRemove =
            {"OFF", "Reading", "Relax", "Concentrate", "Energize", "Red", "Orange", "Blue",
             "Romantic", "Rainbow", ((char) 8) + "OFF", ((char) 8) + "ON", ((char) 8) + "RANDOM"};

        for (String removeKey : moodsToRemove) {
          moodStateMap.remove(removeKey);
        }

        for (String key : moodStateMap.keySet()) {

          ArrayList<String> stateJson = moodStateMap.get(key);
          // bug fix in case there are any empty bulbstates in the old system
          for (int i = 0; i < stateJson.size(); i++) {
            if (stateJson.get(i) == null
                || gson.fromJson(stateJson.get(i), BulbState.class) == null) {
              stateJson.remove(i);
            }
          }

          Event[] events = new Event[stateJson.size()];
          for (int i = 0; i < stateJson.size(); i++) {
            Event e = new Event(gson.fromJson(stateJson.get(i), BulbState.class), i, 0l);
            events[i] = e;
          }
          Mood m = new Mood();
          m.setNumChannels(stateJson.size());
          m.setEvents(events);

          cv.put(MoodColumns.COL_MOOD_NAME, key);
          cv.put(MoodColumns.COL_MOOD_VALUE, HueUrlEncoder.encode(m));
          db.insert(MoodColumns.TABLE_NAME, null, cv);
        }
        cursor.close();
      }

      case 2: {
        db.execSQL("DROP TABLE IF EXISTS " + DeprecatedAlarmColumns.TABLE_NAME);

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DeprecatedAlarmColumns.TABLE_NAME + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY,"
            + DeprecatedAlarmColumns.STATE + " TEXT,"
            + DeprecatedAlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");

        // remove the sunset mood
        String[] moodArgs = {"Sunset"};
        db.delete(MoodColumns.TABLE_NAME, MoodColumns.COL_MOOD_NAME + " =?", moodArgs);
      }
      case 3: {
        //land and fall through to next case
      }
      case 4: {
        ContentValues cv = new ContentValues();
        String[] moodColumns = {MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_VALUE};
        Cursor moodCursor =
            db.query(Definitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
                     null, null);

        HashMap<String, String> moodMap = new HashMap<String, String>();

        while (moodCursor.moveToNext()) {
          String name = moodCursor.getString(0);
          String encodedMood = moodCursor.getString(1);

          moodMap.put(name, encodedMood);
        }

        // remove any nameless moods
        moodMap.remove("");
        moodMap.remove(null);

        // add the stock moods (and write over old hsv versions)
        moodMap.put("Reading", "BgQAAQAAjJTvChKYbxnjBwAQAAAA");
        moodMap.put("Relax", "BgQAAQAAjJQfGPqQb0U5HgAQAAAA");
        moodMap.put("Energize", "BgQAAQAAjJSvUGWKrywgFwAQAAAA﻿");
        moodMap.put("Concentrate", "BgQAAQAAjJSvx8qfr6eTBAAQAAAA﻿");
        moodMap.put("Deep Sea", "BgYAAQAAipSf6G-Ur9oDGCLl49NJ4s09ywYAhiICAAA=﻿");
        moodMap.put("Fruity",
                    "BvaHAQAAACYAwASOmB-hypSvUsQcgClifiBxUb4DwUgApoj5Fp58-EGyywCYABIUIsIqMTUBQBo=﻿");

        // add these stock moods but don't write over prior versions
        if (!moodMap.containsKey("Gentle Sunrise")) {
          moodMap.put("Gentle Sunrise",
                      "AQSAAQAAgDQApAGAJzfkJ8o85KtGLQMAk8j5riCB-ZYxfgDAZPIyfiB9bL5VtUAAMAFgwCSAQwA=");
        }
        if (!moodMap.containsKey("Gentle Sunset")) {
          moodMap.put("Gentle Sunset",
                      "AQSAAQAAgDQApAGAI-cHhj7kW1GOBwCTyd34iaDH-GrSiQHAJDAAMAFgQBWAQwA=");
        }
        if (!moodMap.containsKey("Living Night")) {
          moodMap.put("Living Night", "AfKHAAAAAEwAaGJWfu4rZb4IfDsAk4m_-TkqEvniQEQATAAEFBAVACYA");
        }
        if (!moodMap.containsKey("f.lux")) {
          moodMap
              .put("f.lux",
                   "AQxA5RmHN7_yNEQDWOqnAoAj5-ux8ufr6SQBAJDI-YGhD_lWlOMBACRyvitIYL5ljB8AAAFQFGIoEQAAAA==");
        }

        db.execSQL("DROP TABLE IF EXISTS " + MoodColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + MoodColumns.COL_MOOD_NAME + " TEXT,"
                   + MoodColumns.COL_MOOD_VALUE
                   + " TEXT"
                   + ");");

        for (String key : moodMap.keySet()) {
          cv.put(MoodColumns.COL_MOOD_NAME, key);
          cv.put(MoodColumns.COL_MOOD_VALUE, moodMap.get(key));
          db.insert(MoodColumns.TABLE_NAME, null, cv);
        }
        moodCursor.close();
      }
      case 5: {
        db.execSQL("CREATE TABLE " + NetConnectionColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + NetConnectionColumns.NAME_COLUMN + " TEXT,"
                   + NetConnectionColumns.DEVICE_ID_COLUMN + " TEXT,"
                   + NetConnectionColumns.TYPE_COLUMN
                   + " INTEGER," + NetConnectionColumns.JSON_COLUMN + " TEXT" + ");");

        db.execSQL("CREATE TABLE " + NetBulbColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + NetBulbColumns.NAME_COLUMN + " TEXT,"
                   + NetBulbColumns.DEVICE_ID_COLUMN + " TEXT,"
                   + NetBulbColumns.CONNECTION_DATABASE_ID
                   + " INTEGER," + NetBulbColumns.TYPE_COLUMN + " INTEGER,"
                   + NetBulbColumns.JSON_COLUMN
                   + " TEXT," + NetBulbColumns.CURRENT_MAX_BRIGHTNESS + " INTEGER,"
                   + " FOREIGN KEY ("
                   + NetBulbColumns.CONNECTION_DATABASE_ID + ") REFERENCES "
                   + NetConnectionColumns.TABLE_NAME + " (" + NetConnectionColumns._ID
                   + " ) ON DELETE CASCADE " + ");");

        /** Migrate the groups Database & add placeholder entries into the NetBulb table as needed */

        String[] oldGroupColumns =
            {DeprecatedGroupColumns._ID, DeprecatedGroupColumns.GROUP,
             DeprecatedGroupColumns.PRECEDENCE, "Dbulb"};
        Cursor oldGroupCursor =
            db.query(DeprecatedGroupColumns.TABLE_NAME, oldGroupColumns, null, null,
                     null, null, null);

        // load all the old group data into here <name, list of hue hub bulb <precedence, hub bulb
        // number>>
        HashMap<String, ArrayList<Pair<Integer, Integer>>> oldGroupMap =
            new HashMap<String, ArrayList<Pair<Integer, Integer>>>();

        while (oldGroupCursor.moveToNext()) {

          String name = oldGroupCursor.getString(1);

          int precedence = 0;
          try {
            precedence = oldGroupCursor.getInt(2);
          } catch (Exception e) {
          }
          Integer bulbNumber = oldGroupCursor.getInt(3);

          if (!oldGroupMap.containsKey(name)) {
            oldGroupMap.put(name, new ArrayList<Pair<Integer, Integer>>());
          }
          oldGroupMap.get(name).add(new Pair<Integer, Integer>(precedence, bulbNumber));
        }


        /* remove any illegal group names */
        {
          oldGroupMap.remove("");
          oldGroupMap.remove("ALL");
          oldGroupMap.remove(((char) 8) + "ALL");

        }

        /* now add placeholder entries for every bulb referenced in the groups * */

        // <hub bulb number, database id for corresponding NetBulb entry>
        HashMap<Integer, Long> hubIdToBaseIdMapping = new HashMap<Integer, Long>();
        for (String groupName : oldGroupMap.keySet()) {
          for (Pair<Integer, Integer> oldPair : oldGroupMap.get(groupName)) {
            int hubBulbNumber = oldPair.second;
            if (!hubIdToBaseIdMapping.containsKey(hubBulbNumber)) {
              ContentValues netBulbValues = new ContentValues();

              netBulbValues.put(NetBulbColumns.NAME_COLUMN, "?");
              netBulbValues.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NetBulbType.PHILIPS_HUE);
              netBulbValues.putNull(NetBulbColumns.CONNECTION_DATABASE_ID);
              netBulbValues.put(NetBulbColumns.DEVICE_ID_COLUMN, "" + hubBulbNumber);
              netBulbValues.put(NetBulbColumns.JSON_COLUMN, gson.toJson(new HueBulbData()));

              long baseId = db.insert(NetBulbColumns.TABLE_NAME, null, netBulbValues);
              hubIdToBaseIdMapping.put(hubBulbNumber, baseId);
            }
          }

        }


        /* rebuild the sql tables */
        db.execSQL("DROP TABLE IF EXISTS " + DeprecatedGroupColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + DeprecatedGroupColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + DeprecatedGroupColumns.GROUP + " TEXT,"
                   + DeprecatedGroupColumns.PRECEDENCE
                   + " INTEGER," + DeprecatedGroupColumns.BULB_DATABASE_ID + " INTEGER,"
                   + " FOREIGN KEY ("
                   + DeprecatedGroupColumns.BULB_DATABASE_ID + ") REFERENCES "
                   + NetBulbColumns.TABLE_NAME
                   + " ("
                   + NetBulbColumns._ID + " ) ON DELETE CASCADE " + ");");


        /* now add the groups to the new table */
        for (String groupName : oldGroupMap.keySet()) {
          for (Pair<Integer, Integer> oldPair : oldGroupMap.get(groupName)) {
            int bulbPrecidence = oldPair.first;
            long bulbBaseId = hubIdToBaseIdMapping.get(oldPair.second);

            ContentValues groupValues = new ContentValues();
            groupValues.put(DeprecatedGroupColumns.GROUP, groupName);
            groupValues.put(DeprecatedGroupColumns.PRECEDENCE, bulbPrecidence);
            groupValues.put(DeprecatedGroupColumns.BULB_DATABASE_ID, bulbBaseId);
            db.insert(DeprecatedGroupColumns.TABLE_NAME, null, groupValues);

          }
        }
        oldGroupCursor.close();
      }
      case 6: {
        //land and fall through to next case
      }
      case 7: {
        //land and fall through to next case
      }
      case 8: {
        //land and fall through to next case
      }
      case 9: {
        //TODO clean previous migrations or create non-upgrade path for first run performance
        ContentValues cv = new ContentValues();
        String[] moodColumns = {MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_VALUE};
        Cursor moodCursor =
            db.query(Definitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
                     null, null);

        HashMap<String, Pair<String, Pair<String, Integer>>>
            moodMap =
            new HashMap<String, Pair<String, Pair<String, Integer>>>();

        while (moodCursor.moveToNext()) {
          String visibleName = moodCursor.getString(0);
          String encodedMood = moodCursor.getString(1);
          String lowercaseName = visibleName.toLowerCase().trim();
          Integer priority = 1;

          while (moodMap.containsKey(lowercaseName)) {
            visibleName += " 1";
            lowercaseName += " 1";
          }
          moodMap.put(lowercaseName, new Pair<String, Pair<String, Integer>>(visibleName,
                                                                             new Pair<String, Integer>(
                                                                                 encodedMood,
                                                                                 priority)
          ));
        }

        String onName = mContext.getString(R.string.cap_on);
        moodMap.put(onName.toLowerCase(), new Pair<String, Pair<String, Integer>>(onName,
                                                                                  new Pair<String, Integer>(
                                                                                      getEncodedOn(),
                                                                                      2)
        ));

        String offName = mContext.getString(R.string.cap_off);
        moodMap.put(offName.toLowerCase(), new Pair<String, Pair<String, Integer>>(offName,
                                                                                   new Pair<String, Integer>(
                                                                                       getEncodedOff(),
                                                                                       2)
        ));

        // remove any nameless moods
        moodMap.remove("");
        moodMap.remove(null);

        db.execSQL("DROP TABLE IF EXISTS " + MoodColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " (" +
                   BaseColumns._ID + " INTEGER PRIMARY KEY," +
                   MoodColumns.COL_MOOD_LOWERCASE_NAME + " TEXT," +
                   MoodColumns.COL_MOOD_NAME + " TEXT," +
                   MoodColumns.COL_MOOD_VALUE + " TEXT," +
                   MoodColumns.COL_MOOD_PRIORITY + " INTEGER," +
                   "UNIQUE (" + MoodColumns.COL_MOOD_LOWERCASE_NAME + ") ON CONFLICT REPLACE" +
                   ");");

        for (String key : moodMap.keySet()) {
          Pair<String, Pair<String, Integer>> mapped = moodMap.get(key);
          String visibleName = mapped.first;
          String value = mapped.second.first;
          Integer priority = mapped.second.second;

          cv.put(MoodColumns.COL_MOOD_LOWERCASE_NAME, key.toLowerCase());
          cv.put(MoodColumns.COL_MOOD_NAME, visibleName);
          cv.put(MoodColumns.COL_MOOD_VALUE, value);
          cv.put(MoodColumns.COL_MOOD_PRIORITY, priority);
          db.insert(MoodColumns.TABLE_NAME, null, cv);
        }

        moodCursor.close();
      }
      case 10: {

        db.execSQL("DROP TABLE IF EXISTS " + PlayingMood.TABLE_NAME);

        db.execSQL("CREATE TABLE " + PlayingMood.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + PlayingMood.COL_GROUP_VALUE + " TEXT,"
                   + PlayingMood.COL_MOOD_NAME + " TEXT," + PlayingMood.COL_MOOD_VALUE + " TEXT,"
                   + PlayingMood.COL_MOOD_BRI + " INTEGER,"
                   + PlayingMood.COL_MILI_TIME_STARTED + " INTEGER,"
                   + PlayingMood.COL_INTERNAL_PROGRESS + " INTEGER"
                   + ");");
      }
      case 11: {
        /* query existing groups data */
        String[] oldGroupColumns =
            {DeprecatedGroupColumns.GROUP, DeprecatedGroupColumns.PRECEDENCE,
             DeprecatedGroupColumns.BULB_DATABASE_ID};
        Cursor oldGroupCursor =
            db.query(DeprecatedGroupColumns.TABLE_NAME, oldGroupColumns, null, null, null, null,
                     null);

        /* load all the old group data into here <name, <precedence,bulb_database_id>> */
        ArrayList<Pair<String, Pair<Long, Long>>>
            oldGroupList =
            new ArrayList<Pair<String, Pair<Long, Long>>>();

        oldGroupCursor.moveToPosition(-1);
        while (oldGroupCursor.moveToNext()) {

          String name = oldGroupCursor.getString(0);
          long precedence = 0;
          try {
            precedence = oldGroupCursor.getLong(1);
          } catch (Exception e) {
          }
          Long bulbDatabaseId = null;
          try {
            bulbDatabaseId = oldGroupCursor.getLong(2);
          } catch (Exception e) {
          }

          oldGroupList.add(new Pair<String, Pair<Long, Long>>(name, new Pair<Long, Long>(precedence,
                                                                                         bulbDatabaseId)));
        }
        oldGroupCursor.close();

        /* rebuild the sql tables */
        db.execSQL("DROP TABLE IF EXISTS " + DeprecatedGroupColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
                   + GroupColumns._ID + " INTEGER PRIMARY KEY,"
                   + GroupColumns.COL_GROUP_NAME + " TEXT,"
                   + GroupColumns.COL_GROUP_LOWERCASE_NAME + " TEXT,"
                   + GroupColumns.COL_GROUP_PRIORITY + " INTEGER,"
                   + GroupColumns.COL_GROUP_FLAGS + " INTEGER"
                   + ");");

        db.execSQL("CREATE TABLE " + GroupBulbColumns.TABLE_NAME + " ("
                   + GroupBulbColumns._ID + " INTEGER PRIMARY KEY,"
                   + GroupBulbColumns.COL_GROUP_ID + " INTEGER,"
                   + GroupBulbColumns.COL_BULB_PRECEDENCE + " INTEGER,"
                   + GroupBulbColumns.COL_NET_BULB_ID + " INTEGER,"
                   + " FOREIGN KEY (" + GroupBulbColumns.COL_GROUP_ID + ") REFERENCES "
                   + GroupColumns.TABLE_NAME + " (" + GroupColumns._ID + " ) ON DELETE CASCADE,"
                   + " FOREIGN KEY (" + GroupBulbColumns.COL_NET_BULB_ID + ") REFERENCES "
                   + NetBulbColumns.TABLE_NAME + " (" + NetBulbColumns._ID + " ) ON DELETE CASCADE"
                   + ");");


        /* now add existing groups to the new tables */
        for (Pair<String, Pair<Long, Long>> item : oldGroupList) {
          String groupName = item.first;
          Long bulbPrecidence = item.second.first;
          Long netBulbId = item.second.second;

          ContentValues groupValues = new ContentValues();
          groupValues.put(GroupColumns.COL_GROUP_NAME, groupName);
          groupValues.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, groupName.toLowerCase().trim());
          groupValues.put(GroupColumns.COL_GROUP_PRIORITY, GroupColumns.PRIORITY_UNSTARRED);
          groupValues.put(GroupColumns.COL_GROUP_FLAGS, GroupColumns.FLAG_NORMAL);
          long groupId = db.insert(GroupColumns.TABLE_NAME, null, groupValues);

          ContentValues groupBulbValues = new ContentValues();
          groupBulbValues.put(GroupBulbColumns.COL_GROUP_ID, groupId);
          groupBulbValues.put(GroupBulbColumns.COL_BULB_PRECEDENCE, bulbPrecidence);
          groupBulbValues.put(GroupBulbColumns.COL_NET_BULB_ID, netBulbId);
          db.insert(GroupBulbColumns.TABLE_NAME, null, groupBulbValues);
        }

        /* Now add the all group */
        String allName = mContext.getString(R.string.cap_all);
        ContentValues allValue = new ContentValues();
        allValue.put(GroupColumns.COL_GROUP_NAME, allName);
        allValue.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, allName.toLowerCase().trim());
        allValue.put(GroupColumns.COL_GROUP_PRIORITY, GroupColumns.PRIORITY_STARRED);
        allValue.put(GroupColumns.COL_GROUP_FLAGS, GroupColumns.FLAG_ALL);
        long allGroupId = db.insert(GroupColumns.TABLE_NAME, null, allValue);

        String[] netBulbColumns = {NetBulbColumns._ID};
        Cursor netBulbCursor =
            db.query(NetBulbColumns.TABLE_NAME, netBulbColumns, null, null, null, null, null);

        netBulbCursor.moveToFirst();
        for (int i = 0; i < netBulbCursor.getCount(); i++) {
          long netBulbId = netBulbCursor.getLong(0);
          netBulbCursor.moveToNext();

          ContentValues allBulbValues = new ContentValues();
          allBulbValues.put(GroupBulbColumns.COL_GROUP_ID, allGroupId);
          allBulbValues.put(GroupBulbColumns.COL_BULB_PRECEDENCE, i);
          allBulbValues.put(GroupBulbColumns.COL_NET_BULB_ID, netBulbId);

          db.insert(GroupBulbColumns.TABLE_NAME, null, allBulbValues);
        }
        netBulbCursor.close();


        /* load everything from old alarms table */

        ContentValues cv = new ContentValues();
        String[]
            oldAlarmColumns =
            {DeprecatedAlarmColumns.STATE,
             DeprecatedAlarmColumns.INTENT_REQUEST_CODE};
        Cursor
            oldAlarmsCursor =
            db.query(DeprecatedAlarmColumns.TABLE_NAME, oldAlarmColumns, null, null,
                     null, null, null);

        ArrayList<Pair<DeprecatedAlarmState, Long>>
            oldList = new ArrayList<Pair<DeprecatedAlarmState, Long>>();
        oldAlarmsCursor.moveToPosition(-1);
        while (oldAlarmsCursor.moveToNext()) {
          String jsonOldState = oldAlarmsCursor.getString(0);
          long oldIntentRequestCode = oldAlarmsCursor.getLong(1);
          DeprecatedAlarmState oldState = gson.fromJson(jsonOldState, DeprecatedAlarmState.class);

          oldList.add(new Pair<DeprecatedAlarmState, Long>(oldState, oldIntentRequestCode));
        }
        oldAlarmsCursor.close();

        /* restructure alarms table */

        db.execSQL("DROP TABLE IF EXISTS " + DeprecatedAlarmColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + AlarmColumns.TABLE_NAME + " (" +
                   BaseColumns._ID + " INTEGER PRIMARY KEY," +
                   AlarmColumns.COL_GROUP_ID + " INTEGER," +
                   AlarmColumns.COL_MOOD_ID + " INTEGER," +
                   AlarmColumns.COL_BRIGHTNESS + " INTEGER," +
                   AlarmColumns.COL_IS_ENABLED + " INTEGER," +
                   AlarmColumns.COL_REPEAT_DAYS + " INTEGER," +
                   AlarmColumns.COL_YEAR + " INTEGER," +
                   AlarmColumns.COL_MONTH + " INTEGER," +
                   AlarmColumns.COL_DAY_OF_MONTH + " INTEGER," +
                   AlarmColumns.COL_HOUR_OF_DAY + " INTEGER," +
                   AlarmColumns.COL_MINUTE + " INTEGER," +
                   " FOREIGN KEY (" + AlarmColumns.COL_MOOD_ID + ") REFERENCES " +
                   MoodColumns.TABLE_NAME + " (" + MoodColumns._ID + " ) ON DELETE CASCADE " +
                   ");");

        /* now save all the migrated data to the new table */

        for (Pair<DeprecatedAlarmState, Long> oldRow : oldList) {
          DeprecatedAlarmState oldState = oldRow.first;
          AlarmData alarm = new AlarmData();

          String[] groupCols = {GroupColumns._ID};
          String groupWhere = GroupColumns.COL_GROUP_NAME + " =?";
          String[] groupWhereArgs = {oldState.group};
          Cursor
              groupCursor =
              db.query(GroupColumns.TABLE_NAME, groupCols, groupWhere, groupWhereArgs, null, null,
                       null);
          if (groupCursor.moveToFirst()) {
            alarm.setGroup(groupCursor.getLong(0), oldState.group);
          } else {
            //this group doesn't actually exist, so this alarm must be discarded
            continue;
          }

          String[] moodCols = {MoodColumns._ID};
          String[] moodArgs = {oldState.mood};
          Cursor
              moodCursor =
              db.query(MoodColumns.TABLE_NAME, moodCols, MoodColumns.COL_MOOD_NAME + " =?",
                       moodArgs, null, null, null);
          if (moodCursor.moveToFirst()) {
            alarm.setMood(moodCursor.getLong(0), oldState.mood);
          } else {
            //this mood doesn't actually exist, so this alarm must be discarded
            continue;
          }

          alarm.setBrightness(oldState.brightness);

          alarm.setEnabled(oldState.isScheduled());

          if (oldState.isRepeating()) {
            DaysOfWeek days = new DaysOfWeek();
            for (int i = 0; i < 7; i++) {
              days.setDay(i + 1, oldState.getRepeatingDays()[i]);
            }
            alarm.setRepeatDays(days);
          }

          Long timeInMillis = null;
          if (oldState.isRepeating()) {
            for (Long daysTime : oldState.getRepeatingTimes()) {
              if (daysTime != null) {
                timeInMillis = daysTime;
              }
            }
          } else {
            timeInMillis = oldState.getRepeatingTimes()[0];
          }

          if (timeInMillis != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeInMillis);
            alarm.setAlarmTime(cal);
          } else {
            //this alarm time is invalid, so this alarm must be discarded
            continue;
          }

          db.insert(AlarmColumns.TABLE_NAME, null, alarm.getValues());
          moodCursor.close();
        }
        oldAlarmsCursor.close();
      }
    }
  }


  private static String getEncodedOn() {
    BulbState resultState = new BulbState();
    resultState.setOn(true);
    resultState.setEffect(BulbState.Effect.NONE);
    return HueUrlEncoder.encode(new Mood(resultState));
  }

  private static String getEncodedOff() {
    BulbState resultState = new BulbState();
    resultState.setOn(false);
    resultState.setEffect(BulbState.Effect.NONE);
    return HueUrlEncoder.encode(new Mood(resultState));
  }
}
