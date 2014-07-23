package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.Route;


public class BulbAttributesSuccessListener extends BasicSuccessListener<BulbAttributes> {

  private final String bulbHueId;

  public interface OnBulbAttributesReturnedListener {

    public void onAttributesReturned(BulbAttributes result, String bulbHueId);
  }


  private final OnBulbAttributesReturnedListener listener;

  public BulbAttributesSuccessListener(ConnectionMonitor parrentA,
                                       OnBulbAttributesReturnedListener l, String bHueId, Route r) {
    super(parrentA, r);
    listener = l;
    bulbHueId = bHueId;
  }

  @Override
  public void onResponse(BulbAttributes response) {
    super.onResponse(response);

    if (listener != null) {
      listener.onAttributesReturned(response, bulbHueId);
    }
  }
}
