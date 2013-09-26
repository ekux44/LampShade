package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.kuxhausen.huemore.BulbListFragment;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.GroupListFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.menu;
import com.kuxhausen.huemore.network.GetBulbList.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.support.v7.widget.GridLayout.Spec;

public class EditAdvancedMoodActivity extends GodObject{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_container);
		
		this.restoreSerialized(this.getIntent().getStringExtra(InternalArguments.SERIALIZED_GOD_OBJECT));
		
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
				EditAdvancedMoodFragment firstFragment = new EditAdvancedMoodFragment();
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
								EditAdvancedMoodFragment.class.getName()).commit();
			}

		}
	}

	@Override
	public void onListReturned(BulbAttributes[] bulbsAttributes) {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void onGroupBulbSelected(Integer[] bulb, String name) {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void setBulbListenerFragment(OnBulbListReturnedListener frag) {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public OnBulbListReturnedListener getBulbListenerFragment() {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void onSelected(Integer[] bulbNum, String name,
			GroupListFragment groups, BulbListFragment bulbs) {
		throw new RuntimeException("Not implemented here");
	}
}
