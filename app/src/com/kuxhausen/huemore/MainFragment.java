package com.kuxhausen.huemore;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.example.android.common.view.SlidingTabLayout;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.OnConnectionStatusChangedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

/**
 * @author Eric Kuxhausen
 */
public class MainFragment extends Fragment implements OnConnectionStatusChangedListener, OnServiceConnectedListener{
	
	private NavigationDrawerActivity mParent;
	
	private SharedPreferences mSettings;
	private ViewPager mGroupBulbViewPager;
	private GroupBulbPagerAdapter mGroupBulbPagerAdapter;
    private SlidingTabLayout mGroupBulbSlidingTabLayout;
	private MoodManualPagerAdapter mMoodManualPagerAdapter;
	private ViewPager mMoodManualViewPager;
    private SlidingTabLayout mMoodManualSlidingTabLayout;
    private SeekBar mBrightnessBar;
	private boolean mIsTrackingTouch = false;
    private ForwardingPageListener mForwardPage;
	
	/*@Override
	public void onConnectionStatusChanged(){
		this.supportInvalidateOptionsMenu();
	}*/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.main_activity, null);
		
		mParent = (NavigationDrawerActivity) this.getActivity();
		
		mGroupBulbPagerAdapter = new GroupBulbPagerAdapter(this);
		// Set up the ViewPager, attaching the adapter.
		mGroupBulbViewPager = (ViewPager) myView.findViewById(R.id.bulb_group_pager);
		mGroupBulbViewPager.setAdapter(mGroupBulbPagerAdapter);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(mParent);
		
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mGroupBulbSlidingTabLayout = (SlidingTabLayout) myView.findViewById(R.id.bulb_group_sliding_tabs);
        mGroupBulbSlidingTabLayout.setViewPager(mGroupBulbViewPager);
        mGroupBulbSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.greenwidgets_color));
		
        //add custom page changed lister to sych bulb/group tabs with nav drawer
        mForwardPage = new ForwardingPageListener();
        mGroupBulbSlidingTabLayout.setOnPageChangeListener(mForwardPage);
        
        //Switch to preferred tab (after setting tab listeners)
        if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false)) {
			if (mGroupBulbViewPager.getCurrentItem() != GroupBulbPagerAdapter.GROUP_LOCATION)
				mGroupBulbViewPager.setCurrentItem(GroupBulbPagerAdapter.GROUP_LOCATION);
		} else {
			if (mGroupBulbViewPager.getCurrentItem() != GroupBulbPagerAdapter.BULB_LOCATION)
				mGroupBulbViewPager.setCurrentItem(GroupBulbPagerAdapter.BULB_LOCATION);
		}
		
        
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
					
			mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
			// Set up the ViewPager, attaching the adapter.
			mMoodManualViewPager = (ViewPager) myView.findViewById(R.id.manual_mood_pager);
			mMoodManualViewPager.setAdapter(mMoodManualPagerAdapter);
			
			if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
				mMoodManualViewPager.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
			}
			
			// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
	        // it's PagerAdapter set.
	        mMoodManualSlidingTabLayout = (SlidingTabLayout) myView.findViewById(R.id.manual_mood_sliding_tabs);
	        mMoodManualSlidingTabLayout.setViewPager(mMoodManualViewPager);
	        mMoodManualSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.redwidgets_color));
			
			
			mBrightnessBar = (SeekBar) myView.findViewById(R.id.brightnessBar);
			mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					DeviceManager dm = mParent.getService().getDeviceManager();
					dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
					mIsTrackingTouch = false;
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					DeviceManager dm = mParent.getService().getDeviceManager();
					dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
					mIsTrackingTouch = true;
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					if(fromUser){
						DeviceManager dm = mParent.getService().getDeviceManager();
						dm.setBrightness(dm.getSelectedGroup(), seekBar.getProgress());
					}
				}
			});
		 }
		return myView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mParent.registerOnServiceConnectedListener(this);
		this.setHasOptionsMenu(true);
	}
	@Override
	public void onServiceConnected() {
		mParent.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
	}
	public void onPause(){
		super.onPause();
		mParent.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outstate){
		Editor edit = mSettings.edit();
		switch(mGroupBulbViewPager.getCurrentItem()){
			case GroupBulbPagerAdapter.BULB_LOCATION:
				edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false);
				break;
			case GroupBulbPagerAdapter.GROUP_LOCATION:
				edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, true);
				break;
		}
		if(mMoodManualViewPager!=null){
			switch(mMoodManualViewPager.getCurrentItem()){
				case MoodManualPagerAdapter.MANUAL_LOCATION:
					edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, false);
					break;
				case MoodManualPagerAdapter.MOOD_LOCATION:
					edit.putBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true);
					break;
			}
		}
		edit.commit();
		super.onSaveInstanceState(outstate);
	}
				
	public void invalidateSelection() {
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
			((MoodListFragment) (mMoodManualPagerAdapter.getItem(MoodManualPagerAdapter.MOOD_LOCATION)))
					.invalidateSelection();
		}
	}
	
	public void setTab(int tabNum){
		if(mGroupBulbViewPager!=null)
			mGroupBulbViewPager.setCurrentItem(tabNum);
	}
	
	public void onConnectionStatusChanged() {
		if(mBrightnessBar!=null && !mIsTrackingTouch){
			DeviceManager dm = mParent.getService().getDeviceManager();
			Integer candidateBrightness = dm.getBrightness(dm.getSelectedGroup());
			if(candidateBrightness!=null)
				mBrightnessBar.setProgress(candidateBrightness);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
	
		/*if (Utils.hasProVersion(parrentA)) {
			// has pro version

			// hide unlocks button
			MenuItem unlocksItem = menu.findItem(R.id.action_unlocks);
			if (unlocksItem != null) {
				unlocksItem.setEnabled(false);
				unlocksItem.setVisible(false);
			}
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
		}*/

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
			MenuItem bothItem = menu.findItem(R.id.action_add_both);
			if (bothItem != null) {
				bothItem.setEnabled(false);
				bothItem.setVisible(false);
			}
		}
		
		/*if(this.boundToService() && this.getService().getDeviceManager().isFully){
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
		}*/
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		/*case R.id.action_connected_with_hub:
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
			Intent iSet = new Intent(this, SettingsActivity.class);
			this.startActivity(iSet);
			return true;
		*/case R.id.action_communities:
			CommunityDialogFragment communities = new CommunityDialogFragment();
			communities.show(mParent.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.action_add_both:
			AddMoodGroupSelectorDialogFragment addBoth = new AddMoodGroupSelectorDialogFragment();
			addBoth.show(mParent.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		/*case R.id.action_unlocks:
			UnlocksDialogFragment unlocks = new UnlocksDialogFragment();
			unlocks.show(getSupportFragmentManager(),
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
			          "&body=" + Uri.encode("the body of the message")*//*;
			Uri uri = Uri.parse(uriText);

			send.setData(uri);
			startActivity(Intent.createChooser(send, "Send mail..."));
			return true;
		*/default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	class ForwardingPageListener extends SimpleOnPageChangeListener{

		@Override
		public void onPageSelected(int arg0) {
			
			if(arg0 == GroupBulbPagerAdapter.BULB_LOCATION){
				mParent.markSelected(NavigationDrawerActivity.BULB_FRAG);
			} else if(arg0 == GroupBulbPagerAdapter.GROUP_LOCATION){
				mParent.markSelected(NavigationDrawerActivity.GROUP_FRAG);
			}
		}
		
	}
}
