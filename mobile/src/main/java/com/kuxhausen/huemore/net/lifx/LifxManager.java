package com.kuxhausen.huemore.net.lifx;

import com.google.gson.Gson;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;

import java.util.ArrayList;
import java.util.List;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

public class LifxManager implements LFXNetworkContext.LFXNetworkContextListener,
                                    LFXLightCollection.LFXLightCollectionListener {

  private static final String[] columns = {DatabaseDefinitions.NetConnectionColumns._ID,
                                           DatabaseDefinitions.NetConnectionColumns.TYPE_COLUMN,
                                           DatabaseDefinitions.NetConnectionColumns.NAME_COLUMN,
                                           DatabaseDefinitions.NetConnectionColumns.DEVICE_ID_COLUMN,
                                           DatabaseDefinitions.NetConnectionColumns.JSON_COLUMN};
  private static final Gson gson = new Gson();


  private LFXNetworkContext networkContext;
  private WifiManager.MulticastLock ml = null;

  private DeviceManager mDeviceManager;
  private List<LifxConnection> mConnections;

  public static List<LifxConnection> loadConnections(Context c, DeviceManager dm) {
    ArrayList<LifxConnection> connections = new ArrayList<LifxConnection>();

    String[] selectionArgs = {"" + DatabaseDefinitions.NetBulbColumns.NetBulbType.LIFX};
    Cursor cursor =
        c.getContentResolver().query(DatabaseDefinitions.NetConnectionColumns.URI, columns,
                                     DatabaseDefinitions.NetConnectionColumns.TYPE_COLUMN + " = ?",
                                     selectionArgs, null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      Long baseId = cursor.getLong(0);
      String name = cursor.getString(2);
      String deviceId = cursor.getString(3);
      LifxConnection.ExtraData
          data =
          gson.fromJson(cursor.getString(4), LifxConnection.ExtraData.class);
      connections.add(new LifxConnection(c, baseId, name, deviceId, data, dm));
    }
    Log.d("lifx",connections.size() +" connections loaded");
    return connections;
  }


  public void onCreate(Context c, DeviceManager dm, List<LifxConnection> toInitialize) {
    mDeviceManager = dm;
    mConnections = toInitialize;

    WifiManager wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    ml = wifi.createMulticastLock("lifx_samples_tag");
    ml.setReferenceCounted(true);
    ml.acquire();

    Log.d("lifx", "lifxManager onCreate");

    networkContext = LFXClient.getSharedInstance(c).getLocalNetworkContext();
    networkContext.addNetworkContextListener(this);
    networkContext.getAllLightsCollection().addLightCollectionListener(this);
    networkContext.connect();

    Log.d("lifx", " num lights now:" + networkContext.getAllLightsCollection().getLights()
        .size());

  }


  public void onDestroy() {
    networkContext.disconnect();
    if (ml != null) {
      ml.release();
    }
  }

  @Override
  public void networkContextDidConnect(LFXNetworkContext networkContext) {
    Log.d("lifx",
          "didConnect, num lights now:" + networkContext.getAllLightsCollection().getLights()
              .size()
    );

    mDeviceManager.onBulbsListChanged();
    mDeviceManager.onConnectionChanged();
  }

  @Override
  public void networkContextDidDisconnect(LFXNetworkContext networkContext) {
    mDeviceManager.onConnectionChanged();
    Log.d("lifx", "lifxManager networkContextDidDisconnect");
  }

  @Override
  public void networkContextDidAddTaggedLightCollection(LFXNetworkContext networkContext,
                                                        LFXTaggedLightCollection collection) {
    Log.d("lifx", "lifxManager networkContextDidAddTaggedLightCollection");

  }

  @Override
  public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext networkContext,
                                                           LFXTaggedLightCollection collection) {
    Log.d("lifx", "lifxManager networkContextDidRemoveTaggedLightCollection");

  }

  @Override
  public void lightCollectionDidAddLight(LFXLightCollection lightCollection, LFXLight light) {
    Log.d("lifx", "lifxManager lightCollectionDidAddLight");

    for (LifxConnection con : mConnections) {
      if (con.getDeviceId().equals(light.getDeviceID())) {
        con.lightConnected(light);
      }
    }
  }

  @Override
  public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection, LFXLight light) {
    Log.d("lifx", "lifxManager lightCollectionDidRemoveLight");

    for (LifxConnection con : mConnections) {
      if (con.getDeviceId().equals(light.getDeviceID())) {
        con.lightConnected(light);
      }
    }
  }

  @Override
  public void lightCollectionDidChangeLabel(LFXLightCollection lightCollection, String label) {
    Log.d("lifx", "lifxManager lightCollectionDidChangeLabel");
  }

  @Override
  public void lightCollectionDidChangeColor(LFXLightCollection lightCollection,
                                            LFXHSBKColor color) {
    Log.d("lifx", "lifxManager lightCollectionDidChangeColor");
  }

  @Override
  public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection lightCollection,
                                                      LFXTypes.LFXFuzzyPowerState fuzzyPowerState) {
    Log.d("lifx", "lifxManager lightCollectionDidChangeFuzzyPowerState");
  }
}
