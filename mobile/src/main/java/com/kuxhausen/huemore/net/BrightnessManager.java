package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

import java.util.List;

public class BrightnessManager {

  private BrightnessPolicy mPolicy;
  private Integer mCurrentBri;
  private Integer mVolumeBri;
  private List<NetworkBulb> mBulbs;

  public BrightnessManager(List<NetworkBulb> bulbs) {
    assert bulbs != null;
    mBulbs = bulbs;
    mPolicy = BrightnessPolicy.DIRECT_BRI;
  }

  public void setPolicy(BrightnessPolicy policy) {
    mPolicy = policy;

    //TODO?
  }

  public BrightnessPolicy getPolicy(){
    return mPolicy;
  }

  public void setState(NetworkBulb netBulb, BulbState targetState) {
    assert netBulb != null;
    assert targetState != null;
    assert mBulbs.contains(netBulb);

    //TODO
  }

  public void setBrightness(int brightness) {
    //TODO
  }

  public int getBrightness(){
    return -1;
    //TODO
  }

  public enum BrightnessPolicy {
    DIRECT_BRI, VOLUME_BRI
  }
}
