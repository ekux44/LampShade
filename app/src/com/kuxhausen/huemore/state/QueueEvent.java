package com.kuxhausen.huemore.state;

public class QueueEvent implements Comparable<QueueEvent> {
  public Long bulbBaseId;
  /**
   * event start time measured in SystemClock.elapsedRealtime()
   */
  public Long miliTime;
  public Event event;

  public QueueEvent(Event e) {
    this.event = e.clone();
  }

  @Override
  public int compareTo(QueueEvent another) {
    return miliTime.compareTo(another.miliTime);
  }
}
