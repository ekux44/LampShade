package com.kuxhausen.huemore.state;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class BulbState {

  public enum Alert {
    @SerializedName("none")
    NONE,
    @SerializedName("select")
    FLASH_ONCE,
    @SerializedName("lselect")
    FLASH_30SEC
  }

  public enum Effect {
    @SerializedName("none")
    NONE,
    @SerializedName("colorloop")
    COLORLOOP
  }

  public final static int TRANSITION_TIME_DEFAULT = 4, TRANSITION_TIME_NONE = 0,
      TRANSITION_TIME_BRIGHTNESS_BAR = 2;

  /**
   * On/Off state of the light. On=true, Off=false
   */
  private Boolean on;
  /**
   * The brightness value to set the light to. Brightness is a scale from 0 (the minimum the light
   * is capable of) to 255 (the maximum). Note: a brightness of 0 is not off.
   */
  private Integer bri;
  /**
   * The x and y coordinates of a color in CIE color space. The first entry is the x coordinate and
   * the second entry is the y coordinate. Both x and y must be between 0 and 1. If the specified
   * coordinates are not in the CIE color space, the closest color to the coordinates will be
   * chosen.
   */
  public Float[] xy;
  /**
   * The Mired Color temperature of the light. 2012 connected lights are capable of 153 (6500K) to
   * 500 (2000K).
   */
  private Integer ct;

  /**
   * The alert effect, is a temporary change to the bulb�s state, and has one of the following
   * values: �none� � The light is not performing an alert effect. �select� � The light is
   * performing one breathe cycle. �lselect� � The light is performing breathe cycles for 30 seconds
   * or until an "alert": "none" command is received.
   */
  private Alert alert;

  /**
   * The dynamic effect of the light, can either be "none" or "colorloop"
   */
  private Effect effect;

  /**
   * The duration of the transition from the light�s current state to the new state. This is given
   * as a multiple of 100ms and defaults to 4 (400ms). For example, setting transistiontime:10 will
   * make the transition last 1 second.
   */
  private Integer transitiontime;

  public BulbState() {
  }

  /**
   * Must ensure uniqueness for HueUrlEncoder
   */
  @Override
  public String toString() {
    String result = "";
    if (on != null) {
      result += "on:" + (on ? "true" : "false") + " ";
    }
    if (bri != null) {
      result += "bri:" + bri + " ";
    }
    if (xy != null) {
      result += "xy:" + xy[0] + " " + xy[1] + " ";
    }
    if (ct != null) {
      result += "ct:" + ct + " ";
    }
    if (alert != null) {
      result += "alert:" + alert + " ";
    }
    if (effect != null) {
      result += "effect:" + effect + " ";
    }
    if (transitiontime != null) {
      result += "transitiontime:" + transitiontime + " ";
    }
    return result;
  }

  /**
   * when in doubt, override, only allow 1 of three color modes *
   */
  public void merge(BulbState other) {
    on = (other.on != null) ? other.on : on;
    bri = (other.bri != null) ? other.bri : bri;
    alert = (other.alert != null) ? other.alert : alert;
    effect = (other.effect != null) ? other.effect : effect;
    transitiontime = (other.transitiontime != null) ? other.transitiontime : transitiontime;

    if (other.xy != null) {
      xy = other.xy;
      ct = null;
    } else if (other.ct != null) {
      xy = null;
      ct = other.ct;
    }
  }

  /**
   * return a BulbState with the values of o when the differ from this object
   */
  public BulbState delta(BulbState o) {
    BulbState delta = new BulbState();

    if (o.on != null && (on == null || !on.equals(o.on))) {
      delta.on = o.on;
    }
    if (o.bri != null && (bri == null || !bri.equals(o.bri))) {
      delta.bri = o.bri;
    }
    if (o.alert != null && (alert == null || !alert.equals(o.alert))) {
      delta.alert = o.alert;
    }
    if (o.effect != null && (effect == null || !effect.equals(o.effect))) {
      delta.effect = o.effect;
    }
    if (o.transitiontime != null
        && (transitiontime == null || !transitiontime.equals(o.transitiontime))) {
      delta.transitiontime = o.transitiontime;
    }

    if (o.xy != null && (xy == null || !(xy[0].equals(o.xy[0]) && xy[1].equals(o.xy[1])))) {
      delta.xy = o.xy;
    } else if (o.ct != null && (ct == null || !ct.equals(o.ct))) {
      delta.ct = o.ct;
    }
    return delta;
  }

  /**
   * update confirmed with whatever the nontransient result of that change state is
   */
  public static void confirmChange(BulbState confirmed, BulbState change) {
    if (change.on != null) {
      confirmed.on = change.on;
    }
    if (change.bri != null) {
      confirmed.bri = change.bri;
    }

    // ignore alert for now
    // TODO deal with alert
    if (change.alert != null) {
      confirmed.alert = Alert.NONE;
    }

    if (change.effect != null) {
      confirmed.effect = change.effect;
    }

    // ignore transitiontime for now
    // TODO deal with transitiontime
    if (change.transitiontime != null) {
      confirmed.transitiontime = 4;
    }

    // TODO convert between colormoods instead of filling nulls
    if (change.xy != null) {
      confirmed.xy = change.xy;
      confirmed.ct = null;
    } else if (change.ct != null) {
      confirmed.xy = null;
      confirmed.ct = change.ct;
    }

  }

  public BulbState clone() {
    Gson gson = new Gson();
    try {
      return gson.fromJson(gson.toJson(this), BulbState.class);
    } catch (Exception e) {
      return new BulbState();
    }
  }

  @Override
  public boolean equals(Object obj) {
    assert (obj != null);
    assert (obj instanceof BulbState);
    BulbState other = (BulbState) obj;

    if (this.getOn() != null ^ other.getOn() != null) {
      return false;
    }
    if (this.getOn() != null && other.getOn() != null && !this.getOn().equals(other.getOn())) {
      return false;
    }

    if (this.get255Bri() != null ^ other.get255Bri() != null) {
      return false;
    }
    if (this.get255Bri() != null && other.get255Bri() != null && !this.get255Bri()
        .equals(other.get255Bri())) {
      return false;
    }

    if (this.getTransitionTime() != null ^ other.getTransitionTime() != null) {
      return false;
    }
    if (this.getTransitionTime() != null && other.getTransitionTime() != null && !this
        .getTransitionTime().equals(other.getTransitionTime())) {
      return false;
    }

    if (this.getAlert() != null ^ other.getAlert() != null) {
      return false;
    }
    if (this.getAlert() != null && other.getAlert() != null && !this.getAlert()
        .equals(other.getAlert())) {
      return false;
    }

    if (this.getEffect() != null ^ other.getEffect() != null) {
      return false;
    }
    if (this.getEffect() != null && other.getEffect() != null && !this.getEffect()
        .equals(other.getEffect())) {
      return false;
    }

    if (this.getMiredCT() != null ^ other.getMiredCT() != null) {
      return false;
    }
    if (this.getMiredCT() != null && other.getMiredCT() != null && !this.getMiredCT()
        .equals(other.getMiredCT())) {
      return false;
    }

    if (this.xy != null ^ other.xy != null) {
      return false;
    }
    if (this.xy != null && other.xy != null && !Arrays.equals(this.xy, other.xy)) {
      return false;
    }

    return true;
  }

  public boolean isEmpty() {
    if (on == null && bri == null && xy == null && ct == null
        && alert == null && effect == null && transitiontime == null) {
      return true;
    }
    return false;
  }

  public Boolean getOn() {
    return on;
  }

  public void setOn(Boolean newOn) {
    on = newOn;
  }

  public boolean hasOnlyBri() {
    if (on == null && bri != null && xy == null && ct == null
        && alert == null && effect == null && transitiontime == null) {
      return true;
    }
    return false;
  }

  public Integer getPercentBri() {
    if (bri == null) {
      return bri;
    } else {
      return Math.max(1, Math.min(255, (int) Math.round(bri / 2.55)));
    }
  }

  public void setPercentBri(Integer brightness) {
    if (brightness == null) {
      bri = null;
    } else {
      bri = Math.max(1, Math.min(255, (int) (brightness * 2.55)));
    }
  }

  public Integer get255Bri() {
    return bri;
  }

  public void set255Bri(Integer brightness) {
    if (brightness == null) {
      bri = null;
    } else {
      bri = Math.max(1, Math.min(255, brightness));
    }
  }

  public Integer getMiredCT() {
    return ct;
  }

  public void setMiredCT(Integer newCT) {
    if (newCT == null) {
      ct = newCT;
    } else {
      ct = Math.max(1, newCT);
    }
  }

  public Integer getKelvinCT() {
    if (ct == null) {
      return null;
    } else if (ct == 0) {
      return 1000000;
    } else {
      return (1000000 / ct);
    }
  }

  public void setKelvinCT(Integer newCT) {
    if (newCT == null) {
      ct = newCT;
    } else {
      ct = Math.max(1, (1000000 / Math.max(1, newCT)));
    }
  }

  public Effect getEffect() {
    return effect;
  }

  public void setEffect(Effect ef) {
    effect = ef;
  }

  public Alert getAlert() {
    return alert;
  }

  public void setAlert(Alert a) {
    alert = a;
  }

  public Integer getTransitionTime() {
    return transitiontime;
  }

  public void setTransitionTime(Integer timeInDeciSeconds) {
    if (timeInDeciSeconds == null) {
      transitiontime = timeInDeciSeconds;
    } else {
      transitiontime = Math.max(0, timeInDeciSeconds);
    }
  }

  public static BulbState merge(BulbState priority, BulbState secondary) {
    BulbState result = secondary.clone();
    result.merge(priority);
    return result;
  }
}
