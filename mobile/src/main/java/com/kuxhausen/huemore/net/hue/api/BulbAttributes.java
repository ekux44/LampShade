package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.state.BulbState;

public class BulbAttributes {

  public BulbState state;
  public String type;
  public String name;
  public String modelid;
  public String swversion;
  /**
   * Requires hub firmware 1.4
   */
  public String uniqueid;
  /**
   * Requires hub firmware 1.7
   */
  public String manufacturername;
  /**
   * Requires hub firmware 1.9
   */
  public String luminaireuniqueid;
  /**
   * Internal LampShade field, bulb's number as assigned by the hub
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
}
