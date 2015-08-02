package com.kuxhausen.huemore.net.dev;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kuxhausen.huemore.net.DeviceDriver;
import com.kuxhausen.huemore.net.DeviceListener;

import java.lang.ref.WeakReference;

/**
 * Basic implementation of a service that represents a lighting device type to the device manager
 */
public abstract class SimpleDeviceService extends Service implements DeviceListener {

  /**
   * When bound to the DeviceManager, keep track of the DeviceManager's messenger
   */
  Messenger mDeviceManagerMessenger;

  int mBrightness = 0;

  /**
   * Target we publish for the DeviceManager to send messages to our IncomingHandler.
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler(new WeakReference<>(this)));

  public abstract DeviceDriver getDeviceDriver();

  @Override
  public void onCreate() {
    super.onCreate();
    DevLogger.debugLog("SimpleDevice: onCreate");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    DevLogger.debugLog("SimpleDevice: onDestroy");
  }

  /**
   * When binding to this service, return an interface to our messenger for sending us messages
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }

  @Override
  public void deviceStateChanged(StateMessage state) {
    if (mDeviceManagerMessenger != null) {
      try {
        mDeviceManagerMessenger.send(
            Message.obtain(null, ExperimentalDeviceManager.MSG_OBSERVED_STATEMESSAGE, state));
      } catch (RemoteException e) {
        // The client is dead.  Remove references to it;
        mDeviceManagerMessenger = null;
      }
    }
  }

  /**
   * Handles incoming messages from DeviceManager
   */
  private static class IncomingHandler extends Handler {

    WeakReference<SimpleDeviceService> mManagerWeakReference;

    public IncomingHandler(WeakReference<SimpleDeviceService> managerWeakReference) {
      mManagerWeakReference = managerWeakReference;
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case ExperimentalDeviceManager.MSG_REGISTER_CLIENT:
          DevLogger.debugLog("SimpleDeviceRecieved: registerClient");
          if (mManagerWeakReference.get().mDeviceManagerMessenger != null) {
            // Something bad has happened, clear out any state from the old connection
          }
          mManagerWeakReference.get().mDeviceManagerMessenger = msg.replyTo;

          if (DevLogger.NET_DEBUG) {
            // When debugging, generate some test messages
            new NetExerciser().execute(msg.replyTo, mManagerWeakReference.get().mMessenger);
          }
          break;
        case ExperimentalDeviceManager.MSG_UNREGISTER_CLIENT:
          DevLogger.debugLog("SimpleDeviceRecieved: unregisterClient");
          mManagerWeakReference.get().mDeviceManagerMessenger = null;
          break;

        case ExperimentalDeviceManager.MSG_TARGET_STATEMESSAGE:
          mManagerWeakReference.get().getDeviceDriver().targetStateChanged((StateMessage) msg.obj);
          break;
        case ExperimentalDeviceManager.MSG_TARGET_BULBNAME:
          mManagerWeakReference.get().getDeviceDriver().bulbNameChanged((BulbNameMessage) msg.obj);
          break;

        case ExperimentalDeviceManager.MSG_LAUNCH_CONFIGURATION:
          mManagerWeakReference.get().getDeviceDriver().launchOnboarding();
          break;
        case ExperimentalDeviceManager.MSG_CONNECTIONS_CONNECTIVITY:
          try {
            ConnectivityMessage message =
                mManagerWeakReference.get().getDeviceDriver().getConnectionConnectivity();
            mManagerWeakReference.get().mDeviceManagerMessenger.send(Message.
                obtain(null, ExperimentalDeviceManager.MSG_CONNECTIONS_CONNECTIVITY, message));
          } catch (RemoteException e) {
            // The client is dead.  Remove references to it;
            mManagerWeakReference.get().mDeviceManagerMessenger = null;
          }
          break;
        case ExperimentalDeviceManager.MSG_BULBS_CONNECTIVITY:
          try {
            ConnectivityMessage message =
                mManagerWeakReference.get().getDeviceDriver().getBulbConnectivity();
            mManagerWeakReference.get().mDeviceManagerMessenger.send(Message.
                obtain(null, ExperimentalDeviceManager.MSG_BULBS_CONNECTIVITY, message));
          } catch (RemoteException e) {
            // The client is dead.  Remove references to it;
            mManagerWeakReference.get().mDeviceManagerMessenger = null;
          }
          break;

        case ExperimentalDeviceManager.MSG_DEBUG_PING:
          DevLogger.debugLog("SimpleDeviceService.PING " + msg.arg1);
          DevLogger.getLogger().accumulate("SDS.PING", msg.arg1);
          mManagerWeakReference.get().mBrightness = msg.arg1;
          if (DevLogger.NET_DEBUG) {
            NetExerciser.simulateCrash(.05);
            NetExerciser.simulateWork(5000);
          }
          try {
            mManagerWeakReference.get().mDeviceManagerMessenger.send(
                Message.obtain(null, ExperimentalDeviceManager.MSG_DEBUG_ACK, msg.arg1, 0));
          } catch (RemoteException e) {
            // The client is dead.  Remove references to it;
            mManagerWeakReference.get().mDeviceManagerMessenger = null;
          }
          break;
        case ExperimentalDeviceManager.MSG_DEBUG_ACK:
          DevLogger.debugLog("SimpleDeviceService.ACK " + msg.arg1);
          DevLogger.getLogger().accumulate("SDS.ACK", msg.arg1);
        default:
          super.handleMessage(msg);
      }
    }
  }
}
