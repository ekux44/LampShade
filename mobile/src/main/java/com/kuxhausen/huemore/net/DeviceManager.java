package com.kuxhausen.huemore.net;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.lifx.LifxConnection;
import com.kuxhausen.huemore.net.lifx.LifxManager;
import com.kuxhausen.huemore.persistence.Definitions.NetConnectionColumns;
import com.kuxhausen.huemore.state.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceManager {

  private ArrayList<Connection> mConnections;
  private Context mContext;
  private Group selectedGroup;
  private ArrayList<OnConnectionStatusChangedListener> connectionListeners =
      new ArrayList<OnConnectionStatusChangedListener>();
  public ArrayList<OnStateChangedListener> brightnessListeners =
      new ArrayList<OnStateChangedListener>();
  public ArrayList<OnStateChangedListener> stateListeners = new ArrayList<OnStateChangedListener>();
  private HashMap<Long, NetworkBulb> bulbMap;
  private MyObserver mConnectionObserver;
  private LifxManager mLifxManager;
  private ArrayList<Pair<Group, BrightnessManager>>
      mBrightnessManagers =
      new ArrayList<Pair<Group, BrightnessManager>>();


  public DeviceManager(Context c) {
    mContext = c;

    mConnectionObserver = new MyObserver(new Handler(Looper.getMainLooper()));
    mContext.getContentResolver().registerContentObserver(NetConnectionColumns.URI, true,
                                                          mConnectionObserver);

    loadEverythingFromDatabase();
  }

  private void loadEverythingFromDatabase() {
    destroyManagers();
    { // load all connections from the database
      mConnections = new ArrayList<Connection>();

      //load any hue connections
      mConnections.addAll(HubConnection.loadHubConnections(mContext, this));

      //load any lifx connections
      List<LifxConnection> lifxConnections = LifxManager.loadConnections(mContext, this);
      if (!lifxConnections.isEmpty()) {
        mLifxManager = new LifxManager();
        mLifxManager.onCreate(mContext, this, lifxConnections);
        mConnections.addAll(lifxConnections);
      }
    }
    // load all network bulbs from the connections
    bulbMap = new HashMap<Long, NetworkBulb>();
    for (Connection con : mConnections) {
      for (NetworkBulb bulb : con.getBulbs()) {
        bulbMap.put(bulb.getBaseId(), bulb);
      }
    }

    onBulbsListChanged();
  }

  public void destroyManagers() {
    if (mLifxManager != null) {
      mLifxManager.onDestroy();
      mLifxManager = null;
    }
    mBrightnessManagers.clear();
  }

  public void onDestroy() {
    destroyManagers();
    mContext.getContentResolver().unregisterContentObserver(mConnectionObserver);
    for (Connection c : mConnections) {
      c.onDestroy();
    }
  }

  public Group getSelectedGroup() {
    return selectedGroup;
  }

  public void onGroupSelected(Group group, Integer optionalBri) {
    selectedGroup = group;

    // TODO
  }

  public BrightnessManager obtainBrightnessManager(Group g) {
    assert g!=null;

    //if a manager already exists for this group, return it
    for (Pair<Group, BrightnessManager> pair : mBrightnessManagers) {
      if (pair.first.equals(g)) {
        return pair.second;
      }
    }

    //else eliminate any conflicting managers
    for (int i = 0; i < mBrightnessManagers.size(); i++) {
      if (mBrightnessManagers.get(i).first.conflictsWith(g)) {
        mBrightnessManagers.remove(i);
        i--;
      }
    }

    //and create a new manager
    ArrayList<NetworkBulb> networkBulbs = new ArrayList<NetworkBulb>();
    for (long id : g.getNetworkBulbDatabaseIds()) {
      if (bulbMap.containsKey(id)) {
        networkBulbs.add(bulbMap.get(id));
      }
    }
    BrightnessManager newManager = new BrightnessManager(networkBulbs);
    mBrightnessManagers.add(new Pair<Group, BrightnessManager>(g, newManager));
    return newManager;
  }

  /**
   * if a BrightnessManager is assigned to that group, return it
   * else return null
   */
  public BrightnessManager peekBrightnessManager(Group g){
    //if a manager exists for this group, return it
    for (Pair<Group, BrightnessManager> pair : mBrightnessManagers) {
      if (pair.first.equals(g)) {
        return pair.second;
      }
    }

    return null;
  }

  public ArrayList<Connection> getConnections() {
    return mConnections;
  }

  public void addOnConnectionStatusChangedListener(OnConnectionStatusChangedListener l) {
    connectionListeners.add(l);
  }

  public void removeOnConnectionStatusChangedListener(OnConnectionStatusChangedListener l) {
    connectionListeners.remove(l);
  }

  public void onConnectionChanged() {
    for (OnConnectionStatusChangedListener l : connectionListeners) {
      l.onConnectionStatusChanged();
    }
  }

  public interface OnStateChangedListener {

    public void onStateChanged();
  }

  /**
   * announce state changes to any listeners *
   */
  public void onStateChanged() {
    // only send brightnessListeners brightness state changes
    for (OnStateChangedListener l : brightnessListeners) {
      l.onStateChanged();
    }

    for (OnStateChangedListener l : stateListeners) {
      l.onStateChanged();
    }
  }

  public void registerStateListener(OnStateChangedListener l) {
    stateListeners.add(l);
  }

  public void removeStateListener(OnStateChangedListener l) {
    stateListeners.remove(l);
  }

  public void registerBrightnessListener(OnStateChangedListener l) {
    brightnessListeners.add(l);
    l.onStateChanged();
  }

  public void removeBrightnessListener(OnStateChangedListener l) {
    brightnessListeners.remove(l);
  }

  public void onBulbsListChanged() {
    bulbMap.clear();
    for (Connection con : mConnections) {
      ArrayList<NetworkBulb> conBulbs = con.getBulbs();
      for (NetworkBulb bulb : conBulbs) {
        bulbMap.put(bulb.getBaseId(), bulb);
      }
    }
  }

  public NetworkBulb getNetworkBulb(Long bulbDeviceId) {
    return bulbMap.get(bulbDeviceId);
  }

  class MyObserver extends ContentObserver {

    public MyObserver(Handler handler) {
      super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
      this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      loadEverythingFromDatabase();
    }
  }

  /**
   * Safely deletes references to connection's bulbs & connection, then deletes from database
   *
   * @param selected connection to delete
   */
  public void delete(Connection selected) {
    // TODO Auto-generated method stub
    for (NetworkBulb nb : selected.getBulbs()) {
      bulbMap.remove(nb.getBaseId());
    }
    this.mConnections.remove(selected);

    selected.delete();
    this.onConnectionChanged();
  }
}
