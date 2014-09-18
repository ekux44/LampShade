package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.BulbState;

import java.util.List;

public class BrightnessManager {

  private BrightnessPolicy mPolicy;
  private Integer mVolumeBri;
  private List<NetworkBulb> mBulbs;

  public BrightnessManager(List<NetworkBulb> bulbs) {
    if (bulbs == null) {
      throw new IllegalArgumentException();
    }

    mBulbs = bulbs;
    mPolicy = BrightnessPolicy.DIRECT_BRI;
  }

  public void setPolicy(BrightnessPolicy policy) {
    if (policy == null) {
      throw new IllegalArgumentException();
    }

    mPolicy = policy;

    if (mPolicy == BrightnessPolicy.DIRECT_BRI) {
      mVolumeBri = null;
    }
  }

  public BrightnessPolicy getPolicy() {
    return mPolicy;
  }

  public BulbState getState(NetworkBulb netBulb, NetworkBulb.GetStateConfidence confidence) {
    if (netBulb == null || !mBulbs.contains(netBulb) || confidence == null) {
      throw new IllegalArgumentException();
    }

    BulbState adjusted = netBulb.getState(confidence).clone();
    if (mPolicy == BrightnessPolicy.VOLUME_BRI && adjusted.get255Bri() != null) {
      Integer volume = mVolumeBri;
      if (volume == null) {
        volume = getAveragePercentBrightness(mBulbs, NetworkBulb.GetStateConfidence.KNOWN);
      }
      adjusted.set255Bri(((int) Math.round(adjusted.get255Bri() * 100.0) / volume));
    }
    return adjusted;
  }

  public void setState(NetworkBulb netBulb, BulbState targetState) {
    if (netBulb == null || targetState == null || !mBulbs.contains(netBulb)) {
      throw new IllegalArgumentException();
    }

    BulbState adjusted = targetState.clone();

    //in volume bri mode, new color with unspecified bri is assumed to at 100% of current volume
    if (mPolicy == BrightnessPolicy.VOLUME_BRI && adjusted.get255Bri() == null && (
        adjusted.getMiredCT() != null || adjusted.getXY() != null)) {
      adjusted.set255Bri(255);
    }

    if (mPolicy == BrightnessPolicy.VOLUME_BRI && adjusted.get255Bri() != null) {
      if (mVolumeBri == null) {
        //calculate existing volume bri as average bri
        mVolumeBri = getAveragePercentBrightness(mBulbs, NetworkBulb.GetStateConfidence.KNOWN);
      }
      adjusted.set255Bri((int) Math.round((mVolumeBri * adjusted.get255Bri()) / 100.0));
    }
    netBulb.setState(adjusted);
  }

  //Does not update lights, only valid in volume mode
  public void setVolumeWithoutUpdate(int newVolume) {
    if (mPolicy != BrightnessPolicy.VOLUME_BRI) {
      throw new IllegalStateException();
    }

    mVolumeBri = newVolume;
  }

  public void setBrightness(int brightness) {
    int newBrightness = Math.max(1, Math.min(100, brightness));

    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {

      if (mVolumeBri == null) {
        //calculate existing volume bri as average brightness
        mVolumeBri = getAveragePercentBrightness(mBulbs, NetworkBulb.GetStateConfidence.KNOWN);
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
        bs.setTransitionTime(BulbState.TRANSITION_TIME_BRIGHTNESS_BAR);

        bulb.setState(bs);
      }
      mVolumeBri = newVolume;

    } else {
      BulbState bs = new BulbState();
      bs.setPercentBri(newBrightness);
      bs.setTransitionTime(BulbState.TRANSITION_TIME_BRIGHTNESS_BAR);

      for (NetworkBulb bulb : mBulbs) {
        bulb.setState(bs);
      }
    }
  }

  public int getBrightness() {
    if (mPolicy == BrightnessPolicy.VOLUME_BRI) {
      if (mVolumeBri == null) {
        //calculate existing volume bri as average brightness
        return getAveragePercentBrightness(mBulbs, NetworkBulb.GetStateConfidence.KNOWN);
      } else {
        return mVolumeBri;
      }
    } else {
      return getAveragePercentBrightness(mBulbs, NetworkBulb.GetStateConfidence.GUESS);
    }
  }

  //calculate the largest brightness among the group, returning 1 if no bulbs are sufficiently confident of brightness
  private static int getLargestPercentBrightness(List<NetworkBulb> list) {
    if (list == null) {
      throw new IllegalArgumentException();
    }

    int briMax = 1;
    for (NetworkBulb bulb : list) {
      Integer physicalBri = bulb.getState(NetworkBulb.GetStateConfidence.KNOWN).getPercentBri();
      if (physicalBri != null && physicalBri > briMax) {
        briMax = physicalBri;
      }
    }
    return briMax;
  }

  //calculate the average brightness among the group, returning 1 if no bulbs are sufficiently confident of brightness
  private static int getAveragePercentBrightness(List<NetworkBulb> list,
                                                 NetworkBulb.GetStateConfidence confidence) {
    if (list == null) {
      throw new IllegalArgumentException();
    }

    int briSum = 0;
    int briNum = 0;
    for (NetworkBulb bulb : list) {
      Integer physicalBri = bulb.getState(confidence).getPercentBri();
      if (physicalBri != null) {
        briSum += physicalBri;
        briNum++;
      }
    }

    if (briNum == 0) {
      return 1;
    }
    return briSum / briNum;
  }

  public enum BrightnessPolicy {
    DIRECT_BRI, VOLUME_BRI
  }
}
