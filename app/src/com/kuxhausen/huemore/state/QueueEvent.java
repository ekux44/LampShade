package com.kuxhausen.huemore.state;

public class QueueEvent implements Comparable<QueueEvent>{
	public Long bulbBaseId;
	public Long nanoTime;
	public Event event;
	
	public QueueEvent(Event e){
		this.event = e.clone();
	}
	@Override
	public int compareTo(QueueEvent another) {
		return nanoTime.compareTo(another.nanoTime);
	}
}
