package com.kuxhausen.huemore.timing;

public class AlarmState {
	public String mood;
	public String group;
	public Integer transitiontime;
	public Integer brightness;
	
	/** 7 booleans which days {Sunday, ... ,Saturday} to repeat on } **/
	private Boolean[] repeats;
	public Boolean scheduledForFuture;
	
	/** if nonrepeating, size = 1. If repeating, size = 7**/
	public Long[] scheduledTimes;

	public AlarmState() {
	}
	
	public boolean isRepeating(){
		boolean result = false;
		if(repeats==null)
			return result;
		for(Boolean b : repeats)
			if(b!=null && b==true)
				result = true;
		
		return result;
	}
	
	public boolean[] getRepeatingDays(){
		boolean[] result = new boolean[7];
		if(repeats==null)
			return result;
		for(int i = 0; i<7; i++)
			result[i]= repeats[i];
		return result;		
	}
	/** @param day an array of 7 booleans indicated which days Sunday...Saturday that the alarm should repeat on**/
	public void setRepeatingDays(boolean[] day){
		repeats = new Boolean[7];
		for(int i =0; i<7; i++)
			repeats[i] = day[i];
	}
}
