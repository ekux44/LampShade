package com.kuxhausen.huemore.automation;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.GroupMoodBrightness;

public class FireReceiver extends WakefulBroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO Auto-generated method stub
    if (com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
      Gson gson = new Gson();
      String serializedGMB =
          intent.getExtras().getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE)
              .getString(EditActivity.EXTRA_BUNDLE_SERIALIZED_BY_NAME);
      GroupMoodBrightness gmb = gson.fromJson(serializedGMB, GroupMoodBrightness.class);

      Bundle b = intent.getExtras();
      if (b.containsKey(EditActivity.PERCENT_BRIGHTNESS_KEY)
          && !b.getString(EditActivity.PERCENT_BRIGHTNESS_KEY).contains("%")) {
        try {
          int percent = Integer.parseInt(b.getString(EditActivity.PERCENT_BRIGHTNESS_KEY));
          if (percent >= 0 && percent <= 100) {
            gmb.brightness = ((percent * 255) / 100);
          }
        } catch (Error e) {
        }
      }
      if (b.containsKey(EditActivity.MOOD_NAME_KEY)
          && !b.getString(EditActivity.MOOD_NAME_KEY).contains("%")) {
        String mood = b.getString(EditActivity.MOOD_NAME_KEY);
        if (mood != null && mood.length() > 0) {
          gmb.mood = mood;
        }
      }

      Intent trasmitter = new Intent(context, ConnectivityService.class);
      trasmitter.putExtra(InternalArguments.MOOD_NAME, gmb.mood);
      trasmitter.putExtra(InternalArguments.GROUP_NAME, gmb.group);
      trasmitter.putExtra(InternalArguments.MAX_BRIGHTNESS, gmb.brightness);
      startWakefulService(context, trasmitter);
    }
  }

}
