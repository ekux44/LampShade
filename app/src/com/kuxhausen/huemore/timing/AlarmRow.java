package com.kuxhausen.huemore.timing;


public class AlarmRow {

	private boolean checked;
	private AlarmState aState;
	
	public AlarmRow(AlarmState as){
		aState = as;
	}
	public String getTime(){
		return "time";
	}
	public String getSecondaryDescription(){
		return "group -> mood  Mons";
	}
	public boolean isScheduled(){
		return checked;
	}
	public void toggle(){
		
	}
}
