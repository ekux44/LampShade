package com.kuxhausen.huemore.alarm;

import android.os.Handler;
import android.os.HandlerThread;

import com.kuxhausen.huemore.persistence.Definitions;

public class AlarmAsyncHandler {

  /**
   * Manages the background thread used to perform io operations and handle async broadcasts
   */
  private static final HandlerThread
      sHandlerThread =
      new HandlerThread(Definitions.InternalArguments.ALARM_HANDLER_THREAD);
  private static final Handler sHandler;

  static {
    sHandlerThread.start();
    sHandler = new Handler(sHandlerThread.getLooper());
  }

  public static void post(Runnable r) {
    sHandler.post(r);
  }

  private AlarmAsyncHandler() {
  }
}
