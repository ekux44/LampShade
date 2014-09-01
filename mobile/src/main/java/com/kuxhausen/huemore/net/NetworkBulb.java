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

  public abstract String getName();

  public abstract void rename(String name);

  public abstract Long getBaseId();

  public abstract void setState(BulbState state);

  public abstract BulbState getState(GetStateConfidence confidence);
}
