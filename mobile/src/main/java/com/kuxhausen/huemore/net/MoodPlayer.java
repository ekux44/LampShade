package com.kuxhausen.huemore.net;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Pair;

import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;
import com.kuxhausen.huemore.timing.Conversions;

import java.util.ArrayList;
import java.util.List;

import alt.android.os.CountDownTimer;

public class MoodPlayer {

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

    restoreNappingMoods();
  }

  public void playMood(Group g, Mood m, String mName, Integer maxBri) {
    playMood(g, m, mName, maxBri, SystemClock.elapsedRealtime(), null);
  }

  public synchronized void playMood(Group g, Mood m, String mName, Integer maxBri,
                                    Long savedStartTime,
                                    Long savedProgress) {
    if (g == null) {
      throw new IllegalArgumentException();
    }

    PlayingMood
        pm =
        new PlayingMood(m, mName, g, savedStartTime, Conversions.getDayStartElapsedRealTimeMillis(),
                        savedProgress);

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

    // update notifications
    onActiveMoodsChanged();
    mDeviceManager.onStateChanged();
  }

  public synchronized void cancelMood(Group g) {
    if (g == null) {
      throw new IllegalArgumentException();
    }

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

  public synchronized void cancelAllMoods() {
    for (int i = mPlayingMoods.size() - 1; i >= 0; i--) {
      //unschedule
      mPlayingMoods.remove(i);
    }
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
    if (nextEventTime() != null) {
      saveNappingMoods();
    }
    if (countDownTimer != null) {
      countDownTimer.cancel();
    }
  }

  public synchronized void tick() {
    for (PlayingMood pm : getPlayingMoods()) {

      List<Pair<List<Long>, BulbState>> toDo = pm.tick(SystemClock.elapsedRealtime());
      for (Pair<List<Long>, BulbState> eachBatch : toDo) {
        BulbState toSend = eachBatch.second;
        for (Long id : eachBatch.first) {
          mDeviceManager.obtainBrightnessManager(pm.getGroup())
              .setState(mDeviceManager.getNetworkBulb(id), toSend);
        }
      }
    }

    boolean activeMoodsChanged = false;
    for (int i = 0; i < mPlayingMoods.size(); i++) {
      if (!mPlayingMoods.get(i).hasFutureEvents()) {
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
  }

  /**
   * @return next event time in milliseconds measured in SystemClock.getRealtime. Will return null
   * if no future events
   */
  public synchronized Long nextEventTime() {
    Long result = Long.MAX_VALUE;

    for (PlayingMood pm : getPlayingMoods()) {
      if (pm.hasFutureEvents()) {
        if (pm.getNextEventInCurrentMillis() < result) {
          result = pm.getNextEventInCurrentMillis();
        }
      }
    }

    if (result == Long.MAX_VALUE) {
      return null;
    } else {
      return result;
    }
  }

  /**
   * to save power, service is shutting down so ongoing moods should be schedule to be restarted in
   * time for their next events
   */
  private synchronized void saveNappingMoods() {
    long awakenTime = nextEventTime();
    awakenTime -= LifecycleController.MILIS_AWAKEN_STARTUP_TIME;

    mContext.getContentResolver().delete(Definitions.PlayingMood.URI, null, null);

    for (PlayingMood pm : mPlayingMoods) {
      ContentValues cv = new ContentValues();
      cv.put(Definitions.PlayingMood.COL_GROUP_VALUE, gson.toJson(pm.getGroup()));
      cv.put(Definitions.PlayingMood.COL_MOOD_NAME, pm.getMoodName());
      cv.put(Definitions.PlayingMood.COL_MOOD_VALUE, HueUrlEncoder.encode(pm.getMood()));
      cv.put(Definitions.PlayingMood.COL_MOOD_BRI,
             mDeviceManager.obtainBrightnessManager(pm.getGroup()).getBrightness());
      cv.put(Definitions.PlayingMood.COL_MILI_TIME_STARTED, pm.getStartTime());
      cv.put(Definitions.PlayingMood.COL_INTERNAL_PROGRESS, pm.getInternalProgress());
      mContext.getContentResolver().insert(Definitions.PlayingMood.URI, cv);
    }

    AlarmReciever.scheduleInternalAlarm(mContext, awakenTime);
  }

  private synchronized void restoreNappingMoods() {
    String[] projectionColumns =
        {Definitions.PlayingMood.COL_GROUP_VALUE,
         Definitions.PlayingMood.COL_MOOD_NAME,
         Definitions.PlayingMood.COL_MOOD_VALUE,
         Definitions.PlayingMood.COL_MOOD_BRI,
         Definitions.PlayingMood.COL_MILI_TIME_STARTED,
         Definitions.PlayingMood.COL_INTERNAL_PROGRESS
        };
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
      Integer moodBri = cursor.getInt(3);
      Long miliTimeStarted = cursor.getLong(4);
      Long colInternalProgress = cursor.getLong(5);

      this.playMood(g, m, mName, moodBri, miliTimeStarted, colInternalProgress);
    }
    cursor.close();

    mContext.getContentResolver().delete(Definitions.PlayingMood.URI, null, null);
  }


  public synchronized List<PlayingMood> getPlayingMoods() {
    return mPlayingMoods;
  }
}
