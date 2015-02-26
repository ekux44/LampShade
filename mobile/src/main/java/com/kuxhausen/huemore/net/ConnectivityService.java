package com.kuxhausen.huemore.net;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.util.Pair;

import com.kuxhausen.huemore.DecodeErrorActivity;
import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.OnActiveMoodsChangedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.automation.FireReceiver;
import com.kuxhausen.huemore.automation.VoiceInputReceiver;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.DatabaseGroup;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.NfcGroup;
import com.kuxhausen.huemore.voice.SpeechParser;

import java.util.List;

public class ConnectivityService extends Service implements OnActiveMoodsChangedListener {

  /**
   * Class used for the client Binder. Because we know this service always runs in the same process
   * as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {

    public ConnectivityService getService() {
      // Return this instance of LocalService so clients can call public methods
      return ConnectivityService.this;
    }
  }

  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();
  private final static int notificationId = 1337;

  private LifecycleController mLifecycleController;

  @Override
  public void onCreate() {
    super.onCreate();

    mLifecycleController = new LifecycleController(this, this);
  }

  @Override
  /**
   * Called after onCreate when service attaching to Activity(s)
   */
  public IBinder onBind(Intent intent) {
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.DEAD) {
      mLifecycleController.onStartNapping();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.NAPPING) {
      mLifecycleController.onStartWorking();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.WORKING) {
      mLifecycleController.onStartBound();
    }

    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    super.onUnbind(intent);

    if (mLifecycleController.getLifecycleState()
        == LifecycleController.LifecycleState.BOUND_TO_UI) {
      mLifecycleController.onStopBound();
    }

    return true; // ensures onRebind is called
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);

    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.DEAD) {
      mLifecycleController.onStartNapping();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.NAPPING) {
      mLifecycleController.onStartWorking();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.WORKING) {
      mLifecycleController.onStartBound();
    }
  }

  @Override
  /**
   * Called after onCreate when service (re)started independently
   */
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {

      if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.DEAD) {
        mLifecycleController.onStartNapping();
      }
      if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.NAPPING) {
        mLifecycleController.onStartWorking();
      }

      // remove any possible launched wakelocks
      ConnectivityServiceLauncher.completeWakefulIntent(intent);
      FireReceiver.completeWakefulIntent(intent);
      VoiceInputReceiver.completeWakefulIntent(intent);

      String encodedMood = null;
      String groupName = null;
      String moodName = null;
      Integer maxBri = null;

      Bundle extras = intent.getExtras();
      if (extras != null) {
        if (extras.getBoolean(InternalArguments.FLAG_CANCEL_PLAYING, false)) {
          mLifecycleController.getMoodPlayer().cancelAllMoods();
        }

        if (extras.containsKey(InternalArguments.VOICE_INPUT) || extras.containsKey(
            InternalArguments.VOICE_INPUT_LIST)) {
          String best = extras.getString(InternalArguments.VOICE_INPUT);
          List<String> candidates = extras.getStringArrayList(InternalArguments.VOICE_INPUT_LIST);
          float[]
              confidences =
              extras.getFloatArray(InternalArguments.VOICE_INPUT_CONFIDENCE_ARRAY);

          GroupMoodBrightness parsed = SpeechParser.parse(this, best, candidates, confidences);
          groupName = parsed.group;
          moodName = parsed.mood;
          maxBri = parsed.brightness;
        }

        if (extras.containsKey(InternalArguments.ENCODED_MOOD)) {
          encodedMood = intent.getStringExtra(InternalArguments.ENCODED_MOOD);
        }
        if (extras.containsKey(InternalArguments.GROUP_NAME)) {
          groupName = intent.getStringExtra(InternalArguments.GROUP_NAME);
        }
        if (extras.containsKey(InternalArguments.MOOD_NAME)) {
          moodName = intent.getStringExtra(InternalArguments.MOOD_NAME);
        }
        if (extras.containsKey(InternalArguments.MAX_BRIGHTNESS)) {
          maxBri = intent.getIntExtra(InternalArguments.MAX_BRIGHTNESS, 0);
        }
      }

      if (encodedMood != null) {
        try {
          Pair<Integer[], Pair<Mood, Integer>> moodPairs = HueUrlEncoder.decode(encodedMood);

          if (moodPairs.second.first != null) {
            Group g = new NfcGroup(moodPairs.first, groupName, this);

            moodName = (moodName == null) ? "Unknown Mood" : moodName;

            getMoodPlayer().playMood(g, moodPairs.second.first, moodName, moodPairs.second.second);
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
        Group g = new DatabaseGroup(groupName, this);
        Mood m = Utils.getMoodFromDatabase(moodName, this);

        getMoodPlayer().playMood(g, m, moodName, maxBri);
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  public MoodPlayer getMoodPlayer() {
    return mLifecycleController.getMoodPlayer();
  }

  public DeviceManager getDeviceManager() {
    return mLifecycleController.getDeviceManager();
  }

  public void onActiveMoodsChanged() {

    if (getMoodPlayer().getPlayingMoods().isEmpty()) {
      this.stopForeground(true);
    } else {
      // Creates an explicit intent for an Activity in your app
      Intent openI = new Intent(this, NavigationDrawerActivity.class);
      openI.putExtra(InternalArguments.FLAG_SHOW_NAV_DRAWER, true);
      PendingIntent
          openPI = PendingIntent.getActivity(this, 0, openI, PendingIntent.FLAG_UPDATE_CURRENT);

      // create basic compatibility notification
      NotificationCompat.Builder mBuilder =
          new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_notification_whiteshade)
              .setContentTitle(this.getResources().getString(R.string.app_name))
              .setContentText(getMoodPlayer().getPlayingMoods().get(0).toString())
              .setContentIntent(openPI);

      // now create rich notification for supported devices
      List<PlayingMood> playing = getMoodPlayer().getPlayingMoods();
      InboxStyle iStyle = new NotificationCompat.InboxStyle();
      for (int i = 0; (i < 5 && i < playing.size()); i++) {
        iStyle.addLine(playing.get(i).toString());
      }
      if (playing.size() > 5) {
        iStyle.setSummaryText("+" + (playing.size() - 5) + " "
                              + this.getResources().getString(R.string.notification_overflow_more));
      }
      mBuilder.setStyle(iStyle);

      //now add cancel button for supported devices
      Intent stopI = new Intent(this, ConnectivityService.class);
      stopI.putExtra(InternalArguments.FLAG_CANCEL_PLAYING, true);
      PendingIntent
          stopPI = PendingIntent.getService(this, 0, stopI, PendingIntent.FLAG_UPDATE_CURRENT);

      String
          message =
          (playing.size() == 1) ? getResources().getString(R.string.action_stop_all)
                                : getResources().getString(R.string.action_stop);
      mBuilder.addAction(R.drawable.ic_action_discard, message, stopPI);

      this.startForeground(notificationId, mBuilder.build());
    }
  }


  @Override
  public void onDestroy() {
    if (mLifecycleController.getLifecycleState()
        == LifecycleController.LifecycleState.BOUND_TO_UI) {
      mLifecycleController.onStopBound();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.WORKING) {
      mLifecycleController.onStopWorking();
    }
    if (mLifecycleController.getLifecycleState() == LifecycleController.LifecycleState.NAPPING) {
      mLifecycleController.onStopNappng();
    }

    super.onDestroy();
  }


}
