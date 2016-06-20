package com.kuxhausen.huemore.utils;

import java.util.Arrays;

public class RateLimiter {

  private long mWindowWidth;
  private int mCapacity;
  /**
   * Rolling buffer of the most recent events. An event with N capacity is stored as N timestamps of
   * that event.
   */
  private long[] mTimestamps;
  /**
   * Index of the oldest timestamp in the buffer.
   */
  private int mIndex;
  private long mLatestEventMs;

  /**
   * Implements a sliding widow rate limiter. Optimized for minimal object
   * allocations/de-allocations. O(capacity) performance and O(capacity) memory.
   *
   * @param windowWidthMs width in milliseconds of the rolling window being rate limited.
   * @param capacity      capacity during the window.
   */
  public RateLimiter(long windowWidthMs, int capacity) {
    mWindowWidth = windowWidthMs;
    mCapacity = capacity;
    mTimestamps = new long[capacity];
    Arrays.fill(mTimestamps, Long.MIN_VALUE);
    mIndex = 0;
    mLatestEventMs = Long.MIN_VALUE;
  }

  /**
   * @param timestampMs event timestamp in milliseconds
   * @return if the event can occur without exceeding the rate limit
   */
  public boolean hasCapacity(long timestampMs, int eventCapacity) {
    if (timestampMs < mLatestEventMs) {
      throw new IllegalArgumentException("RateLimiter cannot go backwards in time");
    }
    int tempIndex = mIndex;
    while (eventCapacity > 0) {
      if (mTimestamps[tempIndex] < timestampMs - mWindowWidth) {
        tempIndex = (tempIndex + 1) % mCapacity;
        eventCapacity--;
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Must only be called if hasCapacity returns true.
   *
   * @param timestampMs event timestamp in milliseconds.
   */
  public void consumeCapacity(long timestampMs, int eventCapacity) {
    while (eventCapacity > 0) {
      mTimestamps[mIndex] = timestampMs;
      mIndex = (mIndex + 1) % mCapacity;
      eventCapacity--;
    }
    mLatestEventMs = timestampMs;
  }
}
