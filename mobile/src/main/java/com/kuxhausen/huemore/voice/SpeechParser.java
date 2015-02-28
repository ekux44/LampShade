package com.kuxhausen.huemore.voice;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.state.GroupMoodBrightness;

import java.util.List;

public class SpeechParser {

  public static GroupMoodBrightness parse(Context c, String best, List<String> candidates,
                                          float[] confidences) {
    if (best == null && candidates != null && !candidates.isEmpty()) {
      best = candidates.get(0);
    }
    //TODO do something more intelligent with multiple strings and confidences

    best = best.toLowerCase();
    Log.d("voice", best);

    GroupMoodBrightness result = new GroupMoodBrightness();
    if (best.equals("lumos maxima")) {
      result.group = c.getString(R.string.cap_all);
      result.mood = c.getString(R.string.cap_on);
      result.brightness = 100;
    } else {

      String[] briArgs = best.split(" brightness at ");
      String[] moodArgs = best.split(" to ");
      String[] moodTooArgs = best.split(" too ");
      String[] moodTwoArgs = best.split(" two ");
      String[] mood2Args = best.split(" 2 ");

      if (briArgs.length == 2) {
        Log.d("voice", briArgs[0] + "," + briArgs[1]);

        String lowercaseGroupName = briArgs[0].toLowerCase().trim();
        String brightness = briArgs[1].replaceAll("[^\\d]", "");

        int brightnessVal = -1;
        try {
          brightnessVal = Integer.parseInt(brightness);
        } catch (NumberFormatException e) {
        }

        String groupName = checkGroupName(c, lowercaseGroupName);

        if (groupName != null && brightnessVal != -1) {
          result.group = groupName;
          result.brightness = brightnessVal;

          Log.d("voice", "success:" + groupName + "," + brightnessVal);
        }

      } else if (moodArgs.length == 2) {
        Log.d("voice", moodArgs[0] + "," + moodArgs[1]);

        String lowercaseGroupName = moodArgs[0].toLowerCase().trim();
        String lowercaseMoodName = moodArgs[1].toLowerCase().trim();

        String groupName = checkGroupName(c, lowercaseGroupName);
        String moodName = checkMoodName(c, lowercaseMoodName);

        if (groupName != null && moodName != null) {
          result.group = groupName;
          result.mood = moodName;

          Log.d("voice", "success:" + groupName + "," + moodName);
        }
      } else if (moodTooArgs.length == 2) {
        Log.d("voice", moodTooArgs[0] + "," + moodTooArgs[1]);

        String lowercaseGroupName = moodTooArgs[0].toLowerCase().trim();
        String lowercaseMoodName = moodTooArgs[1].toLowerCase().trim();

        String groupName = checkGroupName(c, lowercaseGroupName);
        String moodName = checkMoodName(c, lowercaseMoodName);

        if (groupName != null && moodName != null) {
          result.group = groupName;
          result.mood = moodName;

          Log.d("voice", "success:" + groupName + "," + moodName);
        }
      } else if (moodTwoArgs.length == 2) {
        Log.d("voice", moodTwoArgs[0] + "," + moodTwoArgs[1]);

        String lowercaseGroupName = moodTwoArgs[0].toLowerCase().trim();
        String lowercaseMoodName = moodTwoArgs[1].toLowerCase().trim();

        String groupName = checkGroupName(c, lowercaseGroupName);
        String moodName = checkMoodName(c, lowercaseMoodName);

        if (groupName != null && moodName != null) {
          result.group = groupName;
          result.mood = moodName;

          Log.d("voice", "success:" + groupName + "," + moodName);
        }
      } else if (mood2Args.length == 2) {
        Log.d("voice", mood2Args[0] + "," + mood2Args[1]);

        String lowercaseGroupName = mood2Args[0].toLowerCase().trim();
        String lowercaseMoodName = mood2Args[1].toLowerCase().trim();

        String groupName = checkGroupName(c, lowercaseGroupName);
        String moodName = checkMoodName(c, lowercaseMoodName);

        if (groupName != null && moodName != null) {
          result.group = groupName;
          result.mood = moodName;

          Log.d("voice", "success:" + groupName + "," + moodName);
        }
      }
    }
    return result;
  }

  private static String checkGroupName(Context c, String lowercaseGroupName) {
    String[] groupColumns = {GroupColumns.COL_GROUP_NAME};
    String[] gWhereClause = {lowercaseGroupName};
    Cursor
        groupCursor =
        c.getContentResolver()
            .query(GroupColumns.URI, groupColumns, GroupColumns.COL_GROUP_LOWERCASE_NAME + "=?",
                   gWhereClause, null);
    String result = null;
    if (groupCursor.getCount() > 0) {
      groupCursor.moveToFirst();
      result = groupCursor.getString(0);
    }
    groupCursor.close();
    return result;
  }

  private static String checkMoodName(Context c, String lowercaseNameCandidate) {
    String[] moodColumns = {MoodColumns.COL_MOOD_NAME};
    String[] mWhereClause = {lowercaseNameCandidate};
    Cursor
        moodCursor =
        c.getContentResolver().query(MoodColumns.MOODS_URI, moodColumns,
                                     MoodColumns.COL_MOOD_LOWERCASE_NAME + "=?", mWhereClause,
                                     null);
    String result = null;
    if (moodCursor.getCount() > 0) {
      moodCursor.moveToFirst();
      result = moodCursor.getString(0);
    }
    moodCursor.close();
    return result;
  }
}
