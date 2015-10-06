package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

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
    boolean useSystemLang =
        prefs.getBoolean(a.getString(R.string.preference_use_system_language), false);
    String localLangOverride =
        prefs.getString(a.getString(R.string.preference_user_selected_locale_lang), null);

    if (!useSystemLang && !TextUtils.isEmpty(localLangOverride)) {
      Locale loc = new Locale(localLangOverride);
      Locale.setDefault(loc);
      Configuration config = new Configuration();
      config.locale = loc;
      a.getBaseContext().getResources()
          .updateConfiguration(config, a.getBaseContext().getResources().getDisplayMetrics());
    }
  }

  public static boolean isDebugVersion() {
    return BuildConfig.BUILD_TYPE.equals("debug");
  }
}
