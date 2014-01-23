package com.kuxhausen.huemore.glass;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.database.Cursor;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;

public class BrightnessActivity extends VoiceLauncherService {

	
	@Override
	public void onResume() {
	    super.onResume();
		ArrayList<String> voiceResults = getIntent().getExtras()
	            .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
	    // ...
	    
	    Pattern p = Pattern.compile("[0-9]+");
	    
	    GroupMoodBrightness gmb = new GroupMoodBrightness();
	    gmb.group = "ALL";
	    
	    if(voiceResults!=null){
		    for(String s : voiceResults)
		    	if(s!=null){
		    		Log.d("voice",s);
		    		Matcher m = p.matcher(s);
		    	    if (m.find()) {
		    	    	try{
                            int percent = Integer.parseInt(m.group());
                            if(percent>=0 && percent <=100)
                                    gmb.brightness = ((percent*255)/100);
                            		gmb.mood = "ON";
    		    	    			Log.d("voice", "parsed: "+gmb.brightness);
                            }
		    	    	catch (Error e){
                        }
		    	    } else if(s.contains("off")){
		    	    	gmb.mood = "OFF";
		    	    }
		    	}
	    }else{
	    	Log.d("voice","no voice results");
	    }
	    
	    
	    if(gmb.mood== null)
	    	return;
	    
	    
	    /** Cut down verison of Alarm Reciver code **/
        // TODO merge

        // Look up bulbs for that mood from database
        String[] groupColumns = { GroupColumns.BULB };
        String[] gWhereClause = { gmb.group };
        Cursor groupCursor = this.getContentResolver()
                        .query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
                                        groupColumns, GroupColumns.GROUP + "=?",
                                        gWhereClause, null);

        ArrayList<Integer> groupStates = new ArrayList<Integer>();
        while (groupCursor.moveToNext()) {
                groupStates.add(groupCursor.getInt(0));
        }
        Integer[] bulbS = groupStates.toArray(new Integer[groupStates
                        .size()]);

        Mood m = Utils.getMoodFromDatabase(gmb.mood, this);
        
        Intent trasmitter = new Intent(this, MoodExecuterService.class);
        trasmitter.putExtra(InternalArguments.ENCODED_MOOD, HueUrlEncoder.encode(m,bulbS,gmb.brightness));
        trasmitter.putExtra(InternalArguments.MOOD_NAME, gmb.mood);
        trasmitter.putExtra(InternalArguments.GROUP_NAME, gmb.group);
        startService(trasmitter);   
	}
}
