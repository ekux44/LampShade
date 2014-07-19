package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

public interface NetworkBulb {

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

  public abstract void setState(BulbState bs);

  public abstract BulbState getState();

  public abstract String getName();

  public abstract void rename(String name);

  public abstract Long getBaseId();

  /** @result 1-100 */
  public abstract int getMaxBrightness();

  /** @param maxBri 1-100 */
  public abstract void setMaxBrightness(int maxBri);

  /** @result 1-100 */
  public abstract int getCurrentBrightness();

  /** @param bri 1-100 */
  public abstract void setCurrentBrightness(int bri);

  /** @param enabled if true, device's physical_brightness = current_bri * (max_bri/100) **/
  public abstract void enableMaxBriMode(boolean enabled);

  public abstract boolean isMaxBriModeEnabled();
}
