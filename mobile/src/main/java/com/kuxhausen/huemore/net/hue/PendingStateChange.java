package com.kuxhausen.huemore.net.hue;

import com.kuxhausen.huemore.state.BulbState;

public class PendingStateChange {
  public BulbState sentState;
  public HueBulb hubBulb;

  public PendingStateChange(BulbState bState, HueBulb hBulb) {
    sentState = bState;
    hubBulb = hBulb;
  }
}
