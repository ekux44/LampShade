package com.kuxhausen.huemore.net.dev;

import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.ref.WeakReference;

/**
 * For generating fake messages on either side of the IPC between DeviceManager and DeviceServices
 */
public class NetExerciser extends AsyncTask<Messenger, Integer, Void> {

  WeakReference<Messenger> targetWeakReference, replyToWeakReference;

  @Override
  protected Void doInBackground(Messenger... param) {
    targetWeakReference = new WeakReference<>(param[0]);
    replyToWeakReference = new WeakReference<>(param[1]);

    for (int i = 0; i < 100; i++) {
      sleep(100 * Math.random());
      publishProgress(i);
    }
    return null;
  }

  protected void onProgressUpdate(Integer... progress) {
    Message
        msg =
        Message.obtain(null, IpcMaster.MSG_DEBUG_PING, progress[0], 0);
    msg.replyTo = replyToWeakReference.get();

    try {
      targetWeakReference.get().send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
      DevLogger.debugLog("RemoteException");
    }
  }

  protected void onPostExecute() {
  }

  public static void sleep(double duration) {
    try {
      Thread.sleep((int) duration, 0);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException("Sleep interupted");
    }
  }

  public static void simulateWork(int maxMilliseconds) {
    sleep(Math.random() * maxMilliseconds);
  }

  public static void simulateCrash(double probability) {
    if (Math.random() < probability) {
      throw new RuntimeException("Simulated crash");
    }
  }
}
