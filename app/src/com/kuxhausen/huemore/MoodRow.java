package com.kuxhausen.huemore;

import com.kuxhausen.huemore.state.Mood;

public class MoodRow {
	Mood m;
	int id;
	String name;
	
	public MoodRow(Mood mood, String mName, int dbid){
		m = mood;
		name = mName;
		id = dbid;
	}
}
