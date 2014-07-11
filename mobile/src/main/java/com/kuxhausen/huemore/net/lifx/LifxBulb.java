package com.kuxhausen.huemore.net.lifx;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.state.BulbState;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;

public class LifxBulb implements NetworkBulb, LFXLight.LFXLightListener {

    LFXLight mLight;

    @Override
    public ConnectivityState getConnectivityState() {
        return ConnectivityState.Unknown;
    }

    @Override
    public void setState(BulbState bs) {
        if (mLight != null && bs != null) {
            if (bs.on != null) {
                if (bs.on) {
                    mLight.setPowerState(LFXTypes.LFXPowerState.ON);
                } else {
                    mLight.setPowerState(LFXTypes.LFXPowerState.OFF);
                }
            }
        }
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
        if (mLight != null)
            mLight.setLabel(name);
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

  public static class ExtraData{

  }
}
