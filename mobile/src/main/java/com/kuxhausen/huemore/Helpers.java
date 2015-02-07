package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;

import com.kuxhausen.huemore.persistence.Definitions;

import java.util.Locale;

public class Helpers {

  /**
   * from http://stackoverflow.com/questions/4336286/tiled-drawable-sometimes-stretches/9500334#9500334
   */
  public static void fixBackgroundRepeat(View view) {
    Drawable bg = view.getBackground();
    if (bg != null) {
      if (bg instanceof BitmapDrawable) {
        BitmapDrawable bmp = (BitmapDrawable) bg;
        bmp.mutate(); // make sure that we aren't sharing state anymore
        bmp.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
      }
    }
  }

  public static void applyLocalizationPreference(Activity a) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a);
    if (prefs.contains(Definitions.PreferenceKeys.USER_SELECTED_LOCALE_LANG)) {
      Locale loc =
          new Locale(prefs.getString(Definitions.PreferenceKeys.USER_SELECTED_LOCALE_LANG, ""));
      Locale.setDefault(loc);
      Configuration config = new Configuration();
      config.locale = loc;
      a.getBaseContext().getResources()
          .updateConfiguration(config, a.getBaseContext().getResources().getDisplayMetrics());
    }
  }
}
