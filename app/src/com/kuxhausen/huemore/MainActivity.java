package com.kuxhausen.huemore;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.android.common.view.SlidingTabLayout;
import com.google.gson.Gson;
import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Inventory;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.net.hue.HubData;
import com.kuxhausen.huemore.network.ConnectionStatusDialogFragment;
import com.kuxhausen.huemore.nfc.NfcWriterActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.DeprecatedPreferenceKeys;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;
import com.kuxhausen.huemore.registration.WelcomeDialogFragment;
import com.kuxhausen.huemore.timing.AlarmListActivity;

/**
 * @author Eric Kuxhausen
 */
public class MainActivity extends NetworkManagedSherlockFragmentActivity{
	private final static Gson gson = new Gson();
	
	protected IabHelper mPlayHelper;
	protected Inventory lastQuerriedInventory;
	private final MainActivity me = this;
	private SharedPreferences mSettings;
	private ViewPager mGroupBulbViewPager;
	private GroupBulbPagerAdapter mGroupBulbPagerAdapter;
    private SlidingTabLayout mGroupBulbSlidingTabLayout;
	private MoodManualPagerAdapter mMoodManualPagerAdapter;
	private ViewPager mMoodManualViewPager;
    private SlidingTabLayout mMoodManualSlidingTabLayout;
    private SeekBar mBrightnessBar;
	private boolean mIsTrackingTouch = false;
    
	@Override
	public void onConnectionStatusChanged(boolean connected){
		this.supportInvalidateOptionsMenu();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		
		mGroupBulbPagerAdapter = new GroupBulbPagerAdapter(this);
		// Set up the ViewPager, attaching the adapter.
		mGroupBulbViewPager = (ViewPager) this.findViewById(R.id.bulb_group_pager);
		mGroupBulbViewPager.setAdapter(mGroupBulbPagerAdapter);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(me);
		
		if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false)) {
			if (mGroupBulbViewPager.getCurrentItem() != GroupBulbPagerAdapter.GROUP_LOCATION)
				mGroupBulbViewPager.setCurrentItem(GroupBulbPagerAdapter.GROUP_LOCATION);
		} else {
			if (mGroupBulbViewPager.getCurrentItem() != GroupBulbPagerAdapter.BULB_LOCATION)
				mGroupBulbViewPager.setCurrentItem(GroupBulbPagerAdapter.BULB_LOCATION);
		}
		
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mGroupBulbSlidingTabLayout = (SlidingTabLayout) this.findViewById(R.id.bulb_group_sliding_tabs);
        mGroupBulbSlidingTabLayout.setViewPager(mGroupBulbViewPager);
        mGroupBulbSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.green_color));
		
		
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
					
			mMoodManualPagerAdapter = new MoodManualPagerAdapter(this);
			// Set up the ViewPager, attaching the adapter.
			mMoodManualViewPager = (ViewPager) this.findViewById(R.id.manual_mood_pager);
			mMoodManualViewPager.setAdapter(mMoodManualPagerAdapter);
			
			if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_MOODS, true)) {
				mMoodManualViewPager.setCurrentItem(MoodManualPagerAdapter.MOOD_LOCATION);
			}
			
			// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
	        // it's PagerAdapter set.
	        mMoodManualSlidingTabLayout = (SlidingTabLayout) this.findViewById(R.id.manual_mood_sliding_tabs);
	        mMoodManualSlidingTabLayout.setViewPager(mMoodManualViewPager);
	        mMoodManualSlidingTabLayout.setSelectedIndicatorColors(this.getResources().getColor(R.color.red_color));
			
			
			mBrightnessBar = (SeekBar) this.findViewById(R.id.brightnessBar);
			mBrightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					me.setBrightness(seekBar.getProgress());
					mIsTrackingTouch = false;
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					me.setBrightness(seekBar.getProgress());
					mIsTrackingTouch = true;
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
		initializeBillingCode();
		
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

		Bundle b = this.getIntent().getExtras();
		if (b != null && b.containsKey(InternalArguments.PROMPT_UPGRADE)
				&& b.getBoolean(InternalArguments.PROMPT_UPGRADE)) {
			UnlocksDialogFragment unlocks = new UnlocksDialogFragment();
			unlocks.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		}
		
		//TODO turn off before relase
		com.kuxhausen.huemore.testing.Tests.tests();
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
			i.putExtra(InternalArguments.GROUP_NAME, (me).getCurentGroupName());
			i.putExtra(InternalArguments.GROUP_VALUES, (me).getCurentGroupValues());
			this.startActivity(i);
		 }
	}	
				
	public void invalidateSelection() {
		if ((getResources().getConfiguration().screenLayout &
				 Configuration.SCREENLAYOUT_SIZE_MASK) >=
				 Configuration.SCREENLAYOUT_SIZE_LARGE){
			((MoodListFragment) (mMoodManualPagerAdapter.getItem(MoodManualPagerAdapter.MOOD_LOCATION)))
					.invalidateSelection();
		}
	}
		
	@Override
	public void onBrightnessChanged(int brightness) {
		if(mBrightnessBar!=null && !mIsTrackingTouch)
			mBrightnessBar.setProgress(brightness);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		if (Utils.hasProVersion(this)) {
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
		}

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
			Intent iSet = new Intent(this, SettingsActivity.class);
			this.startActivity(iSet);
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
		case R.id.action_unlocks:
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
				i.putExtra(InternalArguments.GROUP_NAME, (me).getCurentGroupName());
				i.putExtra(InternalArguments.GROUP_VALUES, (me).getCurentGroupValues());
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
			          "&body=" + Uri.encode("the body of the message")*/;
			Uri uri = Uri.parse(uriText);

			send.setData(uri);
			startActivity(Intent.createChooser(send, "Send mail..."));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onDestroy() {
		if (mPlayHelper != null) {
			try {
				mPlayHelper.dispose();
			} catch (IllegalArgumentException e) {
			}
		}
		mPlayHelper = null;
		//Log.d("asdf", "mPlayHelperDestroyed" + (mPlayHelper == null));
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
		// + data);

		// Pass on the activity result to the helper for handling
		if (!mPlayHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			// Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}
	
	private void initializationDatabaseChecks(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if(settings.contains(DeprecatedPreferenceKeys.BRIDGE_IP_ADDRESS)){
			HubData hubData = new HubData();
			hubData.localHubAddress = settings.getString(DeprecatedPreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS, null);
			hubData.portForwardedAddress = settings.getString(DeprecatedPreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS, null);
			hubData.hashedUsername = settings.getString(DeprecatedPreferenceKeys.HASHED_USERNAME, null);
			if(hubData.localHubAddress==null)
				hubData.localHubAddress = settings.getString(DeprecatedPreferenceKeys.BRIDGE_IP_ADDRESS, null);
			
			if(hubData.hashedUsername!=null && (hubData.localHubAddress!=null || hubData.portForwardedAddress!=null)){
				ContentValues cv = new ContentValues();
				cv.put(DatabaseDefinitions.NetConnectionColumns.TYPE_COLUMN, DatabaseDefinitions.NetBulbColumns.NetBulbType.PHILIPS_HUE);
				cv.put(DatabaseDefinitions.NetConnectionColumns.JSON_COLUMN, gson.toJson(hubData));
				this.getContentResolver().insert(DatabaseDefinitions.NetConnectionColumns.URI, cv);
			}
			Editor edit = settings.edit();
			edit.remove(DeprecatedPreferenceKeys.LOCAL_BRIDGE_IP_ADDRESS);
			edit.remove(DeprecatedPreferenceKeys.INTERNET_BRIDGE_IP_ADDRESS);
			edit.remove(DeprecatedPreferenceKeys.BRIDGE_IP_ADDRESS);
			edit.remove(DeprecatedPreferenceKeys.HASHED_USERNAME);
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
					PreferenceKeys.ALWAYS_FREE_BULBS);// TODO load from google store
			edit.commit();
		} else if (settings.getInt(PreferenceKeys.VERSION_NUMBER, -1)< this.getResources().getInteger(R.integer.major_update_version)){
			UpdateChangesDialogFragment ucdf = new UpdateChangesDialogFragment();
			ucdf.show(this.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
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
		String[] columns = {BaseColumns._ID, NetConnectionColumns.TYPE_COLUMN};
		Cursor cursor = getContentResolver().query(NetConnectionColumns.URI, columns, null, null, null);
		if(cursor.getCount()<=0){
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

	private void initializeBillingCode(){
		String firstChunk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPUhHgGEdnpyPMAWgP3Xw/jHkReU1O0n6d4rtcULxOrVl/hcZlOsVyByMIZY5wMD84gmMXjbz8pFb4RymFTP7Yp8LSEGiw6DOXc7ydNd0lbZ4WtKyDEwwaio1wRbRPxdU7/4JBpMCh9L6geYx6nYLt0ExZEFxULV3dZJpIlEkEYaNGk/64gc0l34yybccYfORrWzu8u+";
		String secondChunk = "5YxJ5k1ikIJJ2I7/2Rp5AXkj2dWybmT+AGx83zh8+iMGGawEQerGtso9NUqpyZWU08EO9DcF8r2KnFwjmyWvqJ2JzbqCMNt0A08IGQNOrd16/C/65GE6J/EtsggkNIgQti6jD7zd3b2NAQIDAQAB";
		String base64EncodedPublicKey = firstChunk + secondChunk;
		// compute your public key and store it in base64EncodedPublicKey
		mPlayHelper = new IabHelper(this, base64EncodedPublicKey);
		//Log.d("asdf", "mPlayHelperCreated" + (mPlayHelper != null));
		mPlayHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					// Log.d("asdf", "Problem setting up In-app Billing: "+
					// result);
				} else {
					// Hooray, IAB is fully set up!
					mPlayHelper.queryInventoryAsync(mGotInventoryListener);
				}
			}
		});
	}
	
	// Listener that's called when we finish querying the items and subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {

			// Log.d("asdf", "Query inventory finished.");
			if (result.isFailure()) {
				// handle error
				return;
			} else {
				// Log.d("asdf", "Query inventory was successful.");
				lastQuerriedInventory = inventory;
				int numUnlocked = PreferenceKeys.ALWAYS_FREE_BULBS;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_1))
					numUnlocked = Math.max(50, numUnlocked);
				if (inventory.hasPurchase(PlayItems.BUY_ME_A_BULB_DONATION_1))
					numUnlocked = Math.max(50, numUnlocked);
				// update UI accordingly

				// Get preferences cache
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(me);
				int previousMax = settings.getInt(
						PreferenceKeys.BULBS_UNLOCKED,
						PreferenceKeys.ALWAYS_FREE_BULBS);
				if (numUnlocked > previousMax) {
					// Update the number held in settings
					Editor edit = settings.edit();
					edit.putInt(PreferenceKeys.BULBS_UNLOCKED, numUnlocked);
					edit.commit();

				}
			}
			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */
			/*
			 * // Do we have the premium upgrade? Purchase premiumPurchase =
			 * inventory.getPurchase(SKU_PREMIUM); mIsPremium = (premiumPurchase
			 * != null && verifyDeveloperPayload(premiumPurchase)); Log.d(TAG,
			 * "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
			 * 
			 * 
			 * updateUi(); setWaitScreen(false); Log.d(TAG,
			 * "Initial inventory query finished; enabling main UI.");
			 */
		}
	};

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();
		/*
		 * TODO: verify that the developer payload of the purchase is correct.
		 * It will be the same one that you sent when initiating the purchase.
		 * 
		 * WARNING: Locally generating a random string when starting a purchase
		 * and verifying it here might seem like a good approach, but this will
		 * fail in the case where the user purchases an item on one device and
		 * then uses your app on a different device, because on the other device
		 * you will not have access to the random string you originally
		 * generated.
		 * 
		 * So a good developer payload has these characteristics:
		 * 
		 * 1. If two different users purchase an item, the payload is different
		 * between them, so that one user's purchase can't be replayed to
		 * another user.
		 * 
		 * 2. The payload must be such that you can verify it even when the app
		 * wasn't the one who initiated the purchase flow (so that items
		 * purchased by the user on one device work on other devices owned by
		 * the user).
		 * 
		 * Using your own server to store and verify developer payloads across
		 * app installations is recommended.
		 */
		return true;
	}
}
