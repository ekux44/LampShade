package com.kuxhausen.huemore.net.dev;

import android.support.v4.util.Pair;

import com.kuxhausen.huemore.state.BulbState;

import java.util.ArrayList;

/**
 * For encapsulating light state data associated with devices
 */
public class StateMessage {

  private ArrayList<Pair<BulbState, ArrayList<Long>>> mMessage = new ArrayList<>();

  /**
   * @return List of (bulb state, list of id's of targeted devices)
   */
  public ArrayList<Pair<BulbState, ArrayList<Long>>> getMessage() {
    return mMessage;
  }

  public void addState(BulbState state, ArrayList<Long> targetDeviceIds) {
    mMessage.add(Pair.create(state, targetDeviceIds));
  }
}
