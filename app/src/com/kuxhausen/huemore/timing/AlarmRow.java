package com.kuxhausen.huemore.timing;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;


public class AlarmRow {

	private AlarmState aState;
	private Context c;
	private int id;
	Gson gson = new Gson();
	
	public AlarmRow(Context context, AlarmState as, int db_ID){
		c = context;
		aState = as;
		id = db_ID;
	}
	public String getTime(){
		return "5:00 PM";
	}
	public String getSecondaryDescription(){
		String result = aState.group+" -> "+aState.mood;
		
		if(aState.repeats!=null){
			boolean[] repeats = new boolean[7];
			for(int i = 0; i<repeats.length; i++)
				repeats[i]=aState.repeats[i];
			result+="   "+NewAlarmDialogFragment.repeatsToString(c, repeats);
		}
		return result;
	}
	public boolean isScheduled(){
		if(aState.scheduledForFuture==null)
			aState.scheduledForFuture=false;
		return aState.scheduledForFuture;
	}
	public void toggle(){
		aState.scheduledForFuture = !isScheduled();
		
		//save change to db
		String rowSelect = BaseColumns._ID + "=?";
		String[] rowArg = { ""+id };
		
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(AlarmColumns.STATE, gson.toJson(aState));
		
		c.getContentResolver().update(
				AlarmColumns.ALARMS_URI, mNewValues, rowSelect,
				rowArg);
	}
}
