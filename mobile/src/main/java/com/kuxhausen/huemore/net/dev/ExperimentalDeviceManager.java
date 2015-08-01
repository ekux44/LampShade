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

  /**
   * Command to the service to register a client, receiving callbacks from the service.  The
   * Message's replyTo field must be a Messenger of the client where callbacks should be sent.
   */
  public static final int MSG_REGISTER_CLIENT = 1;
  /**
   * Command to the service to unregister a client, ot stop receiving callbacks from the service
   */
  public static final int MSG_UNREGISTER_CLIENT = 2;

  // Example data to pass, TODO replace with BulbState fields
  public static final int MSG_SET_BRIGHTNESS = 3;
  public static final int MSG_ACK_BRIGHTNESS = 4;

  Service mContext;

  // TODO generalize to connecting to different device services at the same time
  Messenger mSampleMessanger = null;
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
          Message msg = Message.obtain(null, ExperimentalDeviceManager.MSG_UNREGISTER_CLIENT);
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
        Message msg = Message.obtain(null, ExperimentalDeviceManager.MSG_REGISTER_CLIENT);
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
        case ExperimentalDeviceManager.MSG_SET_BRIGHTNESS:
          DevLogger.debugLog("DeviceManagerRecieved: " + msg.arg1);
          DevLogger.getLogger().accumulate("EDM.SETBRI", msg.arg1);
          if (DevLogger.NET_DEBUG) {
            NetExerciser.sleep(5 * Math.random());
          }
          break;
        case ExperimentalDeviceManager.MSG_ACK_BRIGHTNESS:
          DevLogger.debugLog("DeviceManagerRecieved: ack");
          DevLogger.getLogger().accumulate("EDM.ACKBRI", msg.arg1);
        default:
          super.handleMessage(msg);
      }
    }
  }
}