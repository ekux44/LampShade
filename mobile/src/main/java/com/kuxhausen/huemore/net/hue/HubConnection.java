package com.kuxhausen.huemore.net.hue;

import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.net.hue.api.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.net.hue.api.ConnectionMonitor;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.NetConnectionColumns;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.utils.RateLimiter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import alt.android.os.CountDownTimer;

public class HubConnection implements Connection, OnBulbAttributesReturnedListener,
                                      ConnectionMonitor, OnBulbListReturnedListener {

  private static final String[] columns = {NetConnectionColumns._ID,
                                           NetConnectionColumns.TYPE_COLUMN,
                                           NetConnectionColumns.NAME_COLUMN,
                                           NetConnectionColumns.DEVICE_ID_COLUMN,
                                           NetConnectionColumns.JSON_COLUMN};
  private static final String[] bulbColumns = {NetBulbColumns._ID,
                                               NetBulbColumns.CONNECTION_DATABASE_ID,
                                               NetBulbColumns.TYPE_COLUMN,
                                               NetBulbColumns.NAME_COLUMN,
                                               NetBulbColumns.DEVICE_ID_COLUMN,
                                               NetBulbColumns.JSON_COLUMN};
  private static final Integer TYPE = NetBulbColumns.NetBulbType.PHILIPS_HUE;
  private static final Gson gson = new Gson();

  //In milis
  private final static long TRANSMIT_TIMEOUT_TIME = 10000;
  // In SystemClock.elapsedRealtime();
  private Long mDesiredLastChanged;

  private Long mBaseId;
  private String mName, mDeviceId;
  HubData mData;
  private Context mContext;
  private ArrayList<HueBulb> mBulbList;
  private ArrayList<Route> myRoutes;
  public ChangeLoopManager mLoopManager;

  private DeviceManager mDeviceManager;
  private RequestQueue volleyRQ;

  public HubConnection(Context c, Long baseId, String name, String deviceId, HubData data,
                       DeviceManager dm) {
    mContext = c;
    mBaseId = baseId;
    mName = name;
    mDeviceId = deviceId;
    mData = data;

    mBulbList = new ArrayList<HueBulb>();
    mLoopManager = new ChangeLoopManager();
    myRoutes = new ArrayList<Route>();

    String selection =
        NetBulbColumns.TYPE_COLUMN + " = ?  AND " + NetBulbColumns.CONNECTION_DATABASE_ID + " = ?";
    String[] selectionArgs = {"" + TYPE, "" + mBaseId};
    Cursor cursor =
        c.getContentResolver().query(NetBulbColumns.URI, bulbColumns, selection, selectionArgs,
                                     null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      Long bulbBaseId = cursor.getLong(0);
      String bulbName = cursor.getString(3);
      String bulbDeviceId = cursor.getString(4);
      HueBulbData bulbData = gson.fromJson(cursor.getString(5), HueBulbData.class);
      mBulbList.add(new HueBulb(c, bulbBaseId, bulbName, bulbDeviceId, bulbData, this));
    }
    cursor.close();

    // junk?
    mDeviceManager = dm;
    volleyRQ = Volley.newRequestQueue(mContext);

    // initalized state
    this.mDeviceManager.onStateChanged();
  }

  @Override
  public void initializeConnection(Context c) {
    myRoutes.clear();
    if (mData != null && mData.localHubAddress != null) {
      myRoutes.add(new Route(mData.localHubAddress, ConnectivityState.Unknown));
    }
    if (mData != null && mData.portForwardedAddress != null) {
      myRoutes.add(new Route(mData.portForwardedAddress, ConnectivityState.Unknown));
    }

    getLooper().queueGetList();

    for (HueBulb b : this.mBulbList) {
      getLooper().queueGetState(b);
    }
  }

  public List<Route> getBestRoutes() {
    ConnectivityState bestSoFar = ConnectivityState.Unreachable;
    ArrayList<Route> result = new ArrayList<Route>();

    for (Route route : myRoutes) {
      if (route.state == bestSoFar && route.state != ConnectivityState.Connected) {
        result.add(route);
      } else if (route.isMoreConnectedThan(bestSoFar)) {
        result.clear();
        result.add(route);
      }
    }

    return result;
  }

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }

  @Override
  public void onDestroy() {
    mLoopManager.onDestroy();
    volleyRQ.cancelAll("");
    mBulbList = null;
  }

  public static ArrayList<HubConnection> loadHubConnections(Context c, DeviceManager dm) {
    ArrayList<HubConnection> hubs = new ArrayList<HubConnection>();

    String[] selectionArgs = {"" + NetBulbColumns.NetBulbType.PHILIPS_HUE};
    Cursor cursor =
        c.getContentResolver().query(NetConnectionColumns.URI, columns,
                                     NetConnectionColumns.TYPE_COLUMN + " = ?", selectionArgs,
                                     null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      Long baseId = cursor.getLong(0);
      String name = cursor.getString(2);
      String deviceId = cursor.getString(3);
      HubData data = gson.fromJson(cursor.getString(4), HubData.class);
      hubs.add(new HubConnection(c, baseId, name, deviceId, data, dm));
    }
    cursor.close();

    // initialize all connections
    for (HubConnection h : hubs) {
      h.initializeConnection(c);
    }

    return hubs;
  }


  @Override
  public ArrayList<NetworkBulb> getBulbs() {
    if (mBulbList != null) {
      ArrayList<NetworkBulb> result = new ArrayList<NetworkBulb>(mBulbList.size());
      result.addAll(mBulbList);
      return result;
    } else {
      //TODO fix root cause
      //this case should never occur, but it does
      return new ArrayList<NetworkBulb>();
    }

  }


  @Override
  public void setHubConnectionState(Route r, ConnectivityState newState) {
    if (r.state != newState) {
      r.state = newState;
      mDeviceManager.onConnectionChanged();
    }
  }

  public RequestQueue getRequestQueue() {
    return volleyRQ;
  }

  @Override
  public void onAttributesReturned(BulbAttributes result, String bulbHueId) {
    for (HueBulb bulb : this.mBulbList) {
      if (bulb.getHubBulbNumber().equals(bulbHueId)) {
        //found the bulb who's attributes were returned
        bulb.attributesReturned(result);
      }
    }
  }


  @Override
  public void onListReturned(BulbAttributes[] result) {
    outer:
    for (int i = 0; i < result.length; i++) {
      BulbAttributes fromHue = result[i];

      for (int j = 0; j < mBulbList.size(); j++) {
        HueBulb fromMemory = mBulbList.get(j);
        // check to see if this bulb is already in our database
        if (fromMemory.getHubBulbNumber().equals(fromHue.number)) {
          if (!fromMemory.getName().equals(fromHue.name)) {
            // A known bulb's name has changed
            ContentValues cv = new ContentValues();
            cv.put(NetBulbColumns.NAME_COLUMN, fromHue.name);
            String[] selectionArgs = {"" + fromHue.number};
            mContext.getContentResolver().update(NetBulbColumns.URI, cv,
                                                 NetBulbColumns.DEVICE_ID_COLUMN + " = ?",
                                                 selectionArgs);
          }
          if (!fromMemory.getData().matches(fromHue.getHueBulbData())) {
            // A known bulb's data attributes have changed
            ContentValues cv = new ContentValues();
            cv.put(NetBulbColumns.JSON_COLUMN, gson.toJson(fromHue.getHueBulbData()));
            String[] selectionArgs = {"" + fromHue.number};
            mContext.getContentResolver().update(NetBulbColumns.URI, cv,
                                                 NetBulbColumns.DEVICE_ID_COLUMN + " = ?",
                                                 selectionArgs);
          }
          continue outer;
        }
      }
      // if we reach this point, must not already be in memory, so add to database and memory
      String bulbName = fromHue.name;
      String bulbDeviceId = fromHue.number + "";

      ContentValues cv = new ContentValues();
      cv.put(NetBulbColumns.NAME_COLUMN, bulbName);
      cv.put(NetBulbColumns.DEVICE_ID_COLUMN, bulbDeviceId);
      cv.put(NetBulbColumns.CONNECTION_DATABASE_ID, mBaseId);
      cv.put(NetBulbColumns.JSON_COLUMN, gson.toJson(fromHue.getHueBulbData()));
      cv.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NetBulbType.PHILIPS_HUE);
      cv.put(NetBulbColumns.CURRENT_MAX_BRIGHTNESS, 100);
      long bulbBaseId =
          Long.parseLong(mContext.getContentResolver().insert(NetBulbColumns.URI, cv)
                             .getLastPathSegment());

      mBulbList.add(new HueBulb(mContext, bulbBaseId, bulbName, bulbDeviceId,
                                fromHue.getHueBulbData(), this));

    }

    // manually force reload the list of known bulbs
    // Note test this by trying to play moods on newly connected connections
    this.mDeviceManager.onBulbsListChanged();
  }

  public ConnectivityState getConnectivityState() {
    if (this.getBestRoutes().isEmpty()) {
      return ConnectivityState.Unreachable;
    } else {
      return this.getBestRoutes().get(0).state;
    }
  }

  public void reportStateChangeFailure(PendingStateChange mRequest) {
    mRequest.hubBulb.lastSendInitiatedTime = null;
    this.mLoopManager.queueSendState(mRequest.hubBulb);
  }

  public void reportStateChangeSucess(PendingStateChange request) {
    HueBulb affected = request.hubBulb;

    affected.confirm(request.sentState);

    // if more changes should be sent, do so
    if (affected.hasPendingTransmission()) {
      this.mLoopManager.queueSendState(affected);
    }

    // notify changes
    this.mDeviceManager.onStateChanged();
  }


  @Override
  public String mainDescription() {
    // TODO Auto-generated method stub
    // return "placeholder";
    return this.getConnectivityState().name();
  }

  @Override
  public String subDescription() {
    return this.mContext.getResources().getString(R.string.device_hue);
  }

  public void updateDesiredLastChanged() {
    mDesiredLastChanged = SystemClock.elapsedRealtime();
  }

  @Override
  public boolean hasPendingWork() {
    if (mDesiredLastChanged != null
        && (mDesiredLastChanged + this.TRANSMIT_TIMEOUT_TIME) > SystemClock.elapsedRealtime()) {

      boolean hasPendingWork = false;
      for (HueBulb hb : mBulbList) {
        if (hb.hasOngoingTransmission()) {
          hasPendingWork = true;
        }
      }
      return hasPendingWork;
    }
    return false;
  }

  public HubData getHubData() {
    return mData;
  }

  /**
   * saves new HubData and reinitializes connections
   */
  public void updateConfiguration(HubData newPaths) {
    mData = newPaths;

    ContentValues cv = new ContentValues();
    cv.put(NetConnectionColumns.JSON_COLUMN, gson.toJson(mData));

    String selector = NetConnectionColumns._ID + "=?";
    String[] selectionArgs = {"" + mBaseId};
    mContext.getContentResolver().update(NetConnectionColumns.URI, cv, selector, selectionArgs);

    initializeConnection(mContext);
  }

  @Override
  public void delete() {
    this.onDestroy();

    String selector = NetConnectionColumns._ID + "=?";
    String[] selectionArgs = {"" + mBaseId};
    mContext.getContentResolver().delete(NetConnectionColumns.URI, selector, selectionArgs);
  }

  public ChangeLoopManager getLooper() {
    return mLoopManager;
  }

  public class ChangeLoopManager {

    // How many state changes can be sent per second.
    private final static int TRANSMITS_PER_SECOND = 24;

    private LinkedHashSet<HueBulb> mOutgoingStateQueue = new LinkedHashSet<HueBulb>();
    private LinkedHashSet<HueBulb> mIncomingStateQueue = new LinkedHashSet<HueBulb>();
    private boolean requestList = false;

    private CountDownTimer countDownTimer;
    private RateLimiter mRateLimiter;
    private HueBulb mPendingOutgoingState;

    public ChangeLoopManager() {
      mRateLimiter = new RateLimiter(1000L, TRANSMITS_PER_SECOND);
    }

    protected void onDestroy() {
      if (countDownTimer != null) {
        countDownTimer.cancel();
      }
    }

    public void queueSendState(HueBulb changed) {
      mOutgoingStateQueue.add(changed);
      ensureLooping();
    }

    public void queueGetState(HueBulb querry) {
      mIncomingStateQueue.add(querry);
      ensureLooping();
    }

    public void queueGetList() {
      requestList = true;
      ensureLooping();
    }

    private void ensureLooping() {
      if (countDownTimer == null) {
        countDownTimer = new CountDownTimer(Integer.MAX_VALUE, 1000L / TRANSMITS_PER_SECOND) {

          @Override
          public void onFinish() {
          }

          @Override
          public void onTick(long millisUntilFinished) {
            if (requestList) {
              for (Route route : getBestRoutes()) {
                NetworkMethods
                    .getBulbList(route, mData.hashedUsername, mContext, getRequestQueue(),
                                 HubConnection.this, HubConnection.this);

                Log.d("net.hue.connection.onTi", "perform request list");
              }
              requestList = false;
            } else if (mPendingOutgoingState != null || mOutgoingStateQueue.size() > 0) {
              if(mPendingOutgoingState == null) {
                mPendingOutgoingState = mOutgoingStateQueue.iterator().next();
                mOutgoingStateQueue.remove(mPendingOutgoingState);
              }
              BulbState toSend = mPendingOutgoingState.getSendState();
              if (toSend != null && !toSend.isEmpty()
                  && mPendingOutgoingState.lastSendInitiatedTime == null) {
                long sentTime = SystemClock.elapsedRealtime();
                int capacity = HueUtils.countZibBeeCommandsRequired(toSend);
                if(!mRateLimiter.hasCapacity(sentTime, capacity)) {
                  return;
                }
                mRateLimiter.consumeCapacity(sentTime, capacity);
                PendingStateChange
                    stateChange =
                    new PendingStateChange(toSend, mPendingOutgoingState);
                for (Route route : getBestRoutes()) {
                  NetworkMethods.transmitPendingState(route, mData.hashedUsername, mContext,
                                                      getRequestQueue(), HubConnection.this,
                                                      stateChange);
                  Log.d("net.hue.connection.onTi",
                        "perform transmit" + stateChange.hubBulb.getBaseId()
                        + "," + stateChange.sentState.toString()
                  );
                }
                mPendingOutgoingState.lastSendInitiatedTime = sentTime;
                mPendingOutgoingState = null;
              }
            } else if (mIncomingStateQueue.size() > 0) {
              HueBulb toQuerry = mIncomingStateQueue.iterator().next();
              mIncomingStateQueue.remove(toQuerry);

              for (Route route : getBestRoutes()) {
                NetworkMethods
                    .getBulbAttributes(route, mData.hashedUsername, mContext,
                                       getRequestQueue(),
                                       HubConnection.this, HubConnection.this,
                                       toQuerry.getHubBulbNumber());
                Log.d("net.hue.connection.onTi",
                      "perform querry" + toQuerry.getBaseId()
                );
              }
            } else {
              ChangeLoopManager.this.countDownTimer = null;
              this.cancel();
            }
          }
        };
      }
      countDownTimer.start();
    }

  }
}
