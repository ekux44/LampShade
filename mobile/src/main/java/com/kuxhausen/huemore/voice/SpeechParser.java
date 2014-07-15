package com.kuxhausen.huemore.voice;

import android.content.Context;
import android.os.Bundle;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.GroupMoodBrightness;

import java.util.ArrayList;

public class SpeechParser {

  public static GroupMoodBrightness parse(Context c, Bundle b) {
    String speech = null;
    if (b.containsKey(InternalArguments.VOICE_INPUT)) {
      speech = b.getString(InternalArguments.VOICE_INPUT);
      return parse(c, speech);
    } else if (b.containsKey(InternalArguments.VOICE_INPUT_LIST)) {
      //TODO do something more intelligent with multiple strings and confidences
      ArrayList<String> matches = b.getStringArrayList(InternalArguments.VOICE_INPUT_LIST);
      speech = matches.get(0);
      return parse(c, speech);
    } else {
      return null;
    }
  }

  public static GroupMoodBrightness parse(Context c, String speech) {
    GroupMoodBrightness result = new GroupMoodBrightness();
    if (speech.equals("lumos maxima")) {
      result.group = c.getString(R.string.cap_all);
      result.mood = c.getString(R.string.cap_on);
      result.brightness = 100;
    } else {
      //TODO remove when done debugging
      result.group = c.getString(R.string.cap_all);
      result.mood = "Fruity";
    }

    return result;
  }
}
