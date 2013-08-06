package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodsListFragment.OnMoodSelectedListener;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.GetBulbsAttributes.OnAttributeListReturnedListener;
import com.kuxhausen.huemore.network.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public abstract class GodObject extends NetworkManagedSherlockFragmentActivity implements OnMoodSelectedListener, OnAttributeListReturnedListener{

	public Gson gson = new Gson();
	private ArrayList<AsyncTask<?, ?, ?>> inFlight = new ArrayList<AsyncTask<?, ?, ?>>();
	private String groupS;	
	private Integer[] bulbS;
	private String mood;
	
	private CountDownTimer countDownTimer;
	private boolean hasChanged = false;
	private Mood previewStates;
	
	
	public void restartCountDownTimer(){
		if(countDownTimer!=null)
			countDownTimer.cancel();
		
		int numBulbs = 1;
		if(getBulbs()!=null)
			numBulbs = getBulbs().length;
		
		Log.e("asdf", "count down timer interval rate = "+50*numBulbs);
		//runs at the rate to execute 20 op/sec
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE,
				50*(numBulbs)) {

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
		super.onStart();
		restartCountDownTimer();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		countDownTimer.cancel();

	}
	
	public void updatePreview(Mood mood){
		previewStates = mood;
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
		restartCountDownTimer();
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

	/**
	 * test mood by applying to json states array to previously selected moods
	 * 
	 * @param states
	 */
	public void testMood(Mood m) {
		this.getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, m);
	}

	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;
		
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { mood };
		Cursor moodCursor = getContentResolver().query(
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

		Mood m = HueUrlEncoder.decode(moodCursor.getString(0)).second;
		
		this.getRequestQueue().cancelAll(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		NetworkMethods.PreformTransmitGroupMood(getRequestQueue(), this, bulbS, m);
		
		// TODO clean up after development
		/*
		Intent intent = new Intent(this, MoodExecuter.class);
        startService(intent);
		*/
		
	}
	
}
