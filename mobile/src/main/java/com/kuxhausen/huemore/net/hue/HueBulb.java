package com.kuxhausen.huemore.net.hue;

import android.content.ContentValues;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.state.BulbState;

public class HueBulb implements NetworkBulb {

  public static final long SEND_TIMEOUT_TIME = 2000;
  public final static float BS_BRI_CONVERSION = 2.55f;

  private Long mBaseId;
  private String mName;
  /** for now this is just bulbNumber **/
  private String mDeviceId;
  private HueBulbData mData;
  private Context mContext;
  private HubConnection mConnection;
  private int mMaxBri;
  private boolean mMaxBriMode;

  private BulbState desiredState = new BulbState();

  //using SystemClock.elapsedTimes
  public Long lastSendInitiatedTime;
  public BulbState confirmed = new BulbState();

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
    BulbState preBriAdjusted = bs.clone();
    if (preBriAdjusted.bri != null){
      setCurrentBrightness((int)(preBriAdjusted.bri/BS_BRI_CONVERSION));
      preBriAdjusted.bri = null;
    }
    desiredState.merge(preBriAdjusted);

    mConnection.getLooper().addToQueue(this);
  }

  @Override
  public BulbState getState() {
    return confirmed;
  }

  public void confirm(BulbState transmitted) {
    // remove successful changes from pending
    lastSendInitiatedTime = null;

    Log.d("confirm", "pre" + desiredState.toString());
    // recalculate any remaining desired state
    desiredState = transmitted.delta(desiredState);
    Log.d("confirm", "post" + desiredState.toString());


    // update confirmed
    BulbState.confirmChange(confirmed, transmitted);

    desiredState = confirmed.delta(desiredState);
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
    return desiredState;
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public void rename(String name) {
    BulbAttributes bAttrs = new BulbAttributes();
    bAttrs.name = name;

    for (Route route : mConnection.getBestRoutes())
      NetworkMethods.PreformSetBulbAttributes(route, mConnection.mData.hashedUsername, mContext,
          mConnection.getRequestQueue(), mConnection, Integer.parseInt(mDeviceId), bAttrs);
  }

  @Override
  public Long getBaseId() {
    return mBaseId;
  }

  @Override
  public ConnectivityState getConnectivityState() {
    return mConnection.getConnectivityState();
  }

  @Override
  public int getMaxBrightness() {
    return Math.max(1, Math.min(100, mMaxBri));
  }

  @Override
  public int getCurrentBrightness() {
    if(desiredState.bri!=null){
      int physicalBri = (int)(desiredState.bri / BS_BRI_CONVERSION);
      if(mMaxBriMode){
        return (int)(physicalBri / (getMaxBrightness()/100f));
      } else{
        return physicalBri;
      }
    } else{
      return 50;
    }
  }

  @Override
  public void setMaxBrightness(int newMaxBri) {
    newMaxBri = Math.max(1, Math.min(100, newMaxBri));

    if(desiredState.bri!=null) {
      //if there is an existing current brightness, recalculate it
      int currentBri = getCurrentBrightness();
      mMaxBri = newMaxBri;
      setCurrentBrightness(currentBri);
    } else {
      mMaxBri = newMaxBri;
    }
  }

  public void setCurrentBrightness(int newPercentBri){
    newPercentBri = Math.max(1, Math.min(100, newPercentBri));

    int desiredBulbStateBri;
    if(mMaxBriMode){
      desiredBulbStateBri = (int)((newPercentBri*BS_BRI_CONVERSION)*(getMaxBrightness()/100f));
    } else {
      desiredBulbStateBri = (int)(newPercentBri*BS_BRI_CONVERSION);
    }

    if(desiredState.bri == null || desiredState.bri!=desiredBulbStateBri){
      desiredState.bri = desiredBulbStateBri;
      mConnection.getLooper().addToQueue(this);
    }
  }

  public boolean isMaxBriModeEnabled(){
    return mMaxBriMode;
  }

  public void enableMaxBriMode(boolean enabled){
    mMaxBriMode = true;
  }

}
