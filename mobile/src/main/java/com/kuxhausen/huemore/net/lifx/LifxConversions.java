package com.kuxhausen.huemore.net.lifx;

public class LifxConversions {

  /**
   * @param bri in 0-100
   * @return bri in 0.01 - 1.0
   */
  public static float dbBriToLifxBri(int bri) {
    bri = Math.max(1, Math.min(100, bri));
    return bri / 100f;
  }

}
