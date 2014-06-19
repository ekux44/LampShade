package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.List;
import alt.android.os.CountDownTimer;
import android.content.Context;
import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

public class MoodPlayer{

	private final static int MOODS_TIMES_PER_SECOND = 10;
	
	private DeviceManager mDeviceManager;
	private ArrayList<OnActiveMoodsChangedListener> moodsChangedListeners = new ArrayList<OnActiveMoodsChangedListener>();
	private ArrayList<PlayingMood> mPlayingMoods = new ArrayList<PlayingMood>();
	private static CountDownTimer countDownTimer;
	
	public MoodPlayer(Context c, DeviceManager m){	
		
		mDeviceManager = m;
		
		restartCountDownTimer();
	}
	
	public void playMood(Group g, Mood m, String mName, Integer maxBri){
		PlayingMood pm = new PlayingMood(this, mDeviceManager, g, m, mName, maxBri);
		
		pm.initialMaxBri = maxBri;
		
		for(int i = 0; i< mPlayingMoods.size(); i++){
			if(mPlayingMoods.get(i).getGroup().conflictsWith(pm.getGroup())){
				// remove mood at i to unschedule
				mPlayingMoods.remove(i);
				i--;
			}
		}
		
		mPlayingMoods.add(pm);
		
		
		//update notifications
		onActiveMoodsChanged();
	}
	public void cancelMood(Group g){
		for(int i = 0; i< mPlayingMoods.size(); i++){
			if(mPlayingMoods.get(i).getGroup().equals(g)){
				//TODO remove mood at i to unschedule
				mPlayingMoods.remove(i);
				i--;
			}
		}
		
		//update notifications
		onActiveMoodsChanged();
	}
	
	public void addOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l){
		moodsChangedListeners.add(l);
	}
	public void removeOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l){
		moodsChangedListeners.remove(l);
	}
	public void onActiveMoodsChanged(){
		for(OnActiveMoodsChangedListener l : moodsChangedListeners)
			l.onActiveMoodsChanged();
	}
	
	public void onDestroy() {
		if (countDownTimer != null)
			countDownTimer.cancel();
	}

	public void restartCountDownTimer() {
		if (countDownTimer != null)
			countDownTimer.cancel();

		// runs at the rate to execute 10 times per second
		countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / MOODS_TIMES_PER_SECOND)) {

			@Override
			public void onFinish() {
			}

			@Override
			public void onTick(long millisUntilFinished) {
				for(PlayingMood pm : mPlayingMoods)
					pm.onTick();
			}
		};
		countDownTimer.start();
	}

	public boolean hasImminentPendingWork() {
		for(PlayingMood pm : mPlayingMoods)
			if(pm.hasImminentPendingWork())
				return true;
		return false;
	}

	/**
	 * to save power, service is shutting down so ongoing moods should be schedule to be restarted in time for their next events
	 */
	public void saveOngoingAndScheduleResores() {
		// TODO Auto-generated method stub
	}
	
	public List<PlayingMood> getPlayingMoods(){
		return mPlayingMoods;
	}
}
