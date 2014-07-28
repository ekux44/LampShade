package com.kuxhausen.huemore.automation;

public class LegacyGMB {

  public LegacyGMB() {
  }

  public String group;
  public String mood;

  /**
   * cannot change due to json serialization everywhere
   * may be null in serialization *
   * out of 255
   */
  public Integer brightness;

  public Integer getPercentBrightness(){
    if(brightness==null)
      return null;
    else
      return (int)(brightness/2.55f);
  }
}
