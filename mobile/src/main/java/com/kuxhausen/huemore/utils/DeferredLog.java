package com.kuxhausen.huemore.utils;

import android.util.Log;

import com.kuxhausen.huemore.BuildConfig;

public class DeferredLog {

  public final static boolean IS_LOGGABLE = BuildConfig.DEBUG;

  public static boolean isLoggable() {
    return IS_LOGGABLE;
  }

  public static void d(String tag, String format, Object... args) {
    if (isLoggable()) {
      Log.d(tag, String.format(format, args));
    }
  }

  public static void e(String tag, String format, Object... args) {
    if (isLoggable()) {
      Log.d(tag, String.format(format, args));
    }
  }

  public static void i(String tag, String format, Object... args) {
    if (isLoggable()) {
      Log.d(tag, String.format(format, args));
    }
  }

  public static void v(String tag, String format, Object... args) {
    if (isLoggable()) {
      Log.d(tag, String.format(format, args));
    }
  }

  public static void w(String tag, String format, Object... args) {
    if (isLoggable()) {
      Log.d(tag, String.format(format, args));
    }
  }
}
