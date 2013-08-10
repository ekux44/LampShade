package com.kuxhausen.huemore.state;

public class QueueEvent extends Event {
	public int bulb;
	
	public QueueEvent(Event e){
		this.channel = e.channel;
		this.state = e.state;
		this.time = e.time;
	}
}
