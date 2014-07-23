package com.kuxhausen.huemore.persistence;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Pair;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.hue.HueBulbData;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayingMood;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "huemore.db";
  private static final int DATABASE_VERSION = 8;
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

    db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " (" + BaseColumns._ID
               + " INTEGER PRIMARY KEY," + GroupColumns.GROUP + " TEXT," + GroupColumns.PRECEDENCE
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
            db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
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
            Event e = new Event();
            e.state = gson.fromJson(stateJson.get(i), BulbState.class);
            e.time = 0;
            e.channel = i;
            events[i] = e;
          }
          Mood m = new Mood();
          m.usesTiming = false;
          m.timeAddressingRepeatPolicy = false;
          m.setNumChannels(stateJson.size());
          m.events = events;

          cv.put(MoodColumns.COL_MOOD_NAME, key);
          cv.put(MoodColumns.COL_MOOD_VALUE, HueUrlEncoder.encode(m));
          db.insert(MoodColumns.TABLE_NAME, null, cv);
        }
      }

      case 2: {
        db.execSQL("DROP TABLE IF EXISTS " + AlarmColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + AlarmColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + AlarmColumns.STATE + " TEXT,"
                   + AlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");

        // remove the sunset mood
        String[] moodArgs = {"Sunset"};
        db.delete(MoodColumns.TABLE_NAME, MoodColumns.COL_MOOD_NAME + " =?", moodArgs);
      }
      case 4: {
        ContentValues cv = new ContentValues();
        String[] moodColumns = {MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_VALUE};
        Cursor moodCursor =
            db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
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
            {GroupColumns._ID, GroupColumns.GROUP, GroupColumns.PRECEDENCE, "Dbulb"};
        Cursor oldGroupCursor =
            db.query(DatabaseDefinitions.GroupColumns.TABLE_NAME, oldGroupColumns, null, null,
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
        db.execSQL("DROP TABLE IF EXISTS " + GroupColumns.TABLE_NAME);

        db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + GroupColumns.GROUP + " TEXT,"
                   + GroupColumns.PRECEDENCE
                   + " INTEGER," + GroupColumns.BULB_DATABASE_ID + " INTEGER," + " FOREIGN KEY ("
                   + GroupColumns.BULB_DATABASE_ID + ") REFERENCES " + NetBulbColumns.TABLE_NAME
                   + " ("
                   + NetBulbColumns._ID + " ) ON DELETE CASCADE " + ");");


        /* now add the groups to the new table */
        for (String groupName : oldGroupMap.keySet()) {
          for (Pair<Integer, Integer> oldPair : oldGroupMap.get(groupName)) {
            int bulbPrecidence = oldPair.first;
            long bulbBaseId = hubIdToBaseIdMapping.get(oldPair.second);

            ContentValues groupValues = new ContentValues();
            groupValues.put(GroupColumns.GROUP, groupName);
            groupValues.put(GroupColumns.PRECEDENCE, bulbPrecidence);
            groupValues.put(GroupColumns.BULB_DATABASE_ID, bulbBaseId);
            db.insert(GroupColumns.TABLE_NAME, null, groupValues);

          }
        }

        db.execSQL("CREATE TABLE " + PlayingMood.TABLE_NAME + " (" + BaseColumns._ID
                   + " INTEGER PRIMARY KEY," + PlayingMood.COL_GROUP_VALUE + " TEXT,"
                   + PlayingMood.COL_MOOD_NAME + " TEXT," + PlayingMood.COL_MOOD_VALUE + " TEXT,"
                   + PlayingMood.COL_INITIAL_MAX_BRI + " INTEGER,"
                   + PlayingMood.COL_MILI_TIME_STARTED
                   + " INTEGER" + ");");

      }
      case 7: {
        //TODO clean previous migrations or create non-upgrade path for first run performance
        ContentValues cv = new ContentValues();
        String[] moodColumns = {MoodColumns.COL_MOOD_NAME, MoodColumns.COL_MOOD_VALUE};
        Cursor moodCursor =
            db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null,
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
      }
    }
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
}
