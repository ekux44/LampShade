package com.kuxhausen.huemore.net.dev;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.ref.WeakReference;

/**
 * Prototype implementation of the DeviceManager that isolates the driver for each device type into
 * a separate device service running in another processes.
 *
 * TODO increase fault tolerance and stress test, then bring up with the existing device types
 */
public class ExperimentalDeviceManager {

  // Debugging commands
  public static final int MSG_DEBUG_PING = 101;
  public static final int MSG_DEBUG_ACK = 102;

  // IPC control commands
  /**
   * Message from the device manager to a device driver. Command to register the manager. The
   * Message's replyTo field must be a Messenger of the manager where callbacks should be sent. The
   * device driver must respond with {@link #MSG_DRIVER_PID}.
   */
  public static final int MSG_REGISTER_MANAGER = 201;
  /**
   * Message from a device driver to the device manager. Response to {@link #MSG_REGISTER_MANAGER}
   * containing the Process ID for this device driver.
   */
  public static final int MSG_DRIVER_PID = 202;
  /**
   * Message from the device manager to a device driver. The device driver must respond within an
   * {@link #MSG_WATCHDOG_ACK}. If the device manager doesn't receive a response within 1 second,
   * the device driver may be process killed.
   */
  public static final int MSG_WATCHDOG_POLL = 203;
  /**
   * Response to {@link #MSG_WATCHDOG_POLL}.
   */
  public static final int MSG_WATCHDOG_ACK = 204;
  /**
   * Message from the device manager to a driver driver to stop sending messages and shut down.
   */
  public static final int MSG_UNREGISTER_MANAGER = 204;

  // Device control commands
  public static final int MSG_TARGET_STATEMESSAGE = 301;
  public static final int MSG_OBSERVED_STATEMESSAGE = 302;
  public static final int MSG_CONNECTIONS_CONNECTIVITY = 303;
  public static final int MSG_BULBS_CONNECTIVITY = 304;
  public static final int MSG_TARGET_BULBNAME = 305;
  public static final int MSG_LAUNCH_CONFIGURATION = 306;


  Service mContext;
  // TODO generalize to connecting to different device services at the same time
  Messenger mSampleMessanger = null;
  Integer mSampleDevicePid = null;
  boolean mIsBound;

  public ExperimentalDeviceManager(Service s) {
    mContext = s;
    doBindService();
  }

  public void onDestroy() {
    doUnbindService();
  }

  /**
   * Target we publish for device services to send messages to our IncomingHandler.
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler(new WeakReference<>(this)));

  void doBindService() {
    DevLogger.debugLog("DeviceManagerSending: bind");
    mContext.bindService(new Intent(mContext, SampleDeviceService.class), mConnection,
                         Context.BIND_AUTO_CREATE);
    mIsBound = true;
  }

  void doUnbindService() {
    if (mIsBound) {
      // If we've received the service, and hence registered with it, now is the time to unregister
      if (mSampleMessanger != null) {
        try {
          Message msg = Message.obtain(null, ExperimentalDeviceManager.MSG_UNREGISTER_MANAGER);
          msg.replyTo = mMessenger;
          mSampleMessanger.send(msg);
        } catch (RemoteException e) {
          // There is nothing special we need to do if the service has crashed
        }
      }

      // Detach our existing connection.
      DevLogger.debugLog("DeviceManagerSending: unbind");
      mContext.unbindService(mConnection);
      mIsBound = false;
    }
  }

  /**
   * Class for interacting with the main interface of the service.
   */
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      // This is called when the connection with the service has been established, giving us the
      // service object we can use to interact with the service.  We are communicating with our
      // service through an IDL interface, so get a client-side representation of that from the
      // raw service object.
      mSampleMessanger = new Messenger(service);

      // We want to monitor the service for as long as we are connected to it
      try {
        Message msg = Message.obtain(null, ExperimentalDeviceManager.MSG_REGISTER_MANAGER);
        msg.replyTo = mMessenger;
        mSampleMessanger.send(msg);

        if (DevLogger.NET_DEBUG) {
          //Turn on some test communication
          new NetExerciser().execute(mSampleMessanger, mMessenger);
        }
      } catch (RemoteException e) {
        // The service has crashed before we could even do anything with it; we can count on soon
        // being disconnected (and then reconnected if it can be restarted) so there is no need to
        // do anything here.
      }

      DevLogger.debugLog("DeviceManagerNotified: onServiceConnected");
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been unexpectedly disconnected
      // That is, its process crashed.
      mSampleMessanger = null;

      DevLogger.debugLog("DeviceManagerNotified: onServiceDisconnected");
    }
  };

  /**
   * Handler of incoming messages from device services.
   */
  private static class IncomingHandler extends Handler {

    WeakReference<ExperimentalDeviceManager> mManagerWeakReference;

    public IncomingHandler(WeakReference<ExperimentalDeviceManager> managerWeakReference) {
      mManagerWeakReference = managerWeakReference;
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case ExperimentalDeviceManager.MSG_DRIVER_PID:
          mManagerWeakReference.get().mSampleDevicePid = msg.arg1;
        case ExperimentalDeviceManager.MSG_DEBUG_PING:
          DevLogger.debugLog("ExperimentalDeviceManager.PING " + msg.arg1);
          DevLogger.getLogger().accumulate("EDM.PING", msg.arg1);
          if (DevLogger.NET_DEBUG) {
            NetExerciser.simulateWork(5);
          }
          try {
            mManagerWeakReference.get().mSampleMessanger.send(
                Message.obtain(null, ExperimentalDeviceManager.MSG_DEBUG_ACK, msg.arg1, 0));
          } catch (RemoteException e) {
            // The client is dead.  Remove references to it;
            mManagerWeakReference.get().mSampleMessanger = null;
          }
          break;
        case ExperimentalDeviceManager.MSG_DEBUG_ACK:
          DevLogger.debugLog("ExperimentalDeviceManager.ACK " + msg.arg1);
          DevLogger.getLogger().accumulate("EDM.ACKBRI", msg.arg1);
        default:
          super.handleMessage(msg);
      }
    }
  }
}