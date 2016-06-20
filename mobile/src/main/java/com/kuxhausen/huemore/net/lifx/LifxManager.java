package com.kuxhausen.huemore.net.lifx;

import com.google.gson.Gson;

import android.content.Context;
import android.database.Cursor;
import android.net.wifi.WifiManager;

import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.utils.DeferredLog;

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

  private static final String[] columns = {Definitions.NetConnectionColumns._ID,
                                           Definitions.NetConnectionColumns.TYPE_COLUMN,
                                           Definitions.NetConnectionColumns.NAME_COLUMN,
                                           Definitions.NetConnectionColumns.DEVICE_ID_COLUMN,
                                           Definitions.NetConnectionColumns.JSON_COLUMN};
  private static final Gson gson = new Gson();


  private LFXNetworkContext networkContext;
  private WifiManager.MulticastLock ml = null;

  private DeviceManager mDeviceManager;
  private List<LifxConnection> mConnections;

  public static List<LifxConnection> loadConnections(Context c, DeviceManager dm) {
    ArrayList<LifxConnection> connections = new ArrayList<LifxConnection>();

    String[] selectionArgs = {"" + Definitions.NetBulbColumns.NetBulbType.LIFX};
    Cursor cursor =
        c.getContentResolver().query(Definitions.NetConnectionColumns.URI, columns,
                                     Definitions.NetConnectionColumns.TYPE_COLUMN + " = ?",
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
    cursor.close();

    DeferredLog.d("lifx", "%d connections loaded", connections.size());
    return connections;
  }

  public void assignLights() {
    LFXLightCollection allLifxLights = networkContext.getAllLightsCollection();
    for (LifxConnection userConnection : mConnections) {
      LFXLight lightExists = allLifxLights.getLightWithDeviceID(userConnection.getDeviceId());
      if (lightExists != null) {
        userConnection.lightConnected(lightExists);
      } else {
        userConnection.lightDisconnected();
      }
    }
  }

  public void onCreate(Context c, DeviceManager dm, List<LifxConnection> toInitialize) {
    mDeviceManager = dm;
    mConnections = toInitialize;
    
    DeferredLog.d("lifx", "lifxManager onCreate");

    networkContext = LFXClient.getSharedInstance(c).getLocalNetworkContext();
    networkContext.addNetworkContextListener(this);
    networkContext.getAllLightsCollection().addLightCollectionListener(this);
    networkContext.connect();

    DeferredLog.d("lifx", " num lights now: %d", networkContext.getAllLightsCollection().getLights()
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
    DeferredLog.d("lifx",
          "didConnect, num lights now: %d", networkContext.getAllLightsCollection().getLights()
              .size()
    );

    assignLights();
    mDeviceManager.onConnectionChanged();
  }

  @Override
  public void networkContextDidDisconnect(LFXNetworkContext networkContext) {
    DeferredLog.d("lifx", "lifxManager networkContextDidDisconnect");

    assignLights();
    mDeviceManager.onConnectionChanged();
  }

  @Override
  public void networkContextDidAddTaggedLightCollection(LFXNetworkContext networkContext,
                                                        LFXTaggedLightCollection collection) {
    DeferredLog.d("lifx", "lifxManager networkContextDidAddTaggedLightCollection");

  }

  @Override
  public void networkContextDidRemoveTaggedLightCollection(LFXNetworkContext networkContext,
                                                           LFXTaggedLightCollection collection) {
    DeferredLog.d("lifx", "lifxManager networkContextDidRemoveTaggedLightCollection");

  }

  @Override
  public void lightCollectionDidAddLight(LFXLightCollection lightCollection, LFXLight light) {
    DeferredLog.d("lifx", "lifxManager lightCollectionDidAddLight");

    assignLights();
    mDeviceManager.onConnectionChanged();
  }

  @Override
  public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection, LFXLight light) {
    DeferredLog.d("lifx", "lifxManager lightCollectionDidRemoveLight");

    assignLights();
    mDeviceManager.onConnectionChanged();
  }

  @Override
  public void lightCollectionDidChangeLabel(LFXLightCollection lightCollection, String label) {
    DeferredLog.d("lifx", "lifxManager lightCollectionDidChangeLabel");
  }

  @Override
  public void lightCollectionDidChangeColor(LFXLightCollection lightCollection,
                                            LFXHSBKColor color) {
    DeferredLog.d("lifx", "lifxManager lightCollectionDidChangeColor");
  }

  @Override
  public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection lightCollection,
                                                      LFXTypes.LFXFuzzyPowerState fuzzyPowerState) {
    DeferredLog.d("lifx", "lifxManager lightCollectionDidChangeFuzzyPowerState");
  }
}
