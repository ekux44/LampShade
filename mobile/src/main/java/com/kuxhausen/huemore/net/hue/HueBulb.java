package com.kuxhausen.huemore.net.hue;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.state.BulbState;

public class HueBulb implements NetworkBulb {

  public static final long SEND_TIMEOUT_TIME = 2000;
  public final static float BS_BRI_CONVERSION = 2.55f;

  private Long mBaseId;
  private String mName;
  /**
   * for now this is just bulbNumber *
   */
  private String mDeviceId;
  private HueBulbData mData;
  private Context mContext;
  private HubConnection mConnection;

  private BulbState desiredState = new BulbState();
  private boolean mInstantBrightnessRequested = false;

  //using SystemClock.elapsedTimes
  public Long lastSendInitiatedTime;
  public BulbState confirmed = new BulbState();

  private Integer mMaxBri;

  // TODO chance once a better Device Id implemented
  public String getHubBulbNumber() {
    return mDeviceId;
  }

  public HueBulb(Context c, Long bulbBaseId, String bulbName, String bulbDeviceId,
                 HueBulbData bulbData, HubConnection hubConnection) {
    mContext = c;
    mBaseId = bulbBaseId;
    mName = bulbName;
    mDeviceId = bulbDeviceId;
    mData = bulbData;
    mConnection = hubConnection;
  }

  @Override
  public void setState(BulbState bs, boolean broadcast) {
    BulbState preBriAdjusted = bs.clone();
    if (isMaxBriModeEnabled()) {
      if (preBriAdjusted.bri != null) {
        preBriAdjusted.bri = (int) (preBriAdjusted.bri * getMaxBrightness(true) / 100f);
      }
    }

    if (preBriAdjusted.hasOnlyBri()) {
      mInstantBrightnessRequested = true;
    }

    desiredState.merge(preBriAdjusted);

    mConnection.getLooper().addToQueue(this);

    if (broadcast) {
      this.mConnection.getDeviceManager().onStateChanged();
    }

    Log.i("setState", preBriAdjusted.toString());
  }

  @Override
  public Integer getMaxBrightness(boolean guessIfUnknown) {
    if (mMaxBri != null) {
      return Math.max(1,Math.min(100,mMaxBri));
    } else if (guessIfUnknown) {
      return 100;
    } else {
      return null;
    }
  }

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  @Override
  public Integer getCurrentBrightness(boolean guessIfUnknown) {
    Integer bri = null;
    if (desiredState.bri != null) {
      bri = desiredState.bri;
    } else if (confirmed.bri != null) {
      bri = confirmed.bri;
    } else if (guessIfUnknown) {
      bri = 127;
    }
    if (bri != null) {
      return (int) ((bri / 2.55f) * (100f / getMaxBrightness(true)));
    } else {
      return null;
    }
  }

  @Override
  public void setBrightness(Integer desiredMaxBrightness, Integer desiredCurrentBrightness) {
    Integer oldCurerntBri = this.getCurrentBrightness(false);
    Integer oldMaxBri = mMaxBri;

    boolean currentChanged = false;
    if (oldCurerntBri == null ^ desiredCurrentBrightness == null) {
      currentChanged = true;
    } else if (oldCurerntBri != null && desiredCurrentBrightness != null && !oldCurerntBri
        .equals(desiredCurrentBrightness)) {
      currentChanged = true;
    }

    boolean maxChanged = false;
    if (mMaxBri == null ^ desiredMaxBrightness == null) {
      maxChanged = true;
    } else if (mMaxBri != null && desiredMaxBrightness != null && !mMaxBri
        .equals(desiredMaxBrightness)) {
      maxChanged = true;
    }

    mMaxBri = desiredMaxBrightness;

    if(desiredMaxBrightness==null && maxChanged && desiredCurrentBrightness==null && currentChanged) {
      oldCurerntBri = (int)(oldCurerntBri * oldMaxBri/100f);
    } else if (desiredCurrentBrightness != null) {
      oldCurerntBri = desiredCurrentBrightness;
    }

    if (maxChanged || currentChanged) {
      if (oldCurerntBri != null) {
        BulbState change = new BulbState();
        change.bri = (int) (oldCurerntBri * 2.55f);
        change.transitiontime = 4;
        setState(change, true);
      }
    }
  }

  @Override
  public boolean isMaxBriModeEnabled() {
    return mMaxBri != null;
  }


  @Override
  public BulbState getState(boolean guess) {
    BulbState preBriAdjusted = desiredState.clone();
    if (isMaxBriModeEnabled()) {
      if (preBriAdjusted.bri != null) {
        preBriAdjusted.bri = (int) (preBriAdjusted.bri * 100f / getMaxBrightness(true));
      }
    }

    if (guess) {
      if (preBriAdjusted.bri == null) {
        preBriAdjusted.bri = 127;
      }

    }
    Log.i("net.hue.bulb.getState", preBriAdjusted.toString());
    return preBriAdjusted;
  }

  public void confirm(BulbState transmitted) {
    // remove successful changes from pending
    lastSendInitiatedTime = null;

    Log.d("net.hue.bulb.confirm", "unconfirmedDesired" + desiredState.toString());
    // recalculate any remaining desired state
    //desiredState = transmitted.delta(desiredState);

    // update confirmed
    BulbState.confirmChange(confirmed, transmitted);
    BulbState.confirmChange(desiredState, transmitted);
    //desiredState = confirmed.delta(desiredState);

    Log.d("net.hue.bulb.confirm", "confirmedDesired" + desiredState.toString());

  }

  public void attributesReturned(BulbAttributes attributes) {
    //these may be stale by up to 4 seconds, but lets set them to the confirmed for now
    //TODO better handle
    //for now only touch brightness since that's atleast user visible
    confirmed.bri = attributes.state.bri;
    if (desiredState.bri == null) {
      desiredState.bri = attributes.state.bri;

      //notify brightness bar
      this.mConnection.getDeviceManager().onStateChanged();

      Log.d("net.hue.bulb.attribute", "onAttributeReturned" + desiredState.bri);
    }
  }

  public boolean hasOngoingTransmission() {
    return (lastSendInitiatedTime != null) && (SystemClock.elapsedRealtime() - lastSendInitiatedTime
                                               < SEND_TIMEOUT_TIME);
  }

  public boolean hasPendingTransmission() {
    return (getSendState() != null && !getSendState().isEmpty());
  }

  /**
   * returns desiredState
   */
  public BulbState getSendState() {
    BulbState toSend = confirmed.delta(desiredState);
    if (mInstantBrightnessRequested) {
      BulbState brightnessOnly = new BulbState();
      brightnessOnly.bri = toSend.bri;
      brightnessOnly.transitiontime = 4;
      mInstantBrightnessRequested = false;
      return brightnessOnly;
    } else {
      return toSend;
    }
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public void rename(String name) {
    BulbAttributes bAttrs = new BulbAttributes();
    bAttrs.name = name;

    for (Route route : mConnection.getBestRoutes()) {
      NetworkMethods.PreformSetBulbAttributes(route, mConnection.mData.hashedUsername, mContext,
                                              mConnection.getRequestQueue(), mConnection,
                                              Integer.parseInt(mDeviceId), bAttrs);
    }
  }

  @Override
  public Long getBaseId() {
    return mBaseId;
  }

  @Override
  public ConnectivityState getConnectivityState() {
    return mConnection.getConnectivityState();
  }

}
