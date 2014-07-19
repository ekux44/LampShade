package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

public abstract class NetworkBulb {

  public enum ConnectivityState {
    Unknown, Unreachable, Connected
  }

  interface ConnectionListener {

    public abstract void onConnectivityChanged(ConnectivityState netState);
  }

  interface StateListener {

    public abstract void onStateChanged(BulbState bulbState);
  }

  public abstract ConnectivityState getConnectivityState();

  public abstract void setState(BulbState bs, boolean broadcast);

  /*
   * returns current_bri (not physical_brightness)
   * @param guessIfUnknown will guess value instead of returning null if unknown
   */
  public abstract BulbState getState(boolean guessIfUnknown);

  public abstract String getName();

  public abstract void rename(String name);

  public abstract Long getBaseId();

  private Integer mMaxBri;
  private boolean mMaxBriEnabled;


  protected Integer getRawMaxBrightness() {
    if(mMaxBri!=null) {
      return Math.max(1, Math.min(100, mMaxBri));
    } else
      return null;
  }

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  public abstract Integer getMaxBrightness(boolean guessIfUnknown);


  /*
   * @param enabled if true, device's physical_brightness = current_bri * (max_bri/100) *
   */
  public boolean isMaxBriModeEnabled(){
    return mMaxBriEnabled;
  }

  /**
   *
   * @param enabled if true/false, set maxBriEnabled to value. If null, maxBri changes cause instantaneous stateChanges
   * @param maxBri 1-100, or null to ignore
   * @param broadcast
   */
  public void setMaxBrightness(Boolean enabled, Integer maxBri, boolean broadcast) {
    if (maxBri != null)
      maxBri = Math.max(1, Math.min(100, maxBri));

    if (enabled != null) {
      mMaxBri = maxBri;
      mMaxBriEnabled = enabled;

    } else if (enabled == null) {
      if (maxBri != null && this.isMaxBriModeEnabled()) {
        //this is probably a maxBri slider
        BulbState current = this.getState(false); // only bri needed here
        mMaxBri = maxBri;
        if (current.bri != null) {
          this.setState(current, broadcast);
        }
      }
    }
  }
}
