package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodsListFragment.OnMoodSelectedListener;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbState;

public abstract class GodObject extends SherlockFragmentActivity implements OnMoodSelectedListener, OnAttributeListReturnedListener{

	public Gson gson = new Gson();
	private ArrayList<AsyncTask<?, ?, ?>> inFlight = new ArrayList<AsyncTask<?, ?, ?>>();
	private String groupS;	
	private Integer[] bulbS;
	private String mood;
	
	class SerializedGodObjectForTransport{
		public SerializedGodObjectForTransport(){
		}
		public String group;
		public Integer[] bulb;
		public String moo;
	}
	
	public String getSerialized(){
		SerializedGodObjectForTransport sgoft = new SerializedGodObjectForTransport();
		sgoft.group = groupS;
		sgoft.bulb = bulbS;
		sgoft.moo = mood;
		return gson.toJson(sgoft);
	}
	public void restoreSerialized(String serializedForTransport){
		SerializedGodObjectForTransport sgoft = gson.fromJson(serializedForTransport, SerializedGodObjectForTransport.class);
		groupS = sgoft.group;
		bulbS = sgoft.bulb;
		mood = sgoft.moo;
	}
	
	public void setBulbS(Integer[] newBulbS){
		bulbS = newBulbS;
	}
	public void setGroupS(String newGroupS){
		groupS = newGroupS;
	}
	public String getGroupS(){
		return groupS;
	}
	
	
	public ArrayList<AsyncTask<?, ?, ?>> getInFlight(){
		return inFlight;
	}
	
	@Override
	public void onDestroy() {
		for(int i = 0; i< inFlight.size(); i++){
			inFlight.get(i).cancel(true);
			inFlight.remove(i);
		}
		super.onDestroy();
	}
	
	
	public abstract void onGroupBulbSelected(Integer[] bulb, String name);
	
	public abstract void setBulbListenerFragment(GetBulbList.OnBulbListReturnedListener frag);
	
	public abstract GetBulbList.OnBulbListReturnedListener getBulbListenerFragment();
	
	public abstract void onSelected(Integer[] bulbNum, String name,
			GroupsListFragment groups, BulbsFragment bulbs);
	
	public Integer[] getBulbs(){
		return bulbS;
	}
	
	@Override
	public void onMoodSelected(String moodParam) {
		mood = moodParam;
		pushMoodGroup();
	}

	public void onBrightnessChanged(String brightnessState[]) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				brightnessState, this);
		pushGroupMood.execute();
	}

	/**
	 * test mood by applying to json states array to previously selected moods
	 * 
	 * @param states
	 */
	public void testMood(String[] states) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				states, this);
		pushGroupMood.execute();
	}

	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;
		String[] moodS = null;
		if (mood.equals(PreferencesKeys.RANDOM)) {
			BulbState randomState = new BulbState();
			randomState.on = true;
			randomState.hue = (int) (65535 * Math.random());
			randomState.sat = (short) (255 * (Math.random() * 5. + .25));
			moodS = new String[1];
			moodS[0] = gson.toJson(randomState);
		} else {
			String[] moodColumns = { MoodColumns.STATE };
			String[] mWereClause = { mood };
			Cursor cursor = getContentResolver().query(
					DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use the
																	// default
																	// content
																	// URI
																	// for the
																	// provider.
					moodColumns, // Return the note ID and title for each note.
					MoodColumns.MOOD + "=?", // selection clause
					mWereClause, // election clause args
					null // Use the default sort order.
					);

			ArrayList<String> moodStates = new ArrayList<String>();
			while (cursor.moveToNext()) {
				moodStates.add(cursor.getString(0));
			}
			moodS = moodStates.toArray(new String[moodStates.size()]);
		}

		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				moodS, this);
		pushGroupMood.execute();
	}
	
}
