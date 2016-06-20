package com.kuxhausen.huemore.net.lifx;

import android.content.Context;
import android.os.SystemClock;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.utils.DeferredLog;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;

public class LifxBulb implements NetworkBulb, LFXLight.LFXLightListener {

  //In milis
  private final static long TRANSMIT_TIMEOUT_TIME = 10000;

  LifxConnection mConnection;
  private Context mContext;

  private Long mBaseId;
  private String mName;
  private String mDeviceId;
  private ExtraData mExtraData;

  private LFXLight mLight;
  private long mInitializedTime;

  private BulbState mDesiredState;
  // In SystemClock.elapsedRealtime();
  private Long mDesiredLastChanged;

  private Integer mMaxBri;

  public LifxBulb(Context c, Long bulbBaseId, String bulbName,
                  String bulbDeviceId, ExtraData bulbData,
                  LifxConnection lifxConnection) {
    mBaseId = bulbBaseId;
    mName = bulbName;
    mDeviceId = bulbDeviceId;
    mExtraData = bulbData;

    mContext = c;
    mConnection = lifxConnection;
    mDesiredState = new BulbState();

  }

  protected void onInitialize() {
    mInitializedTime = SystemClock.elapsedRealtime();
  }

  public void lightConnected(LFXLight light) {
    mLight = light;
    mLight.addLightListener(this);

    if (!mDesiredState.isEmpty()) {
      setState(mDesiredState);
      mDesiredState = new BulbState();
    }
  }

  public void lightDisconnected() {
    if (mLight != null) {
      mLight.removeLightListener(this);
    }
    mLight = null;
  }


  @Override
  public ConnectivityState getConnectivityState() {
    if (mLight != null && mLight.getReachability()
        .equals(LFXTypes.LFXDeviceReachability.REACHABLE)) {
      return ConnectivityState.Connected;
    } else if (SystemClock.elapsedRealtime() > (mInitializedTime + this.TRANSMIT_TIMEOUT_TIME)) {
      return ConnectivityState.Unreachable;
    }
    return ConnectivityState.Unknown;
  }

  @Override
  public void setState(BulbState bs) {
    DeferredLog.d("lifx", "setState but mLight?null %b", (mLight == null));

    mDesiredLastChanged = SystemClock.elapsedRealtime();

    if (mLight != null && bs != null) {
      //TODO cache so don't have to guess when SDK dosn't know
      float lifxBrightness = .5f;
      float lifxHue = 0;
      float lifxSat = 0;
      int lifxCt = 3500;
      if (mLight.getColor() != null) {
        lifxBrightness = mLight.getColor().getBrightness();
        lifxHue = mLight.getColor().getHue();
        lifxSat = mLight.getColor().getSaturation();
        lifxCt = mLight.getColor().getKelvin();
      }

      if (bs.get255Bri() != null) {
        lifxBrightness = (bs.get255Bri() / 255f);
      }

      //clip brightness to ensure proper behavior (0 brightness not allowed)
      lifxBrightness = Math.max(.01f, lifxBrightness);

      if (bs.getOn() != null) {
        if (bs.getOn()) {
          mLight.setPowerState(LFXTypes.LFXPowerState.ON);
        } else {
          mLight.setPowerState(LFXTypes.LFXPowerState.OFF);
        }
      }

      //Send full color, color temp, or just brightness
      if (bs.hasXY() || bs.getKelvinCT() != null || bs.get255Bri() != null) {
        if (bs.hasXY()) {
          float[] hs = Utils.xyTOhs(bs.getXY());
          lifxHue = 360 * hs[0];
          lifxSat = hs[1];

          LFXHSBKColor newColor = LFXHSBKColor.getColor(lifxHue, lifxSat, lifxBrightness, 3500);
          mLight.setColor(newColor);

        } else if (bs.getKelvinCT() != null) {
          lifxHue = 0;
          lifxSat = 0;
          lifxCt = bs.getKelvinCT();

          LFXHSBKColor newColor = LFXHSBKColor.getColor(0, 0, lifxBrightness, lifxCt);
          mLight.setColor(newColor);
        }

        LFXHSBKColor newColor = LFXHSBKColor.getColor(lifxHue, lifxSat, lifxBrightness, lifxCt);

        if (bs.getTransitionTime() != null) {
          mLight.setColorOverDuration(newColor, bs.getTransitionTime() * 100);
        } else {
          mLight.setColor(newColor);
        }
      }

    } else {
      //cache for when light not connected yet
      mDesiredState.merge(bs);
    }

    //TODO move or limit to actual state changes
    this.mConnection.getDeviceManager().onStateChanged();
  }

  protected boolean hasPendingWork() {
    if (mDesiredLastChanged != null
        && (mDesiredLastChanged + this.TRANSMIT_TIMEOUT_TIME) > SystemClock.elapsedRealtime()) {
      return true;
    }
    return false;
  }


  @Override
  public BulbState getState(GetStateConfidence confidence) {
    BulbState result = new BulbState();
    switch (confidence) {
      case GUESS:
        BulbState guess = new BulbState();
        guess.setPercentBri(50);
        guess.setOn(true);
        guess.setAlert(BulbState.Alert.NONE);
        guess.setEffect(BulbState.Effect.NONE);
        guess.setMiredCT(300);
        guess.setTransitionTime(BulbState.TRANSITION_TIME_DEFAULT);
        result = BulbState.merge(guess, result);
      case KNOWN:
        if (mLight != null && mLight.getColor() != null) {
          LFXHSBKColor color = mLight.getColor();
          BulbState confirmed = new BulbState();
          confirmed.setOn(mLight.getPowerState() == LFXTypes.LFXPowerState.ON);
          confirmed.setPercentBri((int) (color.getBrightness() * 100.0));

          //TODO improve lifx color logic
          confirmed.setKelvinCT(color.getKelvin());
          float[] hs = {color.getHue() / 360.0f, color.getSaturation()};
          confirmed.setXY(Utils.hsTOxy(hs));

          result = BulbState.merge(confirmed, result);
        }
      case DESIRED:
        result = BulbState.merge(mDesiredState, result);
    }
    return result;
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
    return mBaseId;
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
