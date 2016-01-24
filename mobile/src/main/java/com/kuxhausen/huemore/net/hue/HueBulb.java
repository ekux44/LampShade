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
  public void setState(BulbState bs) {
    this.mConnection.updateDesiredLastChanged();

    desiredState.merge(bs);

    mConnection.getLooper().queueSendState(this);

    //TODO move or limit to actual state changes
    this.mConnection.getDeviceManager().onStateChanged();

    Log.i("setState", bs.toString());
  }

  @Override
  public BulbState getState(GetStateConfidence confidence) {
    BulbState result = new BulbState();
    switch (confidence) {
      case GUESS:
        BulbState guess = new BulbState();
        guess.setPercentBri(50);
        guess.setOn(true);
        guess.setAlert(BulbState.Alert.NONE);
        guess.setEffect(BulbState.Effect.NONE);
        guess.setMiredCT(300);
        guess.setTransitionTime(BulbState.TRANSITION_TIME_DEFAULT);
        result = BulbState.merge(guess, result);
      case KNOWN:
        result = BulbState.merge(confirmed, result);
      case DESIRED:
        result = BulbState.merge(desiredState, result);
    }
    return result;
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
    if (attributes == null || attributes.state == null) {
      return;
    }

    //these may be stale by up to 4 seconds, but lets set them to the confirmed for now
    //TODO better handle
    //for now only touch brightness since that's atleast user visible
    confirmed.set255Bri(attributes.state.get255Bri());
    if (desiredState.get255Bri() == null) {
      desiredState.set255Bri(attributes.state.get255Bri());

      //notify brightness bar
      this.mConnection.getDeviceManager().onStateChanged();

      Log.d("net.hue.bulb.attribute", "onAttributeReturned with bri:" + desiredState.get255Bri());
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
    return toSend;
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
      NetworkMethods.setBulbAttributes(route, mConnection.mData.hashedUsername, mContext,
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
