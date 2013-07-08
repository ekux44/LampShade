package com.kuxhausen.huemore.timing;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;

/**
 * stupid list wrapper to work around the non-existence of a
 * ListFragmentActivity
 **/
public class AlarmListActivity extends SherlockFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_alarm_activity);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				// return;
			} else {

				// Create an instance of ExampleFragment
				AlarmsListFragment firstFragment = new AlarmsListFragment();
				// GroupsListFragment firstFragment = new GroupsListFragment();

				// In case this activity was started with special instructions
				// from
				// an Intent,
				// pass the Intent's extras to the fragment as arguments
				firstFragment.setArguments(getIntent().getExtras());

				// Add the fragment to the 'fragment_container' FrameLayout
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, firstFragment,
								AlarmsListFragment.class.getName()).commit();
			}

		}
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings.getInt(PreferencesKeys.BULBS_UNLOCKED,
				PreferencesKeys.ALWAYS_FREE_BULBS) <= PreferencesKeys.ALWAYS_FREE_BULBS) {
			Intent i = new Intent(this, MainActivity.class);
			i.putExtra(InternalArguments.PROMPT_UPGRADE, true);
			startActivity(i);
			setResult(Activity.RESULT_CANCELED);
		}
	}
}
