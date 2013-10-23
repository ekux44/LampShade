package com.kuxhausen.huemore.automation;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class FireReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent
				.getAction())) {
			Gson gson = new Gson();
			String serializedGMB = intent.getExtras()
					.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE)
					.getString(EditActivity.EXTRA_BUNDLE_SERIALIZED_BY_NAME);
			GroupMoodBrightness gmb = gson.fromJson(serializedGMB,
					GroupMoodBrightness.class);

			Bundle b = intent.getExtras();
			if(b.containsKey(EditActivity.PERCENT_BRIGHTNESS_KEY) && !b.getString(EditActivity.PERCENT_BRIGHTNESS_KEY).contains("%")){
				try{
				int percent = Integer.parseInt(b.getString(EditActivity.PERCENT_BRIGHTNESS_KEY));
				if(percent>=0 && percent <=100)
					gmb.brightness = ((percent*255)/100);
				} catch (Error e){
				}
			}
			if(b.containsKey(EditActivity.MOOD_NAME_KEY) && !b.getString(EditActivity.MOOD_NAME_KEY).contains("%")){
				String mood = b.getString(EditActivity.MOOD_NAME_KEY);
				if(mood!=null && mood.length()>0)
					gmb.mood = mood;
			}
			
			/** Cut down verison of Alarm Reciver code **/
			// TODO merge

			// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = { gmb.group };
			Cursor groupCursor = context.getContentResolver()
					.query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
							groupColumns, GroupColumns.GROUP + "=?",
							gWhereClause, null);

			ArrayList<Integer> groupStates = new ArrayList<Integer>();
			while (groupCursor.moveToNext()) {
				groupStates.add(groupCursor.getInt(0));
			}
			Integer[] bulbS = groupStates.toArray(new Integer[groupStates
					.size()]);

			Mood m = Utils.getMoodFromDatabase(gmb.mood, context);
			
			Intent trasmitter = new Intent(context, MoodExecuterService.class);
			trasmitter.putExtra(InternalArguments.ENCODED_MOOD, HueUrlEncoder.encode(m,bulbS,gmb.brightness));
			trasmitter.putExtra(InternalArguments.MOOD_NAME, gmb.mood);
			startWakefulService(context, trasmitter);
		}
	}

}
