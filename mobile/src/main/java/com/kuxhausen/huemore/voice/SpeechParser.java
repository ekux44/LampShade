package com.kuxhausen.huemore.voice;

import android.content.Context;
import android.util.Log;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.GroupMoodBrightness;

import java.util.List;

public class SpeechParser {

  public static GroupMoodBrightness parse(Context c, String best, List<String> candidates,
                                          float[] confidences) {
    if (best == null && candidates != null && !candidates.isEmpty()) {
      best = candidates.get(0);
    }
    //TODO do something more intelligent with multiple strings and confidences

    Log.d("voice", best);

    GroupMoodBrightness result = new GroupMoodBrightness();
    if (best.equals("lumos maxima")) {
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
