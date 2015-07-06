package com.kuxhausen.huemore.net.dev;

import android.util.Log;

/**
 * Logging & analytics helpers for developing/debugging parts of the .net.dev package
 *
 * TODO: try to refactor into unit tests wherever possible
 */
public class DevLogger {

  public static Boolean NET_DEBUG = false; // Do not check-in as true

  public static void debugLog(String output) {
    if (NET_DEBUG) {
      Log.e("abcd", output);
    }
  }
}
