package com.kuxhausen.huemore.net.dev;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.ref.WeakReference;

/**
 * Sample implementation of a service that represents a lighting device type to the device manager
 */
public class SampleDeviceService extends Service {

  /**
   * When bound to the DeviceManager, keep track of the DeviceManager's messenger
   */
  Messenger mDeviceManagerMessenger;

  int mBrightness = 0;

  /**
   * Target we publish for the DeviceManager to send messages to our IncomingHandler.
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler(new WeakReference<>(this)));

  @Override
  public void onCreate() {
    DevLogger.debugLog("SampleDevice: onCreate");
  }

  @Override
  public void onDestroy() {
    DevLogger.debugLog("SampleDevice: onDestroy");
  }

  /**
   * When binding to this service, return an interface to our messenger for sending us messages
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }

  /**
   * Handles incoming messages from DeviceManager
   */
  private static class IncomingHandler extends Handler {

    WeakReference<SampleDeviceService> mManagerWeakReference;

    public IncomingHandler(WeakReference<SampleDeviceService> managerWeakReference) {
      mManagerWeakReference = managerWeakReference;
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case ExperimentalDeviceManager.MSG_REGISTER_CLIENT:
          DevLogger.debugLog("SampleDeviceRecieved: registerClient");

          if (mManagerWeakReference.get().mDeviceManagerMessenger != null) {
            // Something bad has happened, clear out any state from the old connection
            // TODO ensure MSG_REGISTER_CLIENT always arrived first

          }
          mManagerWeakReference.get().mDeviceManagerMessenger = msg.replyTo;

          if (DevLogger.NET_DEBUG) {
            // When debugging, generate some test messages
            new NetExerciser().execute(msg.replyTo, mManagerWeakReference.get().mMessenger);
          }
          break;
        case ExperimentalDeviceManager.MSG_UNREGISTER_CLIENT:
          DevLogger.debugLog("SampleDeviceRecieved: unregisterClient");
          mManagerWeakReference.get().mDeviceManagerMessenger = null;
          break;
        case ExperimentalDeviceManager.MSG_SET_BRIGHTNESS:
          DevLogger.debugLog("SampleDeviceRecieved: " + msg.arg1);
          mManagerWeakReference.get().mBrightness = msg.arg1;
          try {
            mManagerWeakReference.get().mDeviceManagerMessenger.send(
                Message.obtain(null, ExperimentalDeviceManager.MSG_ACK_BRIGHTNESS, msg.arg1, 0));
          } catch (RemoteException e) {
            // The client is dead.  Remove references to it;
            mManagerWeakReference.get().mDeviceManagerMessenger = null;
          }
          if (DevLogger.NET_DEBUG) {
            NetExerciser.sleep(5 * Math.random());
          }
          break;
        case ExperimentalDeviceManager.MSG_ACK_BRIGHTNESS:
          DevLogger.debugLog("SampleDeviceRecieved: ack");
        default:
          super.handleMessage(msg);
      }
    }
  }
}
