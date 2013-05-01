package com.kuxhausen.huemore.timing;

public class AlarmState {
	public String mood;
	public String group;
	public Integer transitiontime;
	public Integer brightness;
	public Boolean[] repeats;
	public Boolean scheduledForFuture;
	public Long[] scheduledTimes;

	public AlarmState() {
	}
}
