package com.kuxhausen.huemore.editmood;

import android.view.View;

public interface TimeslotDuration{
	
	public abstract View getView();	
	public abstract void setDuration(int duration);
	public abstract int getDuration();
}
