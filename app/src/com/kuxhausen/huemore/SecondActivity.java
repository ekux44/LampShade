package com.kuxhausen.huemore;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.MoodExecuterService.OnBrightnessChangedListener;
import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Inventory;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.nfc.NfcWriterActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.DatabaseHelper;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.timing.AlarmListActivity;

/**
 * @author Eric Kuxhausen
 * 
 */
public class SecondActivity extends NetworkManagedSherlockFragmentActivity {

	public SecondActivity me = this;
	
	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	IabHelper mPlayHelper;
	Inventory lastQuerriedInventory;
	public OnBulbListReturnedListener bulbListenerFragment;
	
	/**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mManualMoodSlidingTabLayout;
 
	
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
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(parrentActivity);
		if (settings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
			mViewPager2.setCurrentItem(MOOD_LOCATION);
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

	SeekBar brightnessBar;
	boolean isTrackingTouch = false;
	
	MoodManualPagerAdapter mMoodManualPagerAdapter;

	private static final int MOOD_LOCATION = 1;
	private static final int MANUAL_LOCATION = 0;

	private static MoodListFragment moodListFragment;
	private static ColorWheelFragment colorWheelFragment;

	ViewPager mViewPager2;
	NetworkManagedSherlockFragmentActivity parrentActivity;
	
	public static class MoodManualPagerAdapter extends FragmentPagerAdapter {

		NetworkManagedSherlockFragmentActivity frag;
		
		public MoodManualPagerAdapter(NetworkManagedSherlockFragmentActivity godObject) {
			super(godObject.getSupportFragmentManager());
			frag = godObject;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case MOOD_LOCATION:
				if (moodListFragment == null)
					moodListFragment = new MoodListFragment();
				return moodListFragment;
			case MANUAL_LOCATION:
				if (colorWheelFragment == null) {
					colorWheelFragment = new ColorWheelFragment();
				}
				return colorWheelFragment;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case MOOD_LOCATION:
				return frag.getString(R.string.moods).toUpperCase();
			case MANUAL_LOCATION:
				return frag.getString(R.string.cap_manual);
			}
			return "";
		}
	}
	
	public void invalidateSelection() {
		((MoodListFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
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
//		if (Utils.hasProVersion(this)) {
//			// has pro version
//
//			if (NfcAdapter.getDefaultAdapter(this) == null) {
//				// hide nfc link if nfc not supported
//				MenuItem nfcItem = menu.findItem(R.id.action_nfc);
//				if (nfcItem != null) {
//					nfcItem.setEnabled(false);
//					nfcItem.setVisible(false);
//				}
//			}
//		} else {
//			MenuItem nfcItem = menu.findItem(R.id.action_nfc);
//			if (nfcItem != null) {
//				nfcItem.setEnabled(false);
//				nfcItem.setVisible(false);
//			}
//			MenuItem alarmItem = menu.findItem(R.id.action_alarms);
//			if (alarmItem != null) {
//				alarmItem.setEnabled(false);
//				alarmItem.setVisible(false);
//			}
//		}

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
//		case R.id.action_settings:
//			SettingsDialogFragment settings = new SettingsDialogFragment();
//			settings.show(getSupportFragmentManager(),
//					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
//			return true;
		case R.id.action_add_both:
			AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
			addBoth.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
//		case R.id.action_nfc:
//			if (!NfcAdapter.getDefaultAdapter(this).isEnabled()) {
//				Toast.makeText(this, this.getString(R.string.nfc_disabled),
//						Toast.LENGTH_SHORT).show();
//			} else {
//				Intent i = new Intent(this, NfcWriterActivity.class);
//				this.startActivity(i);
//			}
//			return true;
//		case R.id.action_alarms:
//			Intent i = new Intent(this, AlarmListActivity.class);
//			this.startActivity(i);
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
