package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodsListFragment.OnMoodSelectedListener;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbState;

public abstract class GodObject extends NetworkManagedSherlockFragmentActivity implements OnMoodSelectedListener, OnAttributeListReturnedListener{

	public Gson gson = new Gson();
	private ArrayList<AsyncTask<?, ?, ?>> inFlight = new ArrayList<AsyncTask<?, ?, ?>>();
	private String groupS;	
	private Integer[] bulbS;
	private String mood;
	
	private CountDownTimer countDownTimer;
	private boolean hasChanged = false;
	private String[] previewStates;
	
	
	public void restartCountDownTimer(){
		if(countDownTimer!=null)
			countDownTimer.cancel();
		
		int numBulbs = 1;
		if(getBulbs()!=null)
			numBulbs = getBulbs().length;
		
		//runs at the rate to execute 15 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE,
				66*(numBulbs)) {
			private boolean warned = false;

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onTick(long millisUntilFinished) {
				if(hasChanged){
					testMood(previewStates);
					hasChanged = false;	
				}
			}

			@Override
			public void onFinish() {
				// try one last time
				if(hasChanged){
					testMood(previewStates);
					hasChanged = false;
				}
			}
		};
		countDownTimer.start();

	}
	@Override
	public void onStart(){
		super.onResume();
		restartCountDownTimer();
	}
	
	@Override
	public void onStop() {
		super.onPause();
		countDownTimer.cancel();

	}
	
	public void updatePreview(String[] states){
		previewStates = states;
		hasChanged = true;
	}
	
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBrightnessChanged(String brightnessState[]) {
		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, brightnessState);
	}

	/**
	 * test mood by applying to json states array to previously selected moods
	 * 
	 * @param states
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void testMood(String[] states) {
		getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, states);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, moodS);
	}
	
}
