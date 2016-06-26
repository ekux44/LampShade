package com.kuxhausen.huemore.net.dev;

import com.kuxhausen.huemore.utils.DeferredLog;

import java.util.HashMap;

/**
 * Logging & analytics helpers for developing/debugging parts of the .net.dev package
 *
 * TODO: try to refactor into unit tests wherever possible
 */
public class DevLogger {

  public static Boolean NET_DEBUG = false; // Do not check-in as true
  private static DevLogger sDevLogger = null;

  public static void debugLog(String output) {
    if (NET_DEBUG) {
      DeferredLog.e("abcd", output);
    }
  }

  public static DevLogger getLogger() {
    if (sDevLogger == null) {
      sDevLogger = new DevLogger();
    }
    return sDevLogger;
  }

  public HashMap<String, MessageAccumulator> mAccumulators;

  public DevLogger() {
    mAccumulators = new HashMap<>();
  }

  public void accumulate(String tag, int message) {
    MessageAccumulator accumulator = mAccumulators.get(tag);
    if (accumulator == null) {
      accumulator = new MessageAccumulator();
      mAccumulators.put(tag, accumulator);
    }

    accumulator.add(message);
    DeferredLog.e(tag, accumulator.toString());
  }
}
