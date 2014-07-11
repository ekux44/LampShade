package com.kuxhausen.huemore.net.lifx;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.kuxhausen.huemore.net.DeviceManager;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

public class LifxManager implements LFXNetworkContext.LFXNetworkContextListener,
                                    LFXLightCollection.LFXLightCollectionListener {

  private LFXNetworkContext networkContext;
  private WifiManager.MulticastLock ml = null;

  private DeviceManager mDeviceManager;

  public void onCreate(Context c, DeviceManager dm) {
    mDeviceManager = dm;

    WifiManager wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    ml = wifi.createMulticastLock("lifx_samples_tag");
    ml.setReferenceCounted(true);
    ml.acquire();

    Log.d("lifx", "lifxManager onCreate");

    networkContext = LFXClient.getSharedInstance(c).getLocalNetworkContext();
    networkContext.addNetworkContextListener(this);
    networkContext.getAllLightsCollection().addLightCollectionListener(this);
    networkContext.connect();

    Log.d("lifx"," num lights now:" + networkContext.getAllLightsCollection().getLights()
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

  }

  @Override
  public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection, LFXLight light) {
    Log.d("lifx", "lifxManager lightCollectionDidRemoveLight");
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
