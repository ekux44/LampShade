package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.Inventory;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.DatabaseHelper;

/**
 * @author Eric Kuxhausen
 */
public class SecondActivity extends NetworkManagedSherlockFragmentActivity {

	public SecondActivity me = this;
	
	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	IabHelper mPlayHelper;
	Inventory lastQuerriedInventory;
	public OnBulbListReturnedListener bulbListenerFragment;
	SharedPreferences settings;
	
	/**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mManualMoodSlidingTabLayout;
 
	SeekBar brightnessBar;
	boolean isTrackingTouch = false;
	
	MoodManualPagerAdapter mMoodManualPagerAdapter;

	ViewPager mViewPager2;
	NetworkManagedSherlockFragmentActivity parrentActivity;
	
	public void setBulbListenerFragment(OnBulbListReturnedListener frag){
		bulbListenerFragment = frag;
	}
	public OnBulbListReturnedListener getBulbListenerFragment(){
		return bulbListenerFragment;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.secondary_activity);
		if(this.getCurentGroupName()!=null && this.getCurentGroupName().length()>0)
			this.getSupportActionBar().setTitle(this.getCurentGroupName());
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
		parrentActivity = this;
		// Set up the ViewPager, attaching the adapter.
		mViewPager2 = (ViewPager) this.findViewById(R.id.manual_mood_pager);
		mViewPager2.setAdapter(mMoodManualPagerAdapter);
		
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mManualMoodSlidingTabLayout = (SlidingTabLayout) this.findViewById(R.id.manual_mood_sliding_tabs);
        mManualMoodSlidingTabLayout.setViewPager(mViewPager2);
        mManualMoodSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.red_color));
		
		settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
		if (settings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
			mViewPager2.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
		}
		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				me.setBrightness(seekBar.getProgress());
				isTrackingTouch = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				me.setBrightness(seekBar.getProgress());
				isTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					me.setBrightness(seekBar.getProgress());
				}
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outstate){
		Editor edit = settings.edit();
		switch(mViewPager2.getCurrentItem()){
			case MoodManualPagerAdapter.MANUAL_LOCATION:
				edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, false);
				break;
			case MoodManualPagerAdapter.MOOD_LOCATION:
				edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
				break;
		}
		edit.commit();
		
		super.onSaveInstanceState(outstate);
	}
	
	public void invalidateSelection() {
		((MoodListFragment) (mMoodManualPagerAdapter.getItem(MoodManualPagerAdapter.MOOD_LOCATION)))
				.invalidateSelection();
	}
	
	@Override
	public void onBrightnessChanged(int brightness) {
		if(brightnessBar!=null && !isTrackingTouch)
			brightnessBar.setProgress(brightness);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.second, menu);

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
			MenuItem bothItem = menu.findItem(R.id.action_add_both);
			if (bothItem != null) {
				bothItem.setEnabled(false);
				bothItem.setVisible(false);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			this.startActivity(new Intent(this,MainActivity.class));
			return true;
		case R.id.action_add_both:
			AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
			addBoth.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
