package com.kuxhausen.huemore.net.dev;

import com.kuxhausen.huemore.net.DeviceDriver;

/**
 * Sample implementation of a service that represents a lighting device type to the device manager
 */
public class SampleDeviceService extends SimpleDeviceService {

  private SampleDeviceDriver mDeviceDriver;

  @Override
  public DeviceDriver getDeviceDriver() {
    if (mDeviceDriver == null) {
      mDeviceDriver = new SampleDeviceDriver();
      mDeviceDriver.initialize(this, this);
    }
    return mDeviceDriver;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
