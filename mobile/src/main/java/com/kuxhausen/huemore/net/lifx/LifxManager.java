package com.kuxhausen.huemore.net.lifx;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.kuxhausen.huemore.net.DeviceManager;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXTaggedLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

public class LifxManager {

    private LFXNetworkContext networkContext;
    private WifiManager.MulticastLock ml = null;

    private DeviceManager mDeviceManager;

    public void onCreate(Context c, DeviceManager dm) {
        mDeviceManager = dm;

        WifiManager wifi;
        wifi = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        ml = wifi.createMulticastLock("lifx_samples_tag");
        ml.acquire();
    }


    public void onDestroy() {
        networkContext.disconnect();
        if (ml != null) {
            ml.release();
        }
    }
}
