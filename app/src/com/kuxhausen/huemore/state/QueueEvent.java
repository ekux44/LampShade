package com.kuxhausen.huemore.state;

public class QueueEvent implements Comparable<QueueEvent>{
	public int bulb;
	public Long nanoTime;
	public Event event;
	
	public QueueEvent(Event e){
		this.event = e;
	}
	@Override
	public int compareTo(QueueEvent another) {
		return nanoTime.compareTo(another.nanoTime);
	}
}
