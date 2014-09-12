package com.kuxhausen.huemore.net;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.R;

import alt.android.os.CountDownTimer;

public class LifecycleController {

  /**
   * How long in milis before the next event the ExecutorService should begin waking back up
   */
  public static final long MILIS_AWAKEN_STARTUP_TIME = 150l;
  public static final long TICKS_PER_SECOND = 10l;
  public static final long EMPTY_CONSECUTIVE_TICKS_TILL_SLEEP = 25;
  public static final long
      MINIMUM_NAP_MILLISECONDS =
      2 * ((EMPTY_CONSECUTIVE_TICKS_TILL_SLEEP * TICKS_PER_SECOND) + MILIS_AWAKEN_STARTUP_TIME);

  private Context mContext;
  private OnActiveMoodsChangedListener mMoodsListener;

  private LifecycleState mLifecycleState;
  private PowerManager.WakeLock mWakeLock;
  private DeviceManager mDeviceManager;
  private MoodPlayer mMoodPlayer;
  private InternalClock mInternalClock;

  public LifecycleController(Context c, OnActiveMoodsChangedListener moodsListener) {
    mContext = c;
    mMoodsListener = moodsListener;
    mLifecycleState = LifecycleState.NAPPING;
  }

  public synchronized DeviceManager getDeviceManager() {
    if (mLifecycleState == LifecycleState.BOUND_TO_UI
        || mLifecycleState == LifecycleState.WORKING) {
      return mDeviceManager;
    } else {
      throw new IllegalStateException();
    }
  }

  public synchronized MoodPlayer getMoodPlayer() {
    if (mLifecycleState == LifecycleState.BOUND_TO_UI
        || mLifecycleState == LifecycleState.WORKING) {
      return mMoodPlayer;
    } else {
      throw new IllegalStateException();
    }
  }

  public synchronized LifecycleState getLifecycleState() {
    return mLifecycleState;
  }

  public synchronized void onStartNapping() {
    //Log.i("lifecycle", "onStartNapping");

    if (mLifecycleState != LifecycleState.DEAD) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.NAPPING;
  }

  public synchronized void onStartWorking() {
    //Log.i("lifecycle", "onStartWorking");

    if (mLifecycleState != LifecycleState.NAPPING) {
      throw new IllegalStateException();
    }

    PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    mWakeLock =
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, mContext.getString(R.string.app_name));
    mWakeLock.acquire();

    mDeviceManager = new DeviceManager(mContext);

    mMoodPlayer = new MoodPlayer(mContext, mDeviceManager);
    mMoodPlayer.addOnActiveMoodsChangedListener(mMoodsListener);

    mInternalClock = new InternalClock();
    mInternalClock.start();

    mLifecycleState = LifecycleState.WORKING;

    mMoodsListener.onActiveMoodsChanged();
  }

  public synchronized void onStartBound() {
    //Log.i("lifecycle", "onStartBound");

    if (mLifecycleState != LifecycleState.WORKING) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.BOUND_TO_UI;
  }

  public synchronized void onStopBound() {
    //Log.i("lifecycle", "onStopBound");

    if (mLifecycleState != LifecycleState.BOUND_TO_UI) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.WORKING;
  }

  public synchronized void onStopWorking() {
    //Log.i("lifecycle", "onStopWorking");

    if (mLifecycleState != LifecycleState.WORKING) {
      throw new IllegalStateException();
    }

    mInternalClock.cancel();
    mInternalClock = null;

    mMoodPlayer.onDestroy();
    mMoodPlayer = null;

    mDeviceManager.onDestroy();
    mDeviceManager = null;

    mLifecycleState = LifecycleState.NAPPING;

    mWakeLock.release();
    mWakeLock = null;
  }

  public synchronized void onStopNappng() {
    //Log.i("lifecycle", "onStopNapping");

    mLifecycleState = LifecycleState.DEAD;
  }

  public enum LifecycleState {
    DEAD,
    NAPPING,
    WORKING,
    BOUND_TO_UI
  }

  public class InternalClock extends CountDownTimer {

    long ticksTillSleep = LifecycleController.EMPTY_CONSECUTIVE_TICKS_TILL_SLEEP;

    public InternalClock() {
      super(Long.MAX_VALUE, (1000l / TICKS_PER_SECOND));
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    @Override
    public void onTick(long millisUntilFinished) {
      synchronized (LifecycleController.this) {
        //every tenth of a second, pump mood player (which will in turn pump playing moods)
        mMoodPlayer.tick();

        //Log.i("wtf", "tick");

        //also check device manager & mood player to see if can sleep
        if (mMoodPlayer.nextEventTime() == null || mMoodPlayer.nextEventTime() > (
            SystemClock.elapsedRealtime() + LifecycleController.MINIMUM_NAP_MILLISECONDS)) {
          ticksTillSleep--;

          //Log.i("wtf", "NextEventTime null or > (elapsedRealtime+MINIMUM_NAP_MILLIS)");

          if (ticksTillSleep < 0) {

            //Log.i("wtf","(ticksTillSleep < LifecycleController.EMPTY_CONSECUTIVE_TICKS_TILL_SLEEP");

            if (LifecycleController.this.getLifecycleState() == LifecycleState.WORKING) {
              //Log.i("wtf", "currently Working");
              LifecycleController.this.onStopWorking();
            }
          }
        } else {
          ticksTillSleep = LifecycleController.EMPTY_CONSECUTIVE_TICKS_TILL_SLEEP;
        }
      }
    }

    /**
     * Callback fired when the time is up.
     */
    @Override
    public void onFinish() {
    }
  }
}
