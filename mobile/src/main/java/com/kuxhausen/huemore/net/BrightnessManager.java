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
    assert policy != null;

    mPolicy = policy;

    if (mPolicy == BrightnessPolicy.DIRECT_BRI) {
      mVolumeBri = null;
    }
  }

  public BrightnessPolicy getPolicy() {
    return mPolicy;
  }

  public BulbState getState(NetworkBulb netBulb, NetworkBulb.GetStateConfidence confidence) {
    assert netBulb != null;
    assert mBulbs.contains(netBulb);

    BulbState adjusted = netBulb.getState(confidence).clone();
    if (mPolicy == BrightnessPolicy.VOLUME_BRI && adjusted.get255Bri() != null) {
      Integer volume = mVolumeBri;
      if (volume == null) {
        volume = getLargestPercentBrightness(mBulbs);
      }
      adjusted.set255Bri(((int) Math.round(adjusted.get255Bri() * 100.0) / volume));
    }
    return adjusted;
  }

  public void setState(NetworkBulb netBulb, BulbState targetState) {
    assert netBulb != null;
    assert targetState != null;
    assert mBulbs.contains(netBulb);

    BulbState adjusted = targetState.clone();
    if (mPolicy == BrightnessPolicy.VOLUME_BRI && adjusted.get255Bri() != null) {
      if (mVolumeBri == null) {
        //calculate existing volume bri as brightest individual
        mVolumeBri = getLargestPercentBrightness(mBulbs);
      }
      adjusted.set255Bri((int) Math.round((mVolumeBri * adjusted.get255Bri()) / 100.0));
    }
    netBulb.setState(adjusted);
  }

  //Does not update lights, only valid in volume mode
  public void setVolumeWithoutUpdate(int newVolume) {
    assert mPolicy == BrightnessPolicy.VOLUME_BRI;
    mVolumeBri = newVolume;
  }

  public void setBrightness(int brightness) {
    int newBrightness = Math.max(1, Math.min(100, brightness));

    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {

      if (mVolumeBri == null) {
        //calculate existing volume bri as brightest individual
        mVolumeBri = getLargestPercentBrightness(mBulbs);
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
        bs.setTransitionTime(BulbState.TRANSITION_TIME_NONE);

        bulb.setState(bs);
      }
      mVolumeBri = newVolume;

    } else {
      BulbState bs = new BulbState();
      bs.setPercentBri(newBrightness);
      bs.setTransitionTime(BulbState.TRANSITION_TIME_NONE);

      for (NetworkBulb bulb : mBulbs) {
        bulb.setState(bs);
      }
    }
  }

  public int getBrightness() {
    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {
      if (mVolumeBri == null) {
        //calculate existing volume bri as brightest individual
        return getLargestPercentBrightness(mBulbs);
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

  //calculate the largest brightness among the group
  private static int getLargestPercentBrightness(List<NetworkBulb> list) {
    int briMax = 1;
    for (NetworkBulb bulb : list) {
      Integer physicalBri = bulb.getState(NetworkBulb.GetStateConfidence.KNOWN).getPercentBri();
      if (physicalBri != null && physicalBri > briMax) {
        briMax = physicalBri;
      }
    }
    return briMax;
  }

  public enum BrightnessPolicy {
    DIRECT_BRI, VOLUME_BRI
  }
}
