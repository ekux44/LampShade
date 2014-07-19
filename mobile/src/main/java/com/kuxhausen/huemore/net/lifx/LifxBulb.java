package com.kuxhausen.huemore.net.lifx;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;

import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;

public class LifxBulb implements NetworkBulb, LFXLight.LFXLightListener {

  //In milis
  private final static long TRANSMIT_TIMEOUT_TIME = 10000;
  public final static float BS_BRI_CONVERSION = 2.55f;

  LifxConnection mConnection;

  private Context mContext;

  private Long mBaseId;
  private String mName;
  private String mDeviceId;
  private ExtraData mExtraData;
  private int mMaxBri;

  private LFXLight mLight;
  private long mInitializedTime;

  private BulbState mDesiredState;
  // In SystemClock.elapsedRealtime();
  private Long mDesiredLastChanged;

  private boolean mMaxBriMode;

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

  protected void onInitialize(){
    mInitializedTime = SystemClock.elapsedRealtime();
  }

  public void lightConnected(LFXLight light) {
    mLight = light;
    mLight.addLightListener(this);

    if(!mDesiredState.isEmpty()){
      setState(mDesiredState);
      mDesiredState = new BulbState();
    }
  }

  public void lightDisconnected() {
    if(mLight!=null)
      mLight.removeLightListener(this);
    mLight = null;
  }


  @Override
  public ConnectivityState getConnectivityState() {
    if (mLight != null && mLight.getReachability()
        .equals(LFXTypes.LFXDeviceReachability.REACHABLE)) {
      return ConnectivityState.Connected;
    } else if(SystemClock.elapsedRealtime() > (mInitializedTime + this.TRANSMIT_TIMEOUT_TIME)){
      return ConnectivityState.Unreachable;
    }
    return ConnectivityState.Unknown;
  }

  @Override
  public void setState(BulbState bs) {
    Log.d("lifx", "setState but mLight?null " + (mLight == null));

    mDesiredLastChanged = SystemClock.elapsedRealtime();

    if (mLight != null && bs != null) {
      float brightness = mLight.getColor().getBrightness();
      if (bs.bri != null) {
        brightness = bs.bri / 255f;
      }
      //TODO apply any maxBri rules

      if (bs.on != null) {
        if (bs.on) {
          mLight.setPowerState(LFXTypes.LFXPowerState.ON);
        } else {
          mLight.setPowerState(LFXTypes.LFXPowerState.OFF);
        }
      }

      //Send full color, color temp, or just brightness
      if (bs.xy != null) {
        Float[] hs = Utils.xyTOhs(bs.xy);
        float lifxHue = 360 * hs[0];
        float lifxSat = hs[1];
        LFXHSBKColor newColor = LFXHSBKColor.getColor(lifxHue, lifxSat, brightness, 3500);
        mLight.setColor(newColor);
      } else if (bs.ct != null) {
        LFXHSBKColor newColor = LFXHSBKColor.getColor(0, 0, brightness, bs.getCtKelvin());
        mLight.setColor(newColor);
      } else if (bs.bri != null) {
        LFXHSBKColor
            newColor =
            LFXHSBKColor
                .getColor(mLight.getColor().getHue(), mLight.getColor().getSaturation(), brightness,
                          mLight.getColor().getKelvin());
        mLight.setColor(newColor);
      }

    } else {
      //cache for when light not connected yet
      mDesiredState.merge(bs);
    }
  }

  protected boolean hasPendingWork(){
    if(mDesiredLastChanged!=null && (mDesiredLastChanged+this.TRANSMIT_TIMEOUT_TIME) > SystemClock.elapsedRealtime()){
      return true;
    }
    return false;
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
    return mBaseId;
  }

  @Override
  public int getCurrentMaxBrightness() {
    return mCurrentMaxBri;
  }

  @Override
  public void setCurrentMaxBrightness(int maxBri, boolean maxBriMode) {
    BulbState hack = new BulbState();
    hack.bri = (int) (2.55f * maxBri);
    if (maxBriMode) {
      this.mCurrentMaxBri = maxBri;
    }
    this.setState(hack);
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


  @Override
  public int getMaxBrightness() {
    return Math.max(1, Math.min(100, mMaxBri));
  }

  @Override
  public int getCurrentBrightness() {
    if(desiredState.bri!=null){
      int physicalBri = (int)(desiredState.bri / BS_BRI_CONVERSION);
      if(mMaxBriMode){
        return (int)(physicalBri / (getMaxBrightness()/100f));
      } else{
        return physicalBri;
      }
    } else{
      return 50;
    }
  }

  @Override
  public void setMaxBrightness(int newMaxBri) {
    newMaxBri = Math.max(1, Math.min(100, newMaxBri));

    if(desiredState.bri!=null) {
      //if there is an existing current brightness, recalculate it
      int currentBri = getCurrentBrightness();
      mMaxBri = newMaxBri;
      setCurrentBrightness(currentBri);
    } else {
      mMaxBri = newMaxBri;
    }
  }

  public void setCurrentBrightness(int newPercentBri){
    newPercentBri = Math.max(1, Math.min(100, newPercentBri));

    int desiredBulbStateBri;
    if(mMaxBriMode){
      desiredBulbStateBri = (int)((newPercentBri*BS_BRI_CONVERSION)*(getMaxBrightness()/100f));
    } else {
      desiredBulbStateBri = (int)(newPercentBri*BS_BRI_CONVERSION);
    }

    if(desiredState.bri == null || desiredState.bri!=desiredBulbStateBri){
      desiredState.bri = desiredBulbStateBri;
      mConnection.getLooper().addToQueue(this);
    }
  }

  public boolean isMaxBriModeEnabled(){
    return mMaxBriMode;
  }

  public void enableMaxBriMode(boolean enabled){
    mMaxBriMode = true;
  }
}
