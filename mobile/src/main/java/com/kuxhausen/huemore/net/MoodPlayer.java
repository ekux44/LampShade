package com.kuxhausen.huemore.net;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;

import java.util.ArrayList;
import java.util.List;

import alt.android.os.CountDownTimer;

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
  private CountDownTimer countDownTimer;

  public MoodPlayer(Context c, DeviceManager m) {
    mContext = c;
    mDeviceManager = m;
  }

  public boolean conflictsWithOngoingPlaying(Group g) {
    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (!mPlayingMoods.get(i).getMood().isSimple()
          && mPlayingMoods.get(i).getGroup().conflictsWith(g)) {
        return true;
      }
    }
    return false;
  }

  public void playMood(Group g, Mood m, String mName, Integer maxBri, Long miliTimeStarted) {
    assert g!=null;

    PlayingMood pm = new PlayingMood(this, mDeviceManager, g, m, mName, maxBri, miliTimeStarted);

    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (mPlayingMoods.get(i).getGroup().conflictsWith(pm.getGroup())) {
        //unschedule any conflicting moods
        mPlayingMoods.remove(i);
        i--;
      }
    }

    BrightnessManager briManager = mDeviceManager.obtainBrightnessManager(g);
    briManager.setPolicy(BrightnessManager.BrightnessPolicy.VOLUME_BRI);
    if (maxBri != null) {
      briManager.setVolumeWithoutUpdate(maxBri);
    }


    mPlayingMoods.add(pm);
    ensureLooping();

    // update notifications
    onActiveMoodsChanged();
    mDeviceManager.onStateChanged();
  }

  public void cancelMood(Group g) {
    assert g!=null;

    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (mPlayingMoods.get(i).getGroup().equals(g)) {
        //unschedule
        mPlayingMoods.remove(i);
        i--;
      }
    }

    mDeviceManager.obtainBrightnessManager(g)
        .setPolicy(BrightnessManager.BrightnessPolicy.DIRECT_BRI);

    // update notifications
    onActiveMoodsChanged();
    mDeviceManager.onStateChanged();
  }

  public void addOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l) {
    moodsChangedListeners.add(l);
  }

  public void removeOnActiveMoodsChangedListener(OnActiveMoodsChangedListener l) {
    moodsChangedListeners.remove(l);
  }

  public void onActiveMoodsChanged() {
    for (OnActiveMoodsChangedListener l : moodsChangedListeners) {
      l.onActiveMoodsChanged();
    }
  }

  public void onDestroy() {
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
  }

  public void ensureLooping() {
    // runs at the rate to execute 10 times per second
    if (countDownTimer == null) {
      countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / MOODS_TIMES_PER_SECOND)) {

        @Override
        public void onFinish() {
        }

        @Override
        public void onTick(long millisUntilFinished) {
          boolean activeMoodsChanged = false;
          for (int i = 0; i < mPlayingMoods.size(); i++) {
            boolean ongoing = mPlayingMoods.get(i).onTick();
            if (!ongoing) {
              PlayingMood pm = mPlayingMoods.get(i);

              mPlayingMoods.remove(i);
              i--;

              mDeviceManager.obtainBrightnessManager(pm.getGroup())
                  .setPolicy(BrightnessManager.BrightnessPolicy.DIRECT_BRI);
              // update notifications
              activeMoodsChanged = true;
            }
          }
          if (activeMoodsChanged) {
            onActiveMoodsChanged();
          }
          if (mPlayingMoods.isEmpty()) {
            countDownTimer = null;
            this.cancel();
          }
        }
      };
      countDownTimer.start();
    }
  }

  public boolean hasImminentPendingWork() {
    for (PlayingMood pm : mPlayingMoods) {
      if (pm.hasImminentPendingWork()) {
        return true;
      }
    }
    return false;
  }

  /**
   * to save power, service is shutting down so ongoing moods should be schedule to be restarted in
   * time for their next events
   */
  public void saveOngoingAndScheduleResores() {
    // calculated from SystemClock.elapsedRealtime
    long awakenTime = Long.MAX_VALUE;
    for (PlayingMood pm : mPlayingMoods) {
      long nextEventTime = pm.getNextEventTime();
      if (nextEventTime < awakenTime) {
        awakenTime = nextEventTime;
      }
    }

    awakenTime -= MILIS_AWAKEN_STARTUP_TIME;

    mContext.getContentResolver().delete(Definitions.PlayingMood.URI, null, null);
    for (PlayingMood pm : mPlayingMoods) {
      ContentValues cv = new ContentValues();
      cv.put(Definitions.PlayingMood.COL_GROUP_VALUE, gson.toJson(pm.getGroup()));
      cv.put(Definitions.PlayingMood.COL_MOOD_NAME, pm.getMoodName());
      cv.put(Definitions.PlayingMood.COL_MOOD_VALUE, HueUrlEncoder.encode(pm.getMood()));
      cv.put(Definitions.PlayingMood.COL_INITIAL_MAX_BRI, pm.getGroupName());
      cv.put(Definitions.PlayingMood.COL_MILI_TIME_STARTED, SystemClock.elapsedRealtime());
      mContext.getContentResolver().insert(Definitions.PlayingMood.URI, cv);
    }

    Log.d("mood", "awaken future millis offset " + (awakenTime - SystemClock.elapsedRealtime()));

    AlarmReciever.scheduleInternalAlarm(mContext, awakenTime);
  }

  public void restoreFromSaved() {
    String[] projectionColumns =
        {Definitions.PlayingMood.COL_GROUP_VALUE,
         Definitions.PlayingMood.COL_MOOD_NAME,
         Definitions.PlayingMood.COL_MOOD_VALUE,
         Definitions.PlayingMood.COL_INITIAL_MAX_BRI,
         Definitions.PlayingMood.COL_MILI_TIME_STARTED};
    Cursor cursor =
        mContext.getContentResolver().query(Definitions.PlayingMood.URI, projectionColumns,
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

      Log.d("mood", "restore at" + SystemClock.elapsedRealtime() + " from " + miliTimeStarted);

      this.playMood(g, m, mName, initialMaxB, miliTimeStarted);
    }
  }


  public List<PlayingMood> getPlayingMoods() {
    return mPlayingMoods;
  }
}
