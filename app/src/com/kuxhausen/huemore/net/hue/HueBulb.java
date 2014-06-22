package com.kuxhausen.huemore.net.hue;

import java.util.ArrayList;

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

  public BulbState desiredState = new BulbState();
  public ArrayList<PendingStateChange> ongoing = new ArrayList<PendingStateChange>();
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

    Log.e("play", bs.toString());
    mConnection.getChangedQueue().add(this);
  }

  @Override
  public BulbState getState() {
    // TODO consider changing this to confirmed?
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

    NetworkMethods.PreformSetBulbAttributes(mContext, mConnection.getRequestQueue(), mConnection,
        Integer.parseInt(mDeviceId), bAttrs);
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
  public void setCurrentMaxBrightness(int maxBri) {
    maxBri = Math.max(1, maxBri); // guard to keep maxBri above 0

    if (mCurrentMaxBri != maxBri) {
      int oldVal = mCurrentMaxBri;
      mCurrentMaxBri = maxBri;
      ContentValues cv = new ContentValues();
      cv.put(DatabaseDefinitions.NetBulbColumns.CURRENT_MAX_BRIGHTNESS, mCurrentMaxBri);
      String[] selectionArgs = {"" + mBaseId};
      mContext.getContentResolver().update(DatabaseDefinitions.NetBulbColumns.URI, cv,
          DatabaseDefinitions.NetBulbColumns._ID + " =?", selectionArgs);

      // update the desired brightness value and add to change queue
      if (desiredState.bri == null)
        desiredState.bri = (int) (255 * (oldVal / 100f));
      desiredState.bri = (int) (desiredState.bri * (mCurrentMaxBri / (float) oldVal));
      mConnection.getChangedQueue().add(this);
    }
  }
}
