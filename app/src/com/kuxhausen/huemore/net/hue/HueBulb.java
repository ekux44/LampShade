package com.kuxhausen.huemore.net.hue;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.state.BulbState;

public class HueBulb implements NetworkBulb {

  private Long mBaseId;
  private String mName;
  /** for now this is just bulbNumber **/
  private String mDeviceId;
  private HueBulbData mData;
  private Context mContext;
  private HubConnection mConnection;
  private int mCurrentMaxBri;

  private BulbState desiredState = new BulbState();
  public PendingStateChange ongoing;
  public BulbState confirmed = new BulbState();

  // TODO chance once a better Device Id implemented
  public String getHubBulbNumber() {
    return mDeviceId;
  }

  public HueBulb(Context c, Long bulbBaseId, String bulbName, String bulbDeviceId,
      HueBulbData bulbData, HubConnection hubConnection, int currentMaxBri) {
    mContext = c;
    mBaseId = bulbBaseId;
    mName = bulbName;
    mDeviceId = bulbDeviceId;
    mData = bulbData;
    mConnection = hubConnection;
    mCurrentMaxBri = Math.max(1, currentMaxBri); // guard to keep maxBri above 0
  }

  @Override
  public void setState(BulbState bs) {
    BulbState preBriAdjusted = bs.clone();
    if (preBriAdjusted.bri != null)
      preBriAdjusted.bri = (int) (preBriAdjusted.bri * mCurrentMaxBri / 100f);
    desiredState.merge(preBriAdjusted);

    mConnection.getChangedQueue().add(this);
  }

  @Override
  public BulbState getState() {
    return confirmed;
  }

  public void confirm(PendingStateChange transmitted) {
    // remove successful changes from pending
    ongoing = null;

    Log.d("confirm", "pre" + desiredState.toString());
    // recalculate any remaining desired state
    desiredState = transmitted.sentState.delta(desiredState);
    Log.d("confirm", "post" + desiredState.toString());


    // update confirmed
    BulbState.confirmChange(confirmed, transmitted.sentState);
  }

  public boolean hasOngoingTransmission() {
    return ongoing != null;
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
  public int getCurrentMaxBrightness() {
    return mCurrentMaxBri;
  }

  @Override
  public void setCurrentMaxBrightness(int bri, boolean maxBriMode) {
    Log.d("brightness", "setCurrentMaxBrightness");

    boolean addToQueue = false;
    bri = Math.max(1, bri); // guard to keep maxBri above 0

    if (mCurrentMaxBri != bri) {
      int oldVal = mCurrentMaxBri;
      mCurrentMaxBri = bri;
      ContentValues cv = new ContentValues();
      cv.put(DatabaseDefinitions.NetBulbColumns.CURRENT_MAX_BRIGHTNESS, mCurrentMaxBri);
      String[] selectionArgs = {"" + mBaseId};
      mContext.getContentResolver().update(DatabaseDefinitions.NetBulbColumns.URI, cv,
          DatabaseDefinitions.NetBulbColumns._ID + " =?", selectionArgs);

      if (maxBriMode) {
        // update the desired brightness value and add to change queue
        if (desiredState.bri == null) {
          int trueBrightness = (int) (255 * (oldVal / 100f));
          desiredState.bri = (int) (trueBrightness * (2.55f * mCurrentMaxBri));
          addToQueue = true;
        }
      }
    }

    if (!maxBriMode && (desiredState.bri == null || desiredState.bri != ((int) (2.55f * bri)))) {
      desiredState.bri = ((int) (2.55f * bri));
      addToQueue = true;
    }

    if (addToQueue)
      mConnection.getChangedQueue().add(this);
  }
}
