package com.kuxhausen.huemore.state;

public class Mood {
	public Event[] events;
	private int numChannels;
	public Boolean usesTiming;
	/**in units of 1/10 of a second */
	public int loopIterationTimeLength;
	/** if true, timestamps in events are offsets from beginning of the day, otherwise they are offsets from mood start time **/
	public Boolean timeAddressingRepeatPolicy;
	/** max value 126 (127 special cased to infinity) **/
	private Integer numLoops;
	
	public Mood(){
		timeAddressingRepeatPolicy = false;
		usesTiming = false;
		numChannels = 1;
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
	public int getNumLoops(){
		return numLoops;
	}
	public int getNumChannels(){
		return Math.max(numChannels, 1);
	}
	public void setNumChannels(int num){
		numChannels = num;
	}
}
