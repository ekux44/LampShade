package com.kuxhausen.huemore.net.hue;

public class HueUtils {

  enum Gamuts {
    COLOR_A, COLOR_B, COLOR_C, COLOR_TEMP, DIMMABLE, UNKNOWN
  }

  public Gamuts getGamut(String modelid) {
    switch (modelid) {
      case "LCT001":
      case "LCT007":
      case "LCT002":
      case "LCT003":
      case "LLM001":
      case "HBL001":
      case "HBL002":
      case "HBL003":
      case "HEL001":
      case "HEL002":
      case "HIL001":
      case "HIL002":
        return Gamuts.COLOR_B;
      case "LST001":
      case "LLC010":
      case "LLC011":
      case "LLC012":
      case "LLC006":
      case "LLC007":
      case "LLC013":
        return Gamuts.COLOR_A;
      case "LLC020":
      case "LST002":
        return Gamuts.COLOR_C;
      case "LWB004":
      case "LWB006":
      case "LWB007":
        return Gamuts.DIMMABLE;
      case "LLM010":
      case "LLM011":
      case "LLM012":
      case "HML001":
      case "HML002":
      case "HML003":
      case "HML007":
        return Gamuts.COLOR_TEMP;
      default:
        return Gamuts.UNKNOWN;
    }
  }

  /**
   * @return cie xy bounds for the gammut. Ordered: [xRed, yRed, xGreen, yGreen, xBlue, yBlue].
   */
  private float[] getBounds(Gamuts gammut) {
    switch (gammut) {
      case COLOR_A:
        return new float[]{0.704f, 0.296f, 0.2151f, 0.7106f, 0.138f, 0.08f};
      case COLOR_B:
        return new float[]{0.675f, 0.322f, 0.409f, 0.518f, 0.167f, 0.04f};
      case COLOR_C:
        return new float[]{0.692f, 0.308f, 0.17f, 0.7f, 0.153f, 0.048f};
      default:
        return new float[]{1f, 0f, 0f, 1f, 0f, 0f};
    }
  }

  public void clipToBounds(){
    //TODO
  }
}
