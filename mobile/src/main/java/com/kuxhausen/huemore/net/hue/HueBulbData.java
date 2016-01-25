package com.kuxhausen.huemore.net.hue;

import android.text.TextUtils;

/**
 * Contains {@link com.kuxhausen.huemore.net.hue.HueBulb} attributes not covered by the generic
 * columns in {@link com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns}. Persistent as
 * json.
 */
public class HueBulbData {

  public String type;
  public String modelid;
  public String swversion;
  public String uniqueid;
  public String manufacturername;
  public String luminaireuniqueid;

  public HueBulbData() {
  }

  /**
   * Simpler alternative to overriding .equals()
   */
  public boolean matches(HueBulbData other) {
    return TextUtils.equals(this.type, other.type)
           && TextUtils.equals(this.modelid, other.modelid)
           && TextUtils.equals(this.swversion, other.swversion)
           && TextUtils.equals(this.uniqueid, other.uniqueid)
           && TextUtils.equals(this.manufacturername, other.manufacturername)
           && TextUtils.equals(this.luminaireuniqueid, other.luminaireuniqueid);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(6);
    result.append(type);
    result.append(modelid);
    result.append(swversion);
    result.append(uniqueid);
    result.append(manufacturername);
    result.append(luminaireuniqueid);
    return result.toString();
  }
}
