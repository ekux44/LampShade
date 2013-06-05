package com.kuxhausen.huemore.automation;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.network.SynchronousTransmitGroupMood;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.api.BulbState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class FireReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
            Gson gson = new Gson();
			String serializedGMB = intent.getExtras().getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE).getString(EditActivity.EXTRA_BUNDLE_SERIALIZED_BY_NAME);
			GroupMoodBrightness gmb = gson.fromJson(serializedGMB, GroupMoodBrightness.class);
			
			
			
			/** Cut down verison of Alarm Reciver code **/ //TODO merge
			
			// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = { gmb.group };
			Cursor groupCursor = context.getContentResolver().query(
					DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, groupColumns,
					GroupColumns.GROUP + "=?", gWhereClause, null);

			ArrayList<Integer> groupStates = new ArrayList<Integer>();
			while (groupCursor.moveToNext()) {
				groupStates.add(groupCursor.getInt(0));
			}
			Integer[] bulbS = groupStates.toArray(new Integer[groupStates.size()]);

			String[] moodColumns = { MoodColumns.STATE };
			String[] mWereClause = {gmb.mood };
			Cursor moodCursor = context.getContentResolver().query(
					DatabaseDefinitions.MoodColumns.MOODSTATES_URI, moodColumns,
					MoodColumns.MOOD + "=?", mWereClause, null);

			ArrayList<String> moodStates = new ArrayList<String>();
			while (moodCursor.moveToNext()) {
				moodStates.add(moodCursor.getString(0));
			}
			String[] moodS = moodStates.toArray(new String[moodStates.size()]);

			int brightness = gmb.brightness;
			for (int i = 0; i < moodS.length; i++) {
				BulbState bs = gson.fromJson(moodS[i], BulbState.class);
				bs.bri = brightness;
				moodS[i] = gson.toJson(bs);// back into json for TransmitGroupMood
			}

			SynchronousTransmitGroupMood trasmitter = new SynchronousTransmitGroupMood();
			trasmitter.execute(context, bulbS, moodS);
        }
	}

}
