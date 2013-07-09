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
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Inventory;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.GetBulbsAttributes;
import com.kuxhausen.huemore.nfc.NfcWriterActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.persistence.DatabaseHelper;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.timing.AlarmListActivity;
import com.kuxhausen.huemore.ui.registration.DiscoverHubDialogFragment;

/**
 * @author Eric Kuxhausen
 * 
 */
public class SecondActivity extends GodObject implements
		MoodsListFragment.OnMoodSelectedListener {

	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	IabHelper mPlayHelper;
	Inventory lastQuerriedInventory;
	public GetBulbList.OnBulbListReturnedListener bulbListenerFragment;
	
	public void setBulbListenerFragment(GetBulbList.OnBulbListReturnedListener frag){
		bulbListenerFragment = frag;
	}
	public GetBulbList.OnBulbListReturnedListener getBulbListenerFragment(){
		return bulbListenerFragment;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.second_activity);
		this.restoreSerialized(this.getIntent().getStringExtra(InternalArguments.SERIALIZED_GOD_OBJECT));
		this.getSupportActionBar().setTitle(this.getGroupS());
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
		parrentActivity = this;
		// Set up the ViewPager, attaching the adapter.
		mViewPager2 = (ViewPager) this.findViewById(R.id.mood_pager);
		mViewPager2.setAdapter(mMoodManualPagerAdapter);
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(parrentActivity);
		if (settings.getBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true)) {
			mViewPager2.setCurrentItem(MOOD_LOCATION);
		}
		brightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				BulbState hs = new BulbState();
				hs.bri = seekBar.getProgress();
				hs.on = true;
				String[] brightnessState = { gson.toJson(hs) };
				parrentActivity.updatePreview(brightnessState);
				isTrackingTouch = false;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				BulbState hs = new BulbState();
				hs.bri = seekBar.getProgress();
				hs.on = true;
				String[] brightnessState = { gson.toJson(hs) };
				parrentActivity.updatePreview(brightnessState);
				isTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					BulbState hs = new BulbState();
					hs.bri = progress;
					hs.on = true;
					String[] brightnessState = { gson.toJson(hs) };
					parrentActivity.updatePreview(brightnessState);
				}
			}
		});

		
	}

	@Override
	public void onGroupBulbSelected(Integer[] bulb, String name) {
		throw new RuntimeException("Not implemented here");
	}

	

	SeekBar brightnessBar;
	boolean isTrackingTouch = false;
	
	MoodManualPagerAdapter mMoodManualPagerAdapter;

	private static final int MOOD_LOCATION = 1;
	private static final int MANUAL_LOCATION = 0;

	private static MoodsListFragment moodsListFragment;
	private static ColorWheelFragment colorWheelFragment;

	ViewPager mViewPager2;
	GodObject parrentActivity;
	
	@Override
	public void onSelected(Integer[] bulbNum, String name,
			GroupsListFragment groups, BulbsFragment bulbs) {
		throw new RuntimeException("Not implemented here");
	}
	
	public static class MoodManualPagerAdapter extends FragmentPagerAdapter {

		GodObject frag;
		
		public MoodManualPagerAdapter(GodObject godObject) {
			super(godObject.getSupportFragmentManager());
			frag = godObject;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case MOOD_LOCATION:
				if (moodsListFragment == null)
					moodsListFragment = new MoodsListFragment();
				return moodsListFragment;
			case MANUAL_LOCATION:
				if (colorWheelFragment == null) {
					colorWheelFragment = new ColorWheelFragment();
					colorWheelFragment.hideTransitionTime();
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
				return frag.getString(R.string.cap_moods);
			case MANUAL_LOCATION:
				return frag.getString(R.string.cap_manual);
			}
			return "";
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void pollBrightness() {
		GetBulbsAttributes getBulbsAttributes = new GetBulbsAttributes(
				parrentActivity, parrentActivity.getBulbs(), this,
				this.parrentActivity);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getBulbsAttributes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			getBulbsAttributes.execute();
		}
	}
	public void invalidateSelection() {
		((MoodsListFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
				.invalidateSelection();
	}
	@Override
	public void onResume() {
		super.onResume();
		pollBrightness();
	}
	@Override
	public void onListReturned(BulbAttributes[] bulbsAttributes) {
		if (!isTrackingTouch && bulbsAttributes != null
				&& bulbsAttributes.length > 0) {
			int brightnessSum = 0;
			int brightnessPool = 0;
			for (BulbAttributes ba : bulbsAttributes) {
				if (ba != null) {
					if (ba.state.on == false)
						brightnessPool++;
					else {
						brightnessSum += ba.state.bri;
						brightnessPool++;
					}
				}
			}
			if (brightnessPool == 0)
				return;
			int brightnessAverage = brightnessSum / brightnessPool;

			brightnessBar.setProgress(brightnessAverage);
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.second, menu);
		if (PreferenceManager.getDefaultSharedPreferences(this).getInt(
				PreferencesKeys.BULBS_UNLOCKED,
				PreferencesKeys.ALWAYS_FREE_BULBS) > PreferencesKeys.ALWAYS_FREE_BULBS) {
			// has pro version

			if (NfcAdapter.getDefaultAdapter(this) == null) {
				// hide nfc link if nfc not supported
				MenuItem nfcItem = menu.findItem(R.id.action_nfc);
				if (nfcItem != null) {
					nfcItem.setEnabled(false);
					nfcItem.setVisible(false);
				}
			}
		} else {
			MenuItem nfcItem = menu.findItem(R.id.action_nfc);
			if (nfcItem != null) {
				nfcItem.setEnabled(false);
				nfcItem.setVisible(false);
			}
			MenuItem alarmItem = menu.findItem(R.id.action_alarms);
			if (alarmItem != null) {
				alarmItem.setEnabled(false);
				alarmItem.setVisible(false);
			}
		}

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
			this.onBackPressed();
			return true;
		case R.id.action_settings:
			SettingsDialogFragment settings = new SettingsDialogFragment();
			settings.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_add_both:
			AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
			addBoth.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_nfc:
			if (!NfcAdapter.getDefaultAdapter(this).isEnabled()) {
				Toast.makeText(this, this.getString(R.string.nfc_disabled),
						Toast.LENGTH_SHORT).show();
			} else {
				Intent i = new Intent(this, NfcWriterActivity.class);
				this.startActivity(i);
			}
			return true;
		case R.id.action_alarms:
			Intent i = new Intent(this, AlarmListActivity.class);
			this.startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
