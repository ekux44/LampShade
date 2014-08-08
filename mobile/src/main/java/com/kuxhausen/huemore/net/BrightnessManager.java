package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

import java.util.List;

public class BrightnessManager {

  private BrightnessPolicy mPolicy;
  private Integer mVolumeBri;
  private List<NetworkBulb> mBulbs;

  public BrightnessManager(List<NetworkBulb> bulbs) {
    assert bulbs != null;
    assert !bulbs.isEmpty();
    mBulbs = bulbs;
    mPolicy = BrightnessPolicy.DIRECT_BRI;
  }

  public void setPolicy(BrightnessPolicy policy) {
    mPolicy = policy;

    //TODO?
  }

  public BrightnessPolicy getPolicy() {
    return mPolicy;
  }

  public void setState(NetworkBulb netBulb, BulbState targetState) {
    assert netBulb != null;
    assert targetState != null;
    assert mBulbs.contains(netBulb);

    //TODO
  }

  //Does not update lights
  public void setVolumeWithoutUpdate(int newVolume) {
    mVolumeBri = newVolume;
  }

  public void setBrightness(int brightness) {
    int newBrightness = Math.max(1, Math.min(100, brightness));

    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {

      if (mVolumeBri == null) {
        //calculate existing volume bri as brightest individual
        int briMax = 1;
        for (NetworkBulb bulb : mBulbs) {
          Integer physicalBri = bulb.getState(NetworkBulb.GetStateConfidence.KNOWN).getPercentBri();
          if (physicalBri != null && physicalBri > briMax) {
            briMax = physicalBri;
          }
        }
        mVolumeBri = briMax;
      }

      int oldVolume = mVolumeBri;
      int newVolume = newBrightness;
      for (NetworkBulb bulb : mBulbs) {
        Integer
            oldPhysicalBri =
            bulb.getState(NetworkBulb.GetStateConfidence.KNOWN).getPercentBri();
        if (oldPhysicalBri == null) {
          oldPhysicalBri = oldVolume;
        }

        oldPhysicalBri = Math.min(oldPhysicalBri, oldVolume);
        int newPhysicalBri = (oldPhysicalBri * newVolume) / oldVolume;

        BulbState bs = new BulbState();
        bs.setPercentBri(newPhysicalBri);
        bs.setTransitiontimeNone();

        bulb.setState(bs);
      }
      mVolumeBri = newVolume;

    } else {
      BulbState bs = new BulbState();
      bs.setPercentBri(newBrightness);
      bs.setTransitiontimeNone();

      for (NetworkBulb bulb : mBulbs) {
        bulb.setState(bs);
      }
    }
  }

  public int getBrightness() {
    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {
      if (mVolumeBri == null) {
        //calculate existing volume bri as brightest individual
        int briMax = 1;
        for (NetworkBulb bulb : mBulbs) {
          Integer physicalBri = bulb.getState(NetworkBulb.GetStateConfidence.KNOWN).getPercentBri();
          if (physicalBri != null && physicalBri > briMax) {
            briMax = physicalBri;
          }
        }
        return briMax;
      } else {
        return mVolumeBri;
      }
    } else {

      int briSum = 0;
      int briNum = 0;
      for (NetworkBulb bulb : mBulbs) {
        briSum += bulb.getState(NetworkBulb.GetStateConfidence.GUESS).getPercentBri();
        briNum++;
      }
      return briSum / briNum;
    }
  }

  public enum BrightnessPolicy {
    DIRECT_BRI, VOLUME_BRI
  }
}
