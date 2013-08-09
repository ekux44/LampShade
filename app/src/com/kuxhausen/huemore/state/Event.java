package com.kuxhausen.huemore.state;

import com.kuxhausen.huemore.state.api.BulbState;

public class Event implements Comparable<Event>{
	public BulbState state;
	/**in units of 1/10 of a second */
	public Integer time;
	/** 0 indexed **/
	public Integer channel;
	
	@Override
	public int compareTo(Event another) {
		return time.compareTo(another.time);
	}
	
}
