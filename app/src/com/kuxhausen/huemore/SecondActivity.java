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
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

/**
 * @author Eric Kuxhausen
 */
public class SecondActivity extends NetworkManagedSherlockFragmentActivity {

	private final SecondActivity me = this;
	private OnBulbListReturnedListener mBulbListenerFragment;
	private SharedPreferences mSettings;
	private ViewPager mMoodManualViewPager;
	private MoodManualPagerAdapter mMoodManualPagerAdapter;
	private SlidingTabLayout mMoodManualSlidingTabLayout;
	private SeekBar mBrightnessBar;
	private boolean mIsTrackingTouch = false;
	
	public void setBulbListenerFragment(OnBulbListReturnedListener frag){
		mBulbListenerFragment = frag;
	}
	public OnBulbListReturnedListener getBulbListenerFragment(){
		return mBulbListenerFragment;
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
		// Set up the ViewPager, attaching the adapter.
		mMoodManualViewPager = (ViewPager) this.findViewById(R.id.manual_mood_pager);
		mMoodManualViewPager.setAdapter(mMoodManualPagerAdapter);
		
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mMoodManualSlidingTabLayout = (SlidingTabLayout) this.findViewById(R.id.manual_mood_sliding_tabs);
        mMoodManualSlidingTabLayout.setViewPager(mMoodManualViewPager);
        mMoodManualSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.red_color));
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(me);
		if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
			mMoodManualViewPager.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
		}
		mBrightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				MoodExecuterService service = SecondActivity.this.getService();
				service.getDeviceManager().setBrightness(seekBar.getProgress());
				mIsTrackingTouch = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				MoodExecuterService service = SecondActivity.this.getService();
				service.getDeviceManager().setBrightness(seekBar.getProgress());
				mIsTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					MoodExecuterService service = SecondActivity.this.getService();
					service.getDeviceManager().setBrightness(seekBar.getProgress());
				}
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outstate){
		Editor edit = mSettings.edit();
		switch(mMoodManualViewPager.getCurrentItem()){
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
	public void onStateChanged() {
		if(mBrightnessBar!=null && !mIsTrackingTouch){
			DeviceManager dm = this.getService().getDeviceManager();
			Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
			if(candidateBrightness!=null)
				mBrightnessBar.setProgress(candidateBrightness);
		}
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
