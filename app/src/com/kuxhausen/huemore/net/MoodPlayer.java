package com.kuxhausen.huemore.net;

import java.util.ArrayList;
import java.util.List;

import alt.android.os.CountDownTimer;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;

public class MoodPlayer {

  /**
   * How long in milis before the next event the ExecutorService should begin waking back up
   */
  private final static long MILIS_AWAKEN_STARTUP_TIME = 50;

  private final static int MOODS_TIMES_PER_SECOND = 10;

  private Gson gson = new Gson();
  private Context mContext;
  private DeviceManager mDeviceManager;
  private ArrayList<OnActiveMoodsChangedListener> moodsChangedListeners =
      new ArrayList<OnActiveMoodsChangedListener>();
  private ArrayList<PlayingMood> mPlayingMoods = new ArrayList<PlayingMood>();
  private static CountDownTimer countDownTimer;

  public MoodPlayer(Context c, DeviceManager m) {
    mContext = c;
    mDeviceManager = m;

    restartCountDownTimer();
  }

  public void playMood(Group g, Mood m, String mName, Integer maxBri, Long miliTimeStarted) {
    PlayingMood pm = new PlayingMood(this, mDeviceManager, g, m, mName, maxBri, miliTimeStarted);

    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (mPlayingMoods.get(i).getGroup().conflictsWith(pm.getGroup())) {
        // remove mood at i to unschedule
        mPlayingMoods.remove(i);
        i--;
      }
    }

    mPlayingMoods.add(pm);


    // update notifications
    onActiveMoodsChanged();
  }

  public void cancelMood(Group g) {
    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (mPlayingMoods.get(i).getGroup().equals(g)) {
        // TODO remove mood at i to unschedule
        mPlayingMoods.remove(i);
        i--;
      }
    }

    // update notifications
    onActiveMoodsChanged();
  }

  public void addOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l) {
    moodsChangedListeners.add(l);
  }

  public void removeOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l) {
    moodsChangedListeners.remove(l);
  }

  public void onActiveMoodsChanged() {
    for (OnActiveMoodsChangedListener l : moodsChangedListeners)
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
      public void onFinish() {}

      @Override
      public void onTick(long millisUntilFinished) {
        for (int i = 0; i < mPlayingMoods.size(); i++) {
          boolean ongoing = mPlayingMoods.get(i).onTick();
          if (!ongoing) {
            mPlayingMoods.remove(i);
            i--;

            // update notifications
            onActiveMoodsChanged();
          }
        }
      }
    };
    countDownTimer.start();
  }

  public boolean hasImminentPendingWork() {
    for (PlayingMood pm : mPlayingMoods)
      if (pm.hasImminentPendingWork())
        return true;
    return false;
  }

  /**
   * to save power, service is shutting down so ongoing moods should be schedule to be restarted in
   * time for their next events
   */
  public void saveOngoingAndScheduleResores() {
    Log.e("nap", "saveAndSchedule");
    // calculated from SystemClock.elapsedRealtime
    long awakenTime = Long.MAX_VALUE;
    for (PlayingMood pm : mPlayingMoods) {
      long nextEventTime = pm.getNextEventTime();
      if (nextEventTime > awakenTime)
        awakenTime = nextEventTime;
    }

    awakenTime -= MILIS_AWAKEN_STARTUP_TIME;

    mContext.getContentResolver().delete(DatabaseDefinitions.PlayingMood.URI, null, null);
    for (PlayingMood pm : mPlayingMoods) {
      ContentValues cv = new ContentValues();
      cv.put(DatabaseDefinitions.PlayingMood.COL_GROUP_VALUE, gson.toJson(pm.getGroup()));
      cv.put(DatabaseDefinitions.PlayingMood.COL_MOOD_NAME, pm.getMoodName());
      cv.put(DatabaseDefinitions.PlayingMood.COL_MOOD_VALUE, HueUrlEncoder.encode(pm.getMood()));
      cv.put(DatabaseDefinitions.PlayingMood.COL_INITIAL_MAX_BRI, pm.getGroupName());
      cv.put(DatabaseDefinitions.PlayingMood.COL_MILI_TIME_STARTED, SystemClock.elapsedRealtime());
      mContext.getContentResolver().insert(DatabaseDefinitions.PlayingMood.URI, cv);
    }

    AlarmReciever.scheduleInternalAlarm(mContext, awakenTime);
  }

  public void restoreFromSaved() {
    Log.e("nap", "restoreFromSaved");
    String[] projectionColumns =
        {DatabaseDefinitions.PlayingMood.COL_GROUP_VALUE,
            DatabaseDefinitions.PlayingMood.COL_MOOD_NAME,
            DatabaseDefinitions.PlayingMood.COL_MOOD_VALUE,
            DatabaseDefinitions.PlayingMood.COL_INITIAL_MAX_BRI,
            DatabaseDefinitions.PlayingMood.COL_MILI_TIME_STARTED};
    Cursor cursor =
        mContext.getContentResolver().query(DatabaseDefinitions.PlayingMood.URI, projectionColumns,
            null, null, null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      Group g = gson.fromJson(cursor.getString(0), Group.class);
      String mName = cursor.getString(1);
      Mood m = null;
      try {
        m = HueUrlEncoder.decode(cursor.getString(2)).second.first;
      } catch (InvalidEncodingException e) {
      } catch (FutureEncodingException e) {
      }
      Integer initialMaxB = cursor.getInt(3);
      Long miliTimeStarted = cursor.getLong(4);

      this.playMood(g, m, mName, initialMaxB, miliTimeStarted);
    }
  }


  public List<PlayingMood> getPlayingMoods() {
    return mPlayingMoods;
  }
}
