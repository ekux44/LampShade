package com.kuxhausen.huemore;

import com.kuxhausen.huemore.DatabaseDefinitions.PreferencesKeys;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements
		GroupsFragment.OnHeadlineSelectedListener {

	DatabaseHandler helper = new DatabaseHandler(this);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hue_more);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			GroupsFragment firstFragment = new GroupsFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();

			helper.initialPopulate();// initialize database

			// check to see if the bridge IP address is setup yet
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (!settings.contains(PreferencesKeys.Bridge_IP_Address)) {
				RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
				rwhdf.show(this.getSupportFragmentManager(), "dialog");
			}
		}
	}

	public void onArticleSelected(int position) {
		// The user selected the headline of an article from the
		// HeadlinesFragment

		// Capture the article fragment from the activity layout
		MoodsFragment articleFrag = (MoodsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.moods_fragment);

		if (articleFrag != null) {
			// If article frag is available, we're in two-pane layout...

			// Call a method in the ArticleFragment to update its content
			articleFrag.updateArticleView(position);

		} else {
			// If the frag is not available, we're in the one-pane layout and
			// must swap frags...

			// Create fragment and give it an argument for the selected article
			MoodsFragment newFragment = new MoodsFragment();
			Bundle args = new Bundle();
			args.putInt(MoodsFragment.ARG_POSITION, position);
			newFragment.setArguments(args);
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.action_register_with_hub:
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			rwhdf.show(getSupportFragmentManager(), "dialog");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
