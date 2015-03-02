package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment implements OnClickListener,
                                                          AdapterView.OnItemSelectedListener {

  private SharedPreferences mSettings;
  private CheckBox mEnableNfcReadPage;
  private Spinner mLanguageSelector;
  private List<String> mLocalizationCodes;
  private int mCurrentSelection = 0;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View myView = inflater.inflate(R.layout.settings, container, false);

    this.getActivity().setTitle(R.string.action_settings);

    Button rateButton = (Button) myView.findViewById(R.id.rateButton);
    rateButton.setOnClickListener(this);

    Button communitiesButton = (Button) myView.findViewById(R.id.action_communities);
    communitiesButton.setOnClickListener(this);

    if (BuildConfig.BUILD_TYPE.equals("debug")) {
      Button debugButton = (Button) myView.findViewById(R.id.action_debugging);
      debugButton.setOnClickListener(this);
      debugButton.setVisibility(View.VISIBLE);
    }

    mSettings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

    mEnableNfcReadPage = (CheckBox) myView.findViewById(R.id.showNfcReadPageCheckBox);
    if (mSettings.getBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, true)) {
      mEnableNfcReadPage.setChecked(true);
    }

    mLocalizationCodes = Arrays.asList(getResources().getStringArray(R.array.language_codes));

    mLanguageSelector = (Spinner) myView.findViewById(R.id.language_selector);
    mLanguageSelector.setOnItemSelectedListener(this);

    String currentLang = Locale.getDefault().getLanguage();
    if (mLocalizationCodes.contains(currentLang)) {
      mCurrentSelection = mLocalizationCodes.indexOf(currentLang);
      mLanguageSelector.setSelection(mCurrentSelection);
    }

    return myView;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.rateButton:
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                                                                    + "com.kuxhausen.huemore")));
        break;
      case R.id.action_communities:
        CommunityDialogFragment communities = new CommunityDialogFragment();
        communities.show(getChildFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        break;
      case R.id.action_debugging:
        DebugDialogFragment debug = new DebugDialogFragment();
        debug.show(getActivity().getSupportFragmentManager(),
                   InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        break;
    }
  }

  @Override
  public void onStop() {
    super.onStop();

    Editor edit = mSettings.edit();
    edit.putBoolean(PreferenceKeys.SHOW_ACTIVITY_ON_NFC_READ, mEnableNfcReadPage.isChecked());
    edit.commit();
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (position != mCurrentSelection) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
      Editor edit = prefs.edit();
      edit.putString(PreferenceKeys.USER_SELECTED_LOCALE_LANG, mLocalizationCodes.get(position));
      edit.commit();

      mCurrentSelection = position;

      //now reload the page with the new language (doesn't work on Gingerbread)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        getActivity().recreate();
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }
}
