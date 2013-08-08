package com.kuxhausen.huemore.automation;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class FireReceiver extends BroadcastReceiver {

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
			
//TODO rewrite/renable			
/*			int brightness = gmb.brightness;
			for (int i = 0; i < moodS.length; i++) {
				BulbState bs = gson.fromJson(moodS[i], BulbState.class);
				bs.bri = brightness;
				moodS[i] = gson.toJson(bs);// back into json for
											// TransmitGroupMood
			}

			SynchronousTransmitGroupMood trasmitter = new SynchronousTransmitGroupMood();
			trasmitter.execute(context, bulbS, moodS);
		*/}
	}

}
