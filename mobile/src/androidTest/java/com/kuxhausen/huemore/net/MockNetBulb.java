package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

public class MockNetBulb implements NetworkBulb{

  @Override
  public ConnectivityState getConnectivityState() {
    return null;
  }

  @Override
  public void setState(BulbState bs, boolean broadcast) {

  }

  @Override
  public BulbState getState(boolean guessIfUnknown) {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void rename(String name) {

  }

  @Override
  public Long getBaseId() {
    return null;
  }

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  @Override
  public Integer getMaxBrightness(boolean guessIfUnknown) {
    return null;
  }

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  @Override
  public Integer getCurrentBrightness(boolean guessIfUnknown) {
    return null;
  }

  @Override
  public void setBrightness(Integer desiredMaxBrightness, Integer desiredCurrentBrightness) {

  }

  @Override
  public boolean isMaxBriModeEnabled() {
    return false;
  }
}
