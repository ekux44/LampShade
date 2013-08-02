package com.kuxhausen.huemore.state;

public class Mood {
	public Event[] events;
	public int numChannels;
	public Boolean usesTiming;
	/** if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets from mood start time **/
	public Boolean timeAddressingRepeatPolicy;
	/** max value 126 (127 special cased to infinity) **/
	public Integer numLoops;
	
	public Mood(){
		timeAddressingRepeatPolicy = false;
		usesTiming = false;
		numChannels = 0;
		numLoops = 0;
		events = new Event[0];
	}
	public void setInfiniteLooping(boolean infinite){
		if(infinite)
			numLoops = 127;
	}
	public boolean isInfiniteLooping(){
		return (numLoops == 127);
	}
	public void setNumLoops(int num){
		numLoops = Math.max(0, Math.min(127, num));
	}
}
