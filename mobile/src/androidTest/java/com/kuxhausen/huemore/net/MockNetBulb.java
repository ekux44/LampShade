package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

public class MockNetBulb implements NetworkBulb {

  public BulbState mKnown = new BulbState();
  public BulbState mTarget = new BulbState();
  long mId = (long) (Math.random() * Integer.MAX_VALUE);

  @Override
  public void setState(BulbState state) {
    if (state != null) {
      mTarget = state.clone();
    }
  }

  @Override
  public BulbState getState(GetStateConfidence confidence) {
    BulbState result = new BulbState();
    switch (confidence) {
      case GUESS:
        result.setPercentBri(50);
        result.on = true;
        result.alert = "none";
        result.effect = "none";
        result.ct = 300;
        result.transitiontime = 4;
      case KNOWN:
        result.merge(mKnown);
      case DESIRED:
        result.merge(mTarget);
    }
    return result;
  }


  @Override
  public ConnectivityState getConnectivityState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setState(BulbState bs, boolean broadcast) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BulbState getState(boolean guessIfUnknown) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void rename(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long getBaseId() {
    return mId;
  }

  //TODO everything beneith this is deprecated?

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  @Override
  public Integer getMaxBrightness(boolean guessIfUnknown) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param guessIfUnknown will guess value instead of returning null if unknown
   * @result 1-100
   */
  @Override
  public Integer getCurrentBrightness(boolean guessIfUnknown) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrightness(Integer desiredMaxBrightness, Integer desiredCurrentBrightness) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMaxBriModeEnabled() {
    throw new UnsupportedOperationException();
  }
}
