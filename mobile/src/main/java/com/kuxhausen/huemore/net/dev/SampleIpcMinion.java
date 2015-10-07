package com.kuxhausen.huemore.net.dev;

/**
 * Sample implementation of a service to represent a device driver in bound Inter-Process
 * Communication with the device manager
 */
public class SampleIpcMinion extends IpcMinion {

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
