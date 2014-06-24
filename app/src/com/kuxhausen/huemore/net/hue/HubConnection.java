package com.kuxhausen.huemore.net.hue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import alt.android.os.CountDownTimer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.net.NetworkBulb.ConnectivityState;
import com.kuxhausen.huemore.net.hue.api.Bulb;
import com.kuxhausen.huemore.net.hue.api.BulbAttributes;
import com.kuxhausen.huemore.net.hue.api.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.net.hue.api.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.net.hue.api.ConnectionMonitor;
import com.kuxhausen.huemore.net.hue.api.NetworkMethods;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.state.BulbState;

public class HubConnection implements Connection, OnBulbAttributesReturnedListener,
    ConnectionMonitor, OnBulbListReturnedListener {

  private static final String[] columns = {NetConnectionColumns._ID,
      NetConnectionColumns.TYPE_COLUMN, NetConnectionColumns.NAME_COLUMN,
      NetConnectionColumns.DEVICE_ID_COLUMN, NetConnectionColumns.JSON_COLUMN};
  private static final String[] bulbColumns = {NetBulbColumns._ID,
      NetBulbColumns.CONNECTION_DATABASE_ID, NetBulbColumns.TYPE_COLUMN,
      NetBulbColumns.NAME_COLUMN, NetBulbColumns.DEVICE_ID_COLUMN, NetBulbColumns.JSON_COLUMN,
      NetBulbColumns.CURRENT_MAX_BRIGHTNESS};
  private static final Integer TYPE = NetBulbColumns.NetBulbType.PHILIPS_HUE;
  private static final Gson gson = new Gson();
  private static final int MAX_NUM_CONCURRENT_REQUESTS_PER_BULB = 1;

  private Long mBaseId;
  private String mName, mDeviceId;
  HubData mData;
  private Context mContext;
  private LinkedHashSet<HueBulb> mChangedQueue;
  private ArrayList<HueBulb> mBulbList;
  private ArrayList<Route> myRoutes;
  private long lastDisconnectedPingInElapsedRealtime;
  private static final long discounnectedPingIntervalMilis = 1000;

  /**
   * once STALL_THRESHOLD many consecutive send failures have occured, stop reporting pendingWork
   * that keeps device awake reset everytime a send succeeds
   */
  private long stallCount;
  private final static long STALL_THRESHOLD = 100;

  public HubConnection(Context c, Long baseId, String name, String deviceId, HubData data,
      DeviceManager dm) {
    mContext = c;
    mBaseId = baseId;
    mName = name;
    mDeviceId = deviceId;
    mData = data;

    mBulbList = new ArrayList<HueBulb>();
    mChangedQueue = new LinkedHashSet<HueBulb>();
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
      int currentMaxBri = cursor.getInt(6);
      mBulbList.add(new HueBulb(c, bulbBaseId, bulbName, bulbDeviceId, bulbData, this,
          currentMaxBri));
    }


    // junk?
    mDeviceManager = dm;
    volleyRQ = Volley.newRequestQueue(mContext);
    restartCountDownTimer();

    // initalized state
    this.mDeviceManager.onStateChanged();
  }

  @Override
  public void initializeConnection(Context c) {
    myRoutes.clear();
    if (mData != null && mData.localHubAddress != null)
      myRoutes.add(new Route(mData.localHubAddress, ConnectivityState.Unknown));
    if (mData != null && mData.portForwardedAddress != null)
      myRoutes.add(new Route(mData.portForwardedAddress, ConnectivityState.Unknown));

    for (Route route : getBestRoutes())
      NetworkMethods.PreformGetBulbList(route, mData.hashedUsername, mContext, getRequestQueue(),
          this, this);
  }

  public List<Route> getBestRoutes() {
    ConnectivityState bestSoFar = ConnectivityState.Unreachable;
    ArrayList<Route> result = new ArrayList<Route>();

    for (Route route : myRoutes) {
      if (route.state == bestSoFar)
        result.add(route);
      else if (route.isMoreConnectedThan(bestSoFar)) {
        result.clear();
        result.add(route);
      }
    }

    return result;
  }

  @Override
  public void onDestroy() {
    if (countDownTimer != null)
      countDownTimer.cancel();
    volleyRQ.cancelAll("");
    mChangedQueue = null;
    mBulbList = null;
  }

  public static ArrayList<HubConnection> loadHubConnections(Context c, DeviceManager dm) {
    ArrayList<HubConnection> hubs = new ArrayList<HubConnection>();

    String[] selectionArgs = {"" + NetBulbColumns.NetBulbType.PHILIPS_HUE};
    Cursor cursor =
        c.getContentResolver().query(NetConnectionColumns.URI, columns,
            NetConnectionColumns.TYPE_COLUMN + " = ?", selectionArgs, null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      Long baseId = cursor.getLong(0);
      String name = cursor.getString(2);
      String deviceId = cursor.getString(3);
      HubData data = gson.fromJson(cursor.getString(4), HubData.class);
      hubs.add(new HubConnection(c, baseId, name, deviceId, data, dm));
    }

    // initialize all connections
    for (HubConnection h : hubs)
      h.initializeConnection(c);

    return hubs;
  }


  @Override
  public ArrayList<NetworkBulb> getBulbs() {
    ArrayList<NetworkBulb> result = new ArrayList<NetworkBulb>(mBulbList.size());
    result.addAll(mBulbList);
    return result;
  }


  @Override
  public void setHubConnectionState(Route r, boolean connected) {
    if (connected)
      r.state = ConnectivityState.Connected;
    else
      r.state = ConnectivityState.Unreachable;

    mDeviceManager.onConnectionChanged();
    if (!getBestRoutes().isEmpty() && getBestRoutes().get(0).state != ConnectivityState.Connected) {
      if (SystemClock.elapsedRealtime() - lastDisconnectedPingInElapsedRealtime > discounnectedPingIntervalMilis) {
        lastDisconnectedPingInElapsedRealtime = SystemClock.elapsedRealtime();
        for (Route route : getBestRoutes())
          NetworkMethods.PreformGetBulbList(route, mData.hashedUsername, mContext,
              getRequestQueue(), this, this);
      }
    }
  }

  protected LinkedHashSet<HueBulb> getChangedQueue() {
    return mChangedQueue;
  }

  private DeviceManager mDeviceManager;
  private RequestQueue volleyRQ;
  private CountDownTimer countDownTimer;
  private final static int TRANSMITS_PER_SECOND = 10;

  public enum KnownState {
    Unknown, ToSend, Getting, Synched
  };


  public RequestQueue getRequestQueue() {
    return volleyRQ;
  }

  public void onAttributesReturned(BulbAttributes result, int bulbNumber) {
    // //figure out which bulb in group (if that group is still selected)
    // int index = calculateBulbPositionInGroup(bulbNumber, mDeviceManager.getSelectedGroup());
    // //if group is still expected this, save
    // if(index>-1 && bulbKnown[index]==KnownState.Getting){
    // bulbKnown[index] = KnownState.Synched;
    // bulbBri[index] = result.state.bri;
    //
    // //if all expected get brightnesses have returned, compute maxbri and notify listeners
    // boolean anyOutstandingGets = false;
    // for(KnownState ks : bulbKnown)
    // anyOutstandingGets |= (ks == KnownState.Getting);
    // if(!anyOutstandingGets){
    // //todo calc more intelligent bri when mood known
    // int briSum = 0;
    // for(int bri : bulbBri)
    // briSum +=bri;
    // maxBrightness = briSum/mDeviceManager.getSelectedGroup().groupAsLegacyArray.length;
    //
    // for(int i = 0; i< mDeviceManager.getSelectedGroup().groupAsLegacyArray.length; i++){
    // bulbBri[i]= maxBrightness;
    // bulbRelBri[i] = MAX_REL_BRI;
    // }
    //
    // mDeviceManager.onStateChanged();
    // }
    // }
  }


  public void restartCountDownTimer() {

    if (countDownTimer != null)
      countDownTimer.cancel();

    // runs at the rate to execute 15 op/sec
    countDownTimer = new CountDownTimer(Integer.MAX_VALUE, (1000 / TRANSMITS_PER_SECOND)) {

      @Override
      public void onFinish() {}

      @Override
      public void onTick(long millisUntilFinished) {
        if (mChangedQueue.size() > 0) {
          HueBulb selected = mChangedQueue.iterator().next();
          mChangedQueue.remove(selected);

          // TODO re enable sendState optimization after further api transience testing
          BulbState toSend = selected.desiredState;// getSendState(selected);
          if (toSend != null && selected.ongoing.size() <= MAX_NUM_CONCURRENT_REQUESTS_PER_BULB) {

            PendingStateChange stateChange =
                new PendingStateChange(toSend, selected, System.nanoTime());
            for (Route route : getBestRoutes())
              NetworkMethods.preformTransmitPendingState(route, mData.hashedUsername, mContext,
                  getRequestQueue(), HubConnection.this, stateChange);
            selected.ongoing.add(stateChange);
          }
        }
      }
    };
    countDownTimer.start();
  }

  @Override
  public void onListReturned(Bulb[] result) {
    outer: for (int i = 0; i < result.length; i++) {
      Bulb fromHue = result[i];

      for (int j = 0; j < mBulbList.size(); j++) {
        NetworkBulb fromMemory = mBulbList.get(j);

        // check to see if this bulb is already in our database
        if (fromMemory instanceof HueBulb
            && ((HueBulb) fromMemory).getHubBulbNumber().equals(fromHue.number)) {
          if (!fromMemory.getName().equals(fromHue.name)) {
            // same bulb but has been renamed by another device
            // must update our version

            ContentValues cv = new ContentValues();
            cv.put(NetBulbColumns.NAME_COLUMN, fromHue.name);
            String[] selectionArgs = {"" + fromHue.number};
            mContext.getContentResolver().update(NetBulbColumns.URI, cv,
                NetBulbColumns.DEVICE_ID_COLUMN + " = ?", selectionArgs);
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
      cv.put(NetBulbColumns.JSON_COLUMN, gson.toJson(new HueBulbData()));
      cv.put(NetBulbColumns.TYPE_COLUMN, NetBulbColumns.NetBulbType.PHILIPS_HUE);
      cv.put(NetBulbColumns.CURRENT_MAX_BRIGHTNESS, 100);
      String[] selectionArgs = {"" + fromHue.number};
      long bulbBaseId =
          Long.parseLong(mContext.getContentResolver().insert(NetBulbColumns.URI, cv)
              .getLastPathSegment());

      mBulbList.add(new HueBulb(mContext, bulbBaseId, bulbName, bulbDeviceId, new HueBulbData(),
          this, 100));

    }

    // manually force reload the list of known bulbs
    // Note test this by trying to play moods on newly connected connections
    this.mDeviceManager.onBulbsListChanged();
  }

  public ConnectivityState getConnectivityState() {
    if (this.getBestRoutes().isEmpty())
      return ConnectivityState.Unreachable;
    else
      return this.getBestRoutes().get(0).state;
  }

  public void reportStateChangeFailure(PendingStateChange mRequest) {
    this.stallCount++;
    mChangedQueue.add(mRequest.hubBulb);
  }

  public void reportStateChangeSucess(PendingStateChange request) {
    this.stallCount = 0;
    HueBulb affected = request.hubBulb;

    // remove successful changes from pending
    affected.ongoing.remove(request);

    // merge successful changes onto confirmed
    affected.confirmed.merge(request.sentState);

    // if more changes should be sent, do so
    if (getSendState(affected) != null) {
      mChangedQueue.add(affected);
    }

    // notify changes
    this.mDeviceManager.onStateChanged();
  }

  /**
   * returns the BulbState delta between current+pending and desired if no delta, return null
   */
  private BulbState getSendState(HueBulb hBulb) {
    BulbState projectedState = hBulb.confirmed.clone();
    for (PendingStateChange p : hBulb.ongoing) {
      projectedState.merge(p.sentState);
    }
    BulbState dif = projectedState.delta(hBulb.desiredState);

    if (dif.toString() != null && dif.toString().length() > 0)
      return dif;
    return null;
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

  @Override
  public boolean hasPendingWork() {
    if (stallCount > STALL_THRESHOLD) {
      // pending work completely stalled, so don't report it and keep device awake
      return false;
    }

    boolean hasPendingWork = false;
    for (HueBulb hb : mBulbList) {
      if (!hb.ongoing.isEmpty()) {
        hasPendingWork = true;
      }
    }
    return hasPendingWork;
  }

  public HubData getHubData() {
    return mData;
  }

  /**
   * saves new HubData and reinitializes connections
   * 
   * @param newPaths
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
}
