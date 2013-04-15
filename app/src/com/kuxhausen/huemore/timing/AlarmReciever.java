package com.kuxhausen.huemore.timing;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmReciever extends BroadcastReceiver {

	private final String REMINDER_BUNDLE = "MyReminderBundle"; 
	
	// this constructor is called by the alarm manager.
    public AlarmReciever(){ }

    // you can use this constructor to create the alarm. 
    //  Just pass in the main activity as the context, 
    //  any extras you'd like to get later when triggered 
    //  and the timeout
     public AlarmReciever(Context context, Bundle extras, int timeoutInSeconds){
         AlarmManager alarmMgr = 
             (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         Intent intent = new Intent(context, AlarmReciever.class);
         intent.putExtra(REMINDER_BUNDLE, extras);
         PendingIntent pendingIntent =
             PendingIntent.getBroadcast(context, 0, intent, 
             PendingIntent.FLAG_UPDATE_CURRENT);
         Calendar time = Calendar.getInstance();
         time.setTimeInMillis(System.currentTimeMillis());
         time.add(Calendar.SECOND, timeoutInSeconds);
         alarmMgr.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                      pendingIntent);
     }
	
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "HueMore Alarm went off", Toast.LENGTH_SHORT).show();
	}

}
