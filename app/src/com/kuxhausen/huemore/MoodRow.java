package com.kuxhausen.huemore;

import com.kuxhausen.huemore.state.Mood;

public class MoodRow {
	Mood m;
	int id;
	String name;
	
	public MoodRow(String mName, int dbid, Mood mood){
		m = mood;
		name = mName;
		id = dbid;
	}
}
