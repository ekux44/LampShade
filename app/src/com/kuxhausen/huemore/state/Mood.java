package com.kuxhausen.huemore.state;

public class Mood {
	public Event[] events;
	public int numChannels;
	public Boolean usesTiming;
	public Boolean infiniteLooping;
	/** if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets from mood start time **/
	public Boolean timeAddressingRepeatPolicy;
	public Integer numLoops;
	
}
