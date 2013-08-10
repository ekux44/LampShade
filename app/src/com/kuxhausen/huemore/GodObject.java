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
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public abstract class GodObject extends NetworkManagedSherlockFragmentActivity implements OnMoodSelectedListener, OnAttributeListReturnedListener{

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

	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;
		
		Mood m = Utils.getMoodFromDatabase(mood, this);
		
		Utils.transmit(this, InternalArguments.ENCODED_MOOD, m, bulbS);
	}
	
}
