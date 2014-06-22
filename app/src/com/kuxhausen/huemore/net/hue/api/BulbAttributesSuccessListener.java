package com.kuxhausen.huemore.net.hue.api;

import com.kuxhausen.huemore.net.hue.Route;


public class BulbAttributesSuccessListener extends BasicSuccessListener<BulbAttributes> {

  private final int bulbNum;

  public interface OnBulbAttributesReturnedListener {
    public void onAttributesReturned(BulbAttributes result, int bulbNumber);
  }


  private final OnBulbAttributesReturnedListener listener;

  public BulbAttributesSuccessListener(ConnectionMonitor parrentA,
      OnBulbAttributesReturnedListener l, int bNum, Route r) {
    super(parrentA, r);
    listener = l;
    bulbNum = bNum;
  }

  @Override
  public void onResponse(BulbAttributes response) {
    super.onResponse(response);

    if (listener != null)
      listener.onAttributesReturned(response, bulbNum);
  }
}
