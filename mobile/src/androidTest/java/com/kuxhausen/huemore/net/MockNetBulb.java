package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.BulbState.Effect;

public class MockNetBulb implements NetworkBulb {

  public BulbState mKnown = new BulbState();
  public BulbState mTarget = new BulbState();
  long mId = (long) (Math.random() * Integer.MAX_VALUE);

  @Override
  public void setState(BulbState state) {
    if (state != null) {
      mTarget = BulbState.merge(state, mTarget);
    }
  }

  @Override
  public BulbState getState(GetStateConfidence confidence) {
    BulbState result = new BulbState();
    switch (confidence) {
      case GUESS:
        BulbState guess = new BulbState();
        guess.setPercentBri(50);
        guess.setOn(true);
        guess.setAlert(Alert.NONE);
        guess.setEffect(Effect.NONE);
        guess.setMiredCT(300);
        guess.setTransitionTime(BulbState.TRANSITION_TIME_DEFAULT);
        result = BulbState.merge(guess, result);
      case KNOWN:
        result = BulbState.merge(mKnown, result);
      case DESIRED:
        result = BulbState.merge(mTarget, result);
    }
    return result;
  }


  @Override
  public ConnectivityState getConnectivityState() {
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
}
