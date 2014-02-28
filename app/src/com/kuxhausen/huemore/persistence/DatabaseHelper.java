package com.kuxhausen.huemore.persistence;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "huemore.db";
	private static final int DATABASE_VERSION = 5;
	Gson gson = new Gson();

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
				+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");

		db.execSQL("CREATE TABLE " + GroupColumns.TABLE_NAME + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
				+ GroupColumns.GROUP + " TEXT," + GroupColumns.PRECEDENCE
				+ " INTEGER," + GroupColumns.BULB + " INTEGER" + ");");
		
		this.onUpgrade(db, 1, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion){
			case 1:
			{
				ContentValues cv = new ContentValues();
				
				/** update 2.4/2.5/switch to serialized b64 **/
				
				String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
				Cursor cursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
				
				HashMap<String,ArrayList<String>> moodStateMap = new HashMap<String,ArrayList<String>>();
				
				while (cursor.moveToNext()) {
					String mood = cursor.getString(0);
					String state = cursor.getString(1);
					if(mood!=null && state!=null && !mood.equals("") && !state.equals("") && !state.equals("{}")){
						ArrayList<String> states;
						if(moodStateMap.containsKey(mood))
							states = moodStateMap.get(mood);
						else
							states =  new ArrayList<String>();
						states.add(state);
						moodStateMap.put(mood, states);
					}
				}
				db.execSQL("DROP TABLE " + MoodColumns.TABLE_NAME);
				
				db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
						+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
						+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");
				
				String[] gSelectionArgs = { "ALL", ((char) 8) + "ALL" };
				db.delete(GroupColumns.TABLE_NAME,
						DatabaseDefinitions.GroupColumns.GROUP + "=? or "
								+ DatabaseDefinitions.GroupColumns.GROUP + "=?",
						gSelectionArgs);
		
				
				//remove standard moods that are no longer correct
				String[] moodsToRemove = {"OFF", "Reading", "Relax", "Concentrate",
						"Energize", "Red", "Orange", "Blue", "Romantic",
						"Rainbow", ((char) 8) + "OFF", ((char) 8) + "ON", ((char) 8) + "RANDOM"};
				
				for(String removeKey : moodsToRemove){
					moodStateMap.remove(removeKey);
				}
				
				
				String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea", "Deep Sea", "Deep Sea", "Fruit", "Fruit", "Fruit"};
				int[] simpleSat = {144, 211 ,49, 232, 253, 230, 253, 244, 254, 173};
				int[] simpleHue = {15331, 13122, 33863, 34495, 45489, 1111, 45489, 15483, 25593, 64684};
				
				for(int i = 0; i< simpleNames.length; i++){
					BulbState hs = new BulbState();
					hs.sat=(short)simpleSat[i];
					hs.hue=simpleHue[i];
					hs.on=true;
					hs.effect="none";
				
					ArrayList<String> states;
					if(moodStateMap.containsKey(simpleNames[i]))
						states = moodStateMap.get(simpleNames[i]);
					else
						states =  new ArrayList<String>();
					states.add(gson.toJson(hs));
					moodStateMap.put(simpleNames[i], states);
				}
				
				for(String key : moodStateMap.keySet()){
					
					ArrayList<String> stateJson = moodStateMap.get(key);
					//bug fix in case there are any empty bulbstates in the old system
					for(int i = 0; i<stateJson.size(); i++){
						if(stateJson.get(i)==null || gson.fromJson(stateJson.get(i),BulbState.class)==null)
							stateJson.remove(i);
					}
					
					Event[] events = new Event[stateJson.size()];
					for(int i = 0; i< stateJson.size(); i++){
						Event e = new Event();
						e.state = gson.fromJson(stateJson.get(i), BulbState.class);
						e.time=0;
						e.channel=i;
						events[i]=e;
					}
					Mood m = new Mood();
					m.usesTiming=false;
					m.timeAddressingRepeatPolicy=false;
					m.setNumChannels(stateJson.size());
					m.events = events;
					
					cv.put(MoodColumns.MOOD, key);
					cv.put(MoodColumns.STATE, HueUrlEncoder.encode(m));
					db.insert(MoodColumns.TABLE_NAME, null, cv);
				}
			}
				
			case 2:
			{
				ContentValues cv = new ContentValues();
				String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
				Cursor cursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
				
				HashMap<String,Mood> moodMap = new HashMap<String,Mood>();
				
				while (cursor.moveToNext()) {
					try {
						String name = cursor.getString(0);
						Mood mood = HueUrlEncoder.decode(cursor.getString(1)).second.first;
						moodMap.put(name, mood);
					} catch (InvalidEncodingException e){
					} catch (FutureEncodingException e) {
					}
				}
				moodMap.remove("Fruity");
				moodMap.remove("Sunset");
				
				db.execSQL("DROP TABLE IF EXISTS " + MoodColumns.TABLE_NAME);
				
				db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
						+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
						+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");
				
				for(String key : moodMap.keySet()){
					cv.put(MoodColumns.MOOD, key);
					cv.put(MoodColumns.STATE, HueUrlEncoder.encode(moodMap.get(key)));
					db.insert(MoodColumns.TABLE_NAME, null, cv);
				}
				
				//Construct animated fruit mood
				{
					BulbState bs1 = new BulbState();
					bs1.on = true;
					bs1.transitiontime = 50;
					bs1.sat = 244;
					bs1.hue = 15483;
					
					BulbState bs2 = new BulbState();
					bs2.on = true;
					bs2.transitiontime = 50;
					bs2.sat = 254;
					bs2.hue= 25593;
					
					BulbState bs3 = new BulbState();
					bs3.on = true;
					bs3.transitiontime = 50;
					bs3.sat = 173;
					bs3.hue= 64684;
					
					Event e1 = new Event(bs1, 0, 0);
					Event e2 = new Event(bs2, 1, 0);
					Event e3 = new Event(bs3, 2, 0);
					Event e4 = new Event(bs2, 0, 100);
					Event e5 = new Event(bs3, 1, 100);
					Event e6 = new Event(bs1, 2, 100);
					Event e7 = new Event(bs3, 0, 200);
					Event e8 = new Event(bs1, 1, 200);
					Event e9 = new Event(bs2, 2, 200);
					Event[] events = {e1,e2,e3,e4,e5,e6,e7,e8,e9};
					
					Mood m = new Mood();
					m.usesTiming = true;
					m.timeAddressingRepeatPolicy = false;
					m.setNumChannels(3);
					m.setInfiniteLooping(true);
					m.events = events;
					m.loopIterationTimeLength = 300;
					
					cv.put(MoodColumns.MOOD, "Fruity");
					cv.put(MoodColumns.STATE, HueUrlEncoder.encode(m));
					db.insert(MoodColumns.TABLE_NAME, null, cv);
				}
				
				db.execSQL("DROP TABLE IF EXISTS " + AlarmColumns.TABLE_NAME);
				
				db.execSQL("CREATE TABLE IF NOT EXISTS " + AlarmColumns.TABLE_NAME + " ("
						+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
						+ AlarmColumns.STATE + " TEXT,"
						+ AlarmColumns.INTENT_REQUEST_CODE + " INTEGER" + ");");
			}
			case 3:
			{
				//remove any nameless moods
				ContentValues cv = new ContentValues();
				String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
				Cursor moodCursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
				
				HashMap<String,Mood> moodMap = new HashMap<String,Mood>();
				
				while (moodCursor.moveToNext()) {
					try {
						String name = moodCursor.getString(0);
						Mood mood = HueUrlEncoder.decode(moodCursor.getString(1)).second.first;
						moodMap.put(name, mood);
					} catch (InvalidEncodingException e){
					} catch (FutureEncodingException e) {
					}
				}
				moodMap.remove("");
				moodMap.remove(null);
				
				db.execSQL("DROP TABLE IF EXISTS " + MoodColumns.TABLE_NAME);
				
				db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
						+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
						+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");
				
				for(String key : moodMap.keySet()){
					try{
						cv.put(MoodColumns.MOOD, key);
						cv.put(MoodColumns.STATE, HueUrlEncoder.encode(moodMap.get(key)));
						db.insert(MoodColumns.TABLE_NAME, null, cv);
					} catch (Exception e) {
					}
				}
				
				//remove any nameless groups
				
				String[] gSelectionArgs = { ""};
				db.delete(GroupColumns.TABLE_NAME,
						DatabaseDefinitions.GroupColumns.GROUP + "=?",
						gSelectionArgs);
				
			}
			case 4:
			{
				String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea1", "Deep Sea2", "Fruit1", "Fruit2", "Fruit3"};
				int[] simpleSat = {144, 211, 49, 232, 253, 230, 244, 254, 173};
				int[] simpleHue = {15331, 13122, 33863, 34495, 45489, 1111, 15483, 25593, 64684};
				float[] simpleX = {0.4571f, 0.5119f, 0.368f, 0.3151f, 0.1859f, 0.6367f, 0.5089f, 0.5651f, 0.4081f};
				float[] simpleY = {0.4123f, 0.4147f, 0.3686f, 0.3252f, 0.0771f, 0.3349f, 0.438f, 0.3306f, 0.518f};
				
				SparseArray<BulbState> conversionMap = new SparseArray<BulbState>();
				
				for(int i = 0; i< simpleHue.length; i++){
					BulbState conversion = new BulbState();
					conversion.sat = (short) simpleSat[i];
					conversion.hue = simpleHue[i];
					Float[] conversionXY = {simpleX[i], simpleY[i]};
					conversion.xy = conversionXY;
					conversionMap.put(conversion.hue,conversion);
				}
				
				
				ContentValues cv = new ContentValues();
				String[] moodColumns = {MoodColumns.MOOD, MoodColumns.STATE};
				Cursor moodCursor = db.query(DatabaseDefinitions.MoodColumns.TABLE_NAME, moodColumns, null, null, null, null, null);
				
				HashMap<String,Mood> moodMap = new HashMap<String,Mood>();
				
				while (moodCursor.moveToNext()) {
					try {
						String name = moodCursor.getString(0);
						Mood mood = HueUrlEncoder.decode(moodCursor.getString(1)).second.first;
						
						for(Event e : mood.events){
							if(e.state.hue!=null && e.state.sat!=null && conversionMap.get(e.state.hue)!=null && conversionMap.get(e.state.hue).sat.equals(e.state.sat)){
								BulbState conversion = conversionMap.get(e.state.hue);
								e.state.hue = null;
								e.state.sat = null;
								e.state.xy = conversion.xy;
							}
						}
						
						moodMap.put(name, mood);
					} catch (InvalidEncodingException e){
					} catch (FutureEncodingException e) {
					}
				}
				try {
					if(!moodMap.containsKey("Gentle Sunrise"))
						moodMap.put("Gentle Sunrise", HueUrlEncoder.decode("AQSAAQAAgDQApAGAJzfkJ8o85KtGLQMAk8j5riCB-ZYxfgDAZPIyfiB9bL5VtUAAMAFgwCSAQwA=").second.first);
					if(!moodMap.containsKey("Gentle Sunset"))
						moodMap.put("Gentle Sunset", HueUrlEncoder.decode("AQSAAQAAgDQApAGAI-cHhj7kW1GOBwCTyd34iaDH-GrSiQHAJDAAMAFgQBWAQwA=").second.first);
					if(!moodMap.containsKey("Living Night"))
						moodMap.put("Living Night", HueUrlEncoder.decode("AfKHAAAAAEwAaGJWfu4rZb4IfDsAk4m_-TkqEvniQEQATAAEFBAVACYA").second.first);
					if(!moodMap.containsKey("f.lux"))
						moodMap.put("f.lux", HueUrlEncoder.decode("AQxA5RmHN7_yNEQDWOqnAoAj5-ux8ufr6SQBAJDI-YGhD_lWlOMBACRyvitIYL5ljB8AAAFQFGIoEQAAAA==").second.first);
				} catch (InvalidEncodingException e) {
				} catch (FutureEncodingException e) {
				}
				
				db.execSQL("DROP TABLE IF EXISTS " + MoodColumns.TABLE_NAME);
				
				db.execSQL("CREATE TABLE " + MoodColumns.TABLE_NAME + " ("
						+ BaseColumns._ID + " INTEGER PRIMARY KEY," + MoodColumns.MOOD
						+ " TEXT," + MoodColumns.STATE + " TEXT" + ");");
				
				for(String key : moodMap.keySet()){
					cv.put(MoodColumns.MOOD, key);
					cv.put(MoodColumns.STATE, HueUrlEncoder.encode(moodMap.get(key)));
					db.insert(MoodColumns.TABLE_NAME, null, cv);
				}
				
			}
		}
	}
}
