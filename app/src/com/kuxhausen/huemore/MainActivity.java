package com.kuxhausen.huemore;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.kuxhausen.huemore.MoodExecuterService.OnBrightnessChangedListener;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.network.ConnectionStatusDialogFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.DatabaseHelper;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;
import com.kuxhausen.huemore.registration.WelcomeDialogFragment;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

/**
 * @author Eric Kuxhausen
 * 
 */
public class MainActivity extends NetworkManagedSherlockFragmentActivity{

	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	private MainActivity me;
	
	@Override
	public void onConnectionStatusChanged(boolean connected){
		this.supportInvalidateOptionsMenu();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.hue_more);
		me = this;
		
		mGroupBulbPagerAdapter = new GroupBulbPagerAdapter(this);
		parrentActivity = this;
		// Set up the ViewPager, attaching the adapter.
		mViewPager1 = (ViewPager) this.findViewById(R.id.group_pager);
		mViewPager1.setAdapter(mGroupBulbPagerAdapter);
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(parrentActivity);
		if (settings.getBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false)) {
			if (mViewPager1.getCurrentItem() != GROUP_LOCATION)
				mViewPager1.setCurrentItem(GROUP_LOCATION);
		} else {
			if (mViewPager1.getCurrentItem() != BULB_LOCATION)
				mViewPager1.setCurrentItem(BULB_LOCATION);
		}
		
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
					
			mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
			parrentActivity = this;
			// Set up the ViewPager, attaching the adapter.
			mViewPager2 = (ViewPager) this.findViewById(R.id.mood_pager);
			mViewPager2.setAdapter(mMoodManualPagerAdapter);
			
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
		
		
		initializationDatabaseChecks();
		
		/*
		Calendar currentTime = Calendar.getInstance();
		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.MONTH, Calendar.NOVEMBER); //TODO remember to change this when releasing new versions
		
		if(currentTime.after(updateTime) && !settings.getBoolean(PreferenceKeys.UPDATE_OPT_OUT, false)){
			PromptUpdateDialogFragment updates = new PromptUpdateDialogFragment();
			updates.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		}
		*/
		
		//TODO turn off before relase
		com.kuxhausen.huemore.testing.Tests.tests();
	}

	@Override
	public void setGroup(int[] bulbs, String optionalName){
		super.setGroup(bulbs, optionalName);
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
			invalidateSelection();
		 }else if( this.boundToService()){
			// only load the moods page if the group has been sent to the service
			Intent i = new Intent(this, SecondActivity.class);
			i.putExtra(InternalArguments.MOOD_NAME, optionalName);
			i.putExtra(InternalArguments.GROUP_VALUES, bulbs);
			this.startActivity(i);
		 }
	}
	
	GroupBulbPagerAdapter mGroupBulbPagerAdapter;

	private static final int GROUP_LOCATION = 1;
	private static final int BULB_LOCATION = 0;

	private static GroupListFragment groupListFragment;
	private static BulbListFragment bulbListFragment;

	ViewPager mViewPager1;
	NetworkManagedSherlockFragmentActivity parrentActivity;
		
	public static class GroupBulbPagerAdapter extends FragmentPagerAdapter {

		NetworkManagedSherlockFragmentActivity frag;

		public GroupBulbPagerAdapter(NetworkManagedSherlockFragmentActivity godObject) {
			super(godObject.getSupportFragmentManager());
			frag = godObject;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case GROUP_LOCATION:
				if (groupListFragment == null) {
					groupListFragment = new GroupListFragment();
				}
				return groupListFragment;
			case BULB_LOCATION:
				if (bulbListFragment == null) {
					bulbListFragment = new BulbListFragment();
				}
				return bulbListFragment;
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
			case GROUP_LOCATION:
				return frag.getString(R.string.groups).toUpperCase();
			case BULB_LOCATION:
				return frag.getString(R.string.cap_bulbs);

			}
			return "";
		}
	}
	
	SeekBar brightnessBar;
	boolean isTrackingTouch = false;
	
	MoodManualPagerAdapter mMoodManualPagerAdapter;

	private static final int MOOD_LOCATION = 1;
	private static final int MANUAL_LOCATION = 0;

	private static MoodListFragment moodListFragment;
	private static ColorWheelFragment colorWheelFragment;

	ViewPager mViewPager2;
	
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
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
			((MoodListFragment) (mMoodManualPagerAdapter.getItem(MOOD_LOCATION)))
					.invalidateSelection();
		}
	}
		
	@Override
	public void onBrightnessChanged(int brightness) {
		if(brightnessBar!=null && !isTrackingTouch)
			brightnessBar.setProgress(brightness);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
			MenuItem bothItem = menu.findItem(R.id.action_add_both);
			if (bothItem != null) {
				bothItem.setEnabled(false);
				bothItem.setVisible(false);
			}
		}
		
		if(this.boundToService() && this.getService().hasHubConnection()){
			MenuItem connectionErrorItem = menu.findItem(R.id.action_register_with_hub);
			if (connectionErrorItem != null) {
				connectionErrorItem.setEnabled(false);
				connectionErrorItem.setVisible(false);
			}
		}else{
			MenuItem connectionItem = menu.findItem(R.id.action_connected_with_hub);
			if (connectionItem != null) {
				connectionItem.setEnabled(false);
				connectionItem.setVisible(false);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_connected_with_hub:
			ConnectionStatusDialogFragment csdf1 = new ConnectionStatusDialogFragment();
			csdf1.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_register_with_hub:
			ConnectionStatusDialogFragment csdf2 = new ConnectionStatusDialogFragment();
			csdf2.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_settings:
			SettingsDialogFragment settings = new SettingsDialogFragment();
			settings.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_communities:
			CommunityDialogFragment communities = new CommunityDialogFragment();
			communities.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_add_both:
			AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
			addBoth.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_report_bug:
			Intent send = new Intent(Intent.ACTION_SENDTO);
			String versionName = "";
			try {
				versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NotFoundException e) {
			} catch (NameNotFoundException e) {
			}
			String uriText = "mailto:" + Uri.encode(getResources().getString(R.string.developer_email)) + 
			          "?subject=" + Uri.encode(getResources().getString(R.string.app_name)
			        		  + " " + versionName
			        		  + " " + getResources().getString(R.string.report_bug_email_subject))/* + 
			          "&body=" + Uri.encode("the body of the message")*/;
			Uri uri = Uri.parse(uriText);

			send.setData(uri);
			startActivity(Intent.createChooser(send, "Send mail..."));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void initializationDatabaseChecks(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if (settings.contains(PreferenceKeys.BRIDGE_IP_ADDRESS) && !settings.contains(PreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS)) {
			Editor edit = settings.edit();
			edit.putString(PreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS, settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS, null));
			edit.commit();
		}
		
		/*{ //debug mode only
			Editor edit = settings.edit();
			edit.putInt(PreferencesKeys.BULBS_UNLOCKED, 50);
			edit.commit();
		}*/
		
		if (!settings.contains(PreferenceKeys.FIRST_RUN)) {
			// Mark no longer first run in preferences cache
			Editor edit = settings.edit();
			edit.putBoolean(PreferenceKeys.FIRST_RUN, false);
			edit.putInt(PreferenceKeys.BULBS_UNLOCKED,
					PreferenceKeys.ALWAYS_FREE_BULBS);// TODO load from
			// google store
			edit.commit();
		} 
		if (!settings.contains(PreferenceKeys.DEFAULT_TO_GROUPS)) {
			Editor edit = settings.edit();
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, true);
			edit.commit();
		}
		if (!settings.contains(PreferenceKeys.DEFAULT_TO_MOODS)) {
			Editor edit = settings.edit();
			edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
			edit.commit();
		}

		// check to see if the bridge IP address is setup yet
		if (!settings.contains(PreferenceKeys.BRIDGE_IP_ADDRESS)) {
			if(!settings.contains(PreferenceKeys.DONE_WITH_WELCOME_DIALOG))
			{
				WelcomeDialogFragment wdf = new WelcomeDialogFragment();
				wdf.show(this.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			}else{
				DiscoverHubDialogFragment dhdf = new DiscoverHubDialogFragment();
				dhdf.show(this.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			}
		} else if(!settings.contains(PreferenceKeys.HAS_SHOWN_COMMUNITY_DIALOG)){
			CommunityDialogFragment cdf = new CommunityDialogFragment();
			cdf.show(this.getSupportFragmentManager(),
				InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			
		}
		if (!settings.contains(PreferenceKeys.NUMBER_OF_CONNECTED_BULBS)) {
			Editor edit = settings.edit();
			edit.putInt(PreferenceKeys.NUMBER_OF_CONNECTED_BULBS,1);
			edit.commit();
		}
		
		Editor edit = settings.edit();
		try {
			edit.putInt(PreferenceKeys.VERSION_NUMBER, this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
		} catch (NameNotFoundException e) {
		}
		edit.commit();
	}
}
