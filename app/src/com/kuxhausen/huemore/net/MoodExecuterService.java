package com.kuxhausen.huemore.net;

import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.util.Log;
import android.util.Pair;

import com.kuxhausen.huemore.DecodeErrorActivity;
import com.kuxhausen.huemore.MainFragment;
import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.drawable;
import com.kuxhausen.huemore.R.string;
import com.kuxhausen.huemore.automation.FireReceiver;
import com.kuxhausen.huemore.net.DeviceManager.OnStateChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.timing.AlarmReciever;

public class MoodExecuterService extends Service implements OnActiveMoodsChangedListener,
    OnStateChangedListener {

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
    public MoodExecuterService getService() {
      // Return this instance of LocalService so clients can call public methods
      return MoodExecuterService.this;
    }
  }

  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();
  private final static int notificationId = 1337;

  private boolean mBound;
  private WakeLock mWakelock;
  private DeviceManager mDeviceManager;
  private MoodPlayer mMoodPlayer;

  @Override
  public void onCreate() {
    super.onCreate();

    // acquire wakelock needed till everything initialized
    PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
    mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
    mWakelock.acquire();

    // Initialize DeviceManager and Mood Player
    mDeviceManager = new DeviceManager(this);
    mDeviceManager.registerStateListener(this);
    mMoodPlayer = new MoodPlayer(this, mDeviceManager);
    mMoodPlayer.addOnActiveMoodsChangedListener(this);

  }

  @Override
  /** 
   * Called after onCreate when service attaching to Activity(s)
   */
  public IBinder onBind(Intent intent) {
    mBound = true;
    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    super.onUnbind(intent);
    mBound = false;
    return true; // ensures onRebind is called
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);
    mBound = true;
  }

  @Override
  /** 
   * Called after onCreate when service (re)started independently
   */
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      // remove any possible launched wakelocks
      AlarmReciever.completeWakefulIntent(intent);
      FireReceiver.completeWakefulIntent(intent);


      String encodedMood = intent.getStringExtra(InternalArguments.ENCODED_MOOD);
      String groupName = intent.getStringExtra(InternalArguments.GROUP_NAME);
      String moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
      Integer maxBri = intent.getIntExtra(InternalArguments.MAX_BRIGHTNESS, -1);
      if (maxBri == -1)
        maxBri = null;

      if (encodedMood != null) {
        try {
          Pair<Integer[], Pair<Mood, Integer>> moodPairs = HueUrlEncoder.decode(encodedMood);

          if (moodPairs.second.first != null) {
            Group g = Group.loadFromLegacyData(moodPairs.first, groupName, this);


            moodName = (moodName == null) ? "Unknown Mood" : moodName;
            mMoodPlayer
                .playMood(g, moodPairs.second.first, moodName, moodPairs.second.second, null);
          }
        } catch (InvalidEncodingException e) {
          Intent i = new Intent(this, DecodeErrorActivity.class);
          i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, false);
          startActivity(i);
        } catch (FutureEncodingException e) {
          Intent i = new Intent(this, DecodeErrorActivity.class);
          i.putExtra(InternalArguments.DECODER_ERROR_UPGRADE, true);
          startActivity(i);
        }
      } else if (moodName != null && groupName != null) {
        Group g = Group.loadFromDatabase(groupName, this);
        Mood m = Utils.getMoodFromDatabase(moodName, this);

        mMoodPlayer.playMood(g, m, moodName, maxBri, null);
      } else if (intent.hasExtra(InternalArguments.FLAG_AWAKEN_PLAYING_MOODS)
          && intent.getExtras().getBoolean(InternalArguments.FLAG_AWAKEN_PLAYING_MOODS)) {
        mMoodPlayer.restoreFromSaved();
      }
    }
    calculateWakeNeeds();
    return super.onStartCommand(intent, flags, startId);
  }

  /**
   * don't call till after onCreate and near the end of onStartCommand so device doesn't sleep
   * before launching mood events queued
   */
  public void calculateWakeNeeds() {
    boolean shouldStayAwake = false;

    if (mMoodPlayer.hasImminentPendingWork())
      shouldStayAwake = true;
    Log.e("na", "MoodPlayer StayAwake=" + shouldStayAwake);

    for (Connection c : mDeviceManager.getConnections()) {
      if (c.hasPendingWork())
        shouldStayAwake = true;
    }
    Log.e("na", "MP & DM StayAwake=" + shouldStayAwake);

    if (shouldStayAwake) {
      if (mWakelock == null) {
        // acquire wakelock till done doing work
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakelock.acquire();
      }
    } else {
      if (!mBound) {
        // not bound, so service may sleep after releasing wakelock
        // save ongoing moods and schedule a broadcast to restart service before next playing mood
        // event
        mMoodPlayer.saveOngoingAndScheduleResores();
      }

      if (mWakelock != null) {
        mWakelock.release();
        mWakelock = null;
      }

      if (!mBound && mMoodPlayer.getPlayingMoods().isEmpty()) {

        // with no ongoing moods and not bound, go ahead and completely shut down
        // this.stopSelf();
        // doesn't work because it gets triggered before a service finishes binding and can't be
        // undone.
      }
    }
  }

  public MoodPlayer getMoodPlayer() {
    return mMoodPlayer;
  }

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }


  @Override
  public void onStateChanged() {
    Log.e("state", "heard onStateChanged");
    calculateWakeNeeds();
  }

  public void onActiveMoodsChanged() {
    if (mMoodPlayer.getPlayingMoods().isEmpty()) {
      this.stopForeground(true);
    } else {
      // Creates an explicit intent for an Activity in your app
      Intent resultIntent = new Intent(this, NavigationDrawerActivity.class);
      PendingIntent resultPendingIntent =
          PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      // create basic compatibility notification
      NotificationCompat.Builder mBuilder =
          new NotificationCompat.Builder(this).setSmallIcon(R.drawable.lampshade_notification)
              .setContentTitle(this.getResources().getString(R.string.app_name))
              .setContentText(mMoodPlayer.getPlayingMoods().get(0).toString())
              .setContentIntent(resultPendingIntent);

      // now create rich notification for supported devices
      List<PlayingMood> playing = mMoodPlayer.getPlayingMoods();
      InboxStyle iStyle = new NotificationCompat.InboxStyle();
      for (int i = 0; (i < 5 && i < playing.size()); i++) {
        iStyle.addLine(playing.get(i).toString());
      }
      if (playing.size() > 5) {
        iStyle.setSummaryText("+" + (playing.size() - 5) + " "
            + this.getResources().getString(R.string.notification_overflow_more));
      }
      mBuilder.setStyle(iStyle);


      this.startForeground(notificationId, mBuilder.build());

    }

    calculateWakeNeeds();
  }


  @Override
  public void onDestroy() {
    mMoodPlayer.onDestroy();
    mDeviceManager.onDestroy();
    if (mWakelock != null)
      mWakelock.release();
    super.onDestroy();
  }
}
