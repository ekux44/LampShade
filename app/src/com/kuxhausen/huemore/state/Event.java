package com.kuxhausen.huemore.state;

import com.kuxhausen.huemore.state.api.BulbState;

public class Event implements Comparable<Event>{
	public BulbState state;
	/** 0 indexed **/
	public Integer channel;
	/**in units of 1/10 of a second */
	public Integer time;
	
	@Override
	public int compareTo(Event another) {
		return time.compareTo(another.time);
	}
	
	public Event(){}
	
	public Event(BulbState state, Integer channel, Integer time){
		this.state = state;
		this.channel = channel;
		this.time = time;
	}
	
}
