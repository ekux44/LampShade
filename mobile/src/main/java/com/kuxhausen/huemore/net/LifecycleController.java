package com.kuxhausen.huemore.net;

import android.content.Context;
import android.os.PowerManager;

import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.R;

import alt.android.os.CountDownTimer;

public class LifecycleController {

  public static final long TICKS_PER_SECOND = 10l;

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
    if (mLifecycleState != LifecycleState.DEAD) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.NAPPING;
  }

  public synchronized void onStartWorking() {
    synchronized (this) {
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
    }
    mMoodsListener.onActiveMoodsChanged();
  }

  public synchronized void onStartBound() {
    if (mLifecycleState != LifecycleState.WORKING) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.BOUND_TO_UI;
  }

  public synchronized void onStopBound() {
    if (mLifecycleState != LifecycleState.BOUND_TO_UI) {
      throw new IllegalStateException();
    }

    mLifecycleState = LifecycleState.WORKING;
  }

  public synchronized void onStopWorking() {
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
    mLifecycleState = LifecycleState.DEAD;
  }

  public enum LifecycleState {
    DEAD,
    NAPPING,
    WORKING,
    BOUND_TO_UI
  }

  public class InternalClock extends CountDownTimer {

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
      //every tenth of a second, pump mood player (which will in turn pump playing moods)
      mMoodPlayer.tick();

      //also check device manager & mood player to see if can sleep

    }

    /**
     * Callback fired when the time is up.
     */
    @Override
    public void onFinish() {

    }
  }
}
