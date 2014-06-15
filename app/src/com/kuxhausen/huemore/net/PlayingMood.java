package com.kuxhausen.huemore.net;

import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.state.Mood;

/** used to store activity data about an ongoing mood and format the data for consumption by visualizations/notefications **/
public class PlayingMood {
	public Mood mood;
	public String moodName;
	public Group group;	
	
	public Integer initialMaxBri; // careful this might go stale after playing starts
	
	public String getMoodName(){
		if(moodName!=null)
			return moodName;
		return "?";
	}
	public String getGroupName(){
		if(group!=null)
			return group.getName();
		return "?";
	}
	
	public String toString(){
		return getGroupName()+" \u2190 "+getMoodName();
	}
}
