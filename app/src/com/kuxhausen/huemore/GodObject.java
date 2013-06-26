package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kuxhausen.huemore.MoodsListFragment.OnMoodSelectedListener;
import com.kuxhausen.huemore.network.GetBulbList;

public abstract class GodObject extends SherlockFragmentActivity implements OnMoodSelectedListener{

	public abstract void onBrightnessChanged(String brightnessState[]);
	
	public abstract Integer[] getBulbs();
	
	public abstract ArrayList<AsyncTask<?, ?, ?>> getInFlight();
	
	public abstract void onGroupBulbSelected(Integer[] bulb, String name);
	
	public abstract void setBulbListenerFragment(GetBulbList.OnBulbListReturnedListener frag);
	
	public abstract GetBulbList.OnBulbListReturnedListener getBulbListenerFragment();
	
	public abstract void testMood(String[] states);
}
