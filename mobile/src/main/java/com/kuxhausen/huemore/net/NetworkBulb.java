package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

public interface NetworkBulb {

  public enum ConnectivityState {
    Unknown, Unreachable, Connected
  }

  public enum GetStateConfidence {
    GUESS, KNOWN, DESIRED
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

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  public abstract Integer getMaxBrightness(boolean guessIfUnknown);

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  public abstract Integer getCurrentBrightness(boolean guessIfUnknown);

  /*
   * will always broadcast
   */
  public abstract void setBrightness(Integer desiredMaxBrightness,
                                     Integer desiredCurrentBrightness);

  /*
   * @param enabled if true, device's physical_brightness = current_bri * (max_bri/100) *
   */
  public abstract boolean isMaxBriModeEnabled();


  public abstract void setState(BulbState state);

  public abstract BulbState getState(GetStateConfidence confidence);
}
