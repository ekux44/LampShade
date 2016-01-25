package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.HueBulbData;
import com.kuxhausen.huemore.state.BulbState;

public class BulbAttributes {

  public BulbState state;
  public String type;
  public String name;
  public String modelid;
  public String swversion;
  /**
   * @since hub firmware 1.4
   */
  public String uniqueid;
  /**
   * @since hub firmware 1.7
   */
  public String manufacturername;
  /**
   * @since hub firmware 1.9
   */
  public String luminaireuniqueid;
  /**
   * Internal LampShade field. Bulb's number as assigned by the hub.
   */
  public String number;

  public BulbAttributes() {
  }

  /**
   * @return A subset of this object with only the fields accepted by the "set light attributes" hub
   * api endpoint
   */
  public BulbAttributes getSettableAttributes() {
    BulbAttributes settableAttributes = new BulbAttributes();
    settableAttributes.name = this.name;
    return settableAttributes;
  }

  public HueBulbData getHueBulbData() {
    HueBulbData data = new HueBulbData();
    data.type = this.type;
    data.modelid = this.modelid;
    data.swversion = this.swversion;
    data.uniqueid = this.uniqueid;
    data.manufacturername = this.manufacturername;
    data.luminaireuniqueid = this.luminaireuniqueid;
    return data;
  }
}
