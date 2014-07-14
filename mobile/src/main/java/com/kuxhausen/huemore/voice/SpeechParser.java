package com.kuxhausen.huemore.voice;

import android.content.Context;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.GroupMoodBrightness;

public class SpeechParser {
  public static GroupMoodBrightness parse(Context c, String speech){
    GroupMoodBrightness result = new GroupMoodBrightness();

    if(speech.equals("lumos maxima")){
      result.group = c.getString(R.string.cap_all);
      result.mood = c.getString(R.string.cap_on);
      result.brightness = 100;
    }

    else{
      //TODO remove when done debugging
      result.group = c.getString(R.string.cap_all);
      result.mood = "Fruity";
    }

    return result;
  }
}
