package com.kuxhausen.huemore.net.lifx;

import android.content.Context;
import android.util.Log;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.state.BulbState;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;

public class LifxBulb implements NetworkBulb, LFXLight.LFXLightListener {

  LifxConnection mConnection;

  private Context mContext;

  private Long mBaseId;
  private String mName;
  private String mDeviceId;
  private ExtraData mExtraData;
  private int mCurrentMaxBri;

  private LFXLight mLight;

  public LifxBulb(Context c, Long bulbBaseId, String bulbName,
                  String bulbDeviceId, ExtraData bulbData,
                  LifxConnection lifxConnection, int currentMaxBri) {
    mBaseId = bulbBaseId;
    mName = bulbName;
    mDeviceId = bulbDeviceId;
    mExtraData = bulbData;
    mCurrentMaxBri = currentMaxBri;

    mContext = c;
    mConnection = lifxConnection;

  }

  public void lightConnected(LFXLight light) {
    mLight = light;
    mLight.addLightListener(this);
  }

  public void lightDisconnected() {
    mLight.removeLightListener(this);
    mLight = null;
  }


  @Override
  public ConnectivityState getConnectivityState() {
    if (mLight != null && mLight.getReachability()
        .equals(LFXTypes.LFXDeviceReachability.REACHABLE)) {
      return ConnectivityState.Connected;
    }
    //TODO finish
    return ConnectivityState.Unknown;
  }

  @Override
  public void setState(BulbState bs) {
    Log.d("lifx", "setState but mLight?null " + (mLight == null));

    if (mLight != null && bs != null) {
      if (bs.on != null) {
        if (bs.on) {
          mLight.setPowerState(LFXTypes.LFXPowerState.ON);
        } else {
          mLight.setPowerState(LFXTypes.LFXPowerState.OFF);
        }
      }
    }
    //TODO finish and add timed caching when unreachable
  }

  @Override
  public BulbState getState() {
    return new BulbState();
  }

  @Override
  public String getName() {
    if (mLight != null) {
      return mLight.getLabel();
    }
    return "";
  }

  @Override
  public void rename(String name) {
    if (mLight != null) {
      mLight.setLabel(name);
    }
  }

  @Override
  public Long getBaseId() {
    return -1l;
  }

  @Override
  public int getCurrentMaxBrightness() {
    return 0;
  }

  @Override
  public void setCurrentMaxBrightness(int maxBri, boolean maxBriMode) {

  }

  @Override
  public void lightDidChangeLabel(LFXLight light, String label) {

  }

  @Override
  public void lightDidChangeColor(LFXLight light, LFXHSBKColor color) {

  }

  @Override
  public void lightDidChangePowerState(LFXLight light, LFXTypes.LFXPowerState powerState) {

  }

  public static class ExtraData {

  }
}
