package com.kuxhausen.huemore.editmood;

import android.view.View;

public interface TimeslotStartTime{
	
	public abstract View getView();	
	public abstract void setStartTime(int startTime);
	public abstract int getStartTime();
}
