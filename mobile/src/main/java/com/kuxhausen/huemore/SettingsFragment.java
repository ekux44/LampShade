package com.kuxhausen.huemore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class SettingsFragment extends PreferenceFragmentCompat implements
                                                               SharedPreferences.OnSharedPreferenceChangeListener {

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.settings);

    if (Helpers.isDebugVersion()) {
      Context ctx = getPreferenceManager().getContext();

      Preference preference = new Preference(ctx);
      preference.setKey(getString(R.string.preference_developer_options));
      preference.setTitle(R.string.developer_options_title);
      this.getPreferenceScreen().addPreference(preference);
    }

    // Hide the doze option if the phone doesn't support doze.
    if (!DisableDozeDialogFragment.systemSupportsDoze()) {
      Preference ignoreDoze = getPreferenceScreen().findPreference(getString(R.string.preference_ignore_doze));
      if (ignoreDoze != null) {
          ignoreDoze.setVisible(false);
      }
    }

    showSelectedLanguage();

    Preference buildVersion = getPreferenceScreen().findPreference(
        getString(R.string.preference_build_version));
    buildVersion.setSummary(BuildConfig.VERSION_NAME);
  }

  public void showSelectedLanguage() {
    String
        userSelectedLangCode =
        getPreferenceManager().getSharedPreferences()
            .getString(getString(R.string.preference_user_selected_locale_lang), null);

    String[] langCodes = getResources().getStringArray(R.array.language_codes);
    String[] langNames = getResources().getStringArray(R.array.language_names);

    Preference userSelectedLang = getPreferenceScreen().findPreference(
        getString(R.string.preference_user_selected_locale_lang));

    for (int i = 0; i < langCodes.length; i++) {
      if (langCodes[i].equals(userSelectedLangCode)) {
        userSelectedLang.setSummary(langNames[i]);
        return;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    super.onPause();
  }

  @Override
  public boolean onPreferenceTreeClick(Preference preference) {
    String key = preference.getKey();

    if (getString(R.string.preference_social_links).equals(key)) {
      CommunityDialogFragment communities = new CommunityDialogFragment();
      communities.show(getActivity().getSupportFragmentManager(),
                       InternalArguments.FRAG_MANAGER_DIALOG_TAG);
      return true;

    } else if (getString(R.string.preference_developer_options).equals(key)) {
      DebugDialogFragment debug = new DebugDialogFragment();
      debug.show(getActivity().getSupportFragmentManager(),
                 InternalArguments.FRAG_MANAGER_DIALOG_TAG);
      return true;

    }
    return super.onPreferenceTreeClick(preference);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (getString(R.string.preference_user_selected_locale_lang).equals(key)) {
      //now reload the page with the new language (doesn't work on Gingerbread)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        getActivity().recreate();
      } else {
        showSelectedLanguage();
      }
    }
  }
}
