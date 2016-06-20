package com.kuxhausen.huemore.net.hue;

import android.support.annotation.Nullable;

import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;

public class HueUtils {

  enum Gamut {
    /**
     * Color light. Supports: on, transitiontime, alert, bri, effect, xy
     */
    COLOR_A,
    /**
     * Extended color light. Supports: on, transitiontime, alert, bri, ct, effect, xy
     */
    COLOR_B,
    /**
     * Extended color light. Supports: on, transitiontime, alert, bri, ct, effect, xy
     */
    COLOR_C,
    /**
     * Color temperature light. Supports: on, transitiontime, alert, bri, ct
     */
    COLOR_TEMP,
    /**
     * Dimmable light. Supports: on, transitiontime, alert, bri
     */
    DIMMABLE,
    UNKNOWN
  }

  private static Gamut getGamut(@Nullable String modelid) {
    if (modelid == null) {
      return Gamut.UNKNOWN;
    }

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
        return Gamut.COLOR_B;
      case "LST001":
      case "LLC010":
      case "LLC011":
      case "LLC012":
      case "LLC006":
      case "LLC007":
      case "LLC013":
        return Gamut.COLOR_A;
      case "LLC020":
      case "LST002":
        return Gamut.COLOR_C;
      case "LLM010":
      case "LLM011":
      case "LLM012":
      case "HML001":
      case "HML002":
      case "HML003":
      case "HML007":
        return Gamut.COLOR_TEMP;
      case "LWB004":
      case "LWB006":
      case "LWB007":
        return Gamut.DIMMABLE;
      default:
        return Gamut.UNKNOWN;
    }
  }

  /**
   * @return cie xy bounds for the gammut. Ordered: [xRed, yRed, xGreen, yGreen, xBlue, yBlue].
   */
  private static float[] getBounds(Gamut gammut) {
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

  public static BulbState toReachableState(HueBulbData bulbData, BulbState target) {
    BulbState adjusted = target.clone();
    Gamut gamut = getGamut(bulbData.modelid);

    switch (gamut) {
      case COLOR_A:
        if (target.hasXY()) {
          adjusted.setXY(Utils.toReachableXY(getBounds(gamut), target.getXY()));
        } else if (target.hasCT()) {
          adjusted.setXY(Utils.ctTOxy(target.getMiredCT()));
          adjusted.setMiredCT(null);
        }
        adjusted.setEffect(null);
      case COLOR_B:
        if (target.hasXY()) {
          adjusted.setXY(Utils.toReachableXY(getBounds(gamut), target.getXY()));
        }
      case COLOR_C:
        if (target.hasXY()) {
          adjusted.setXY(Utils.toReachableXY(getBounds(gamut), target.getXY()));
        }
        break;
      case COLOR_TEMP:
        adjusted.setXY(null); // TODO opportunistically convert from XY to CT when no CT specified
        adjusted.setEffect(null);
        break;
      case DIMMABLE:
        adjusted.setXY(null);
        adjusted.setEffect(null);
        adjusted.setMiredCT(null);
        adjusted.setAlert(null);
        break;
    }

    return adjusted;
  }

  /**
   * @return The number of ZigBee commands that will be required to send all of the state parameters
   * to a single bulb in the hue network. Note: ZigBee can do at most 1 command per 40ms.
   */
  public static int countZibBeeCommandsRequired(BulbState state) {
    int result = 0;
    if (state.hasOn()) {
      result += 1;
    }
    if (state.hasBri()) {
      result += 1;
    }
    if (state.hasXY()) {
      result += 1;
    }
    if (state.hasCT()) {
      result += 1;
    }
    if (state.hasAlert()) {
      result += 2; // Hue docs don't cover this, so play conservative here
    }
    if (state.hasEffect()) {
      result += 2; // Hue docs don't cover this, so play conservative here
    }
    if (state.hasTransitionTime()) {
      result += 0; // Hue docs have a missing footnote about this, so probably need further testing
    }
    return result;
  }
}
