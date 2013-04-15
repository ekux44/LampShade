package com.kuxhausen.huemore;

import java.util.ArrayList;
import java.util.BitSet;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.kuxhausen.huemore.billing.IabHelper;
import com.kuxhausen.huemore.billing.IabResult;
import com.kuxhausen.huemore.billing.Inventory;
import com.kuxhausen.huemore.billing.Purchase;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.network.TransmitGroupMood;
import com.kuxhausen.huemore.nfc.NfcWriterActivity;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PlayItems;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.persistence.DatabaseHelper;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.ui.registration.RegisterWithHubDialogFragment;

/**
 * @author Eric Kuxhausen
 * 
 */
public class MainActivity extends FragmentActivity implements
		GroupBulbPagingFragment.OnBulbGroupSelectedListener,
		MoodsFragment.OnMoodSelectedListener {

	DatabaseHelper databaseHelper = new DatabaseHelper(this);
	Integer[] bulbS;
	String mood;
	IabHelper mPlayHelper;
	MainActivity m;
	Inventory lastQuerriedInventory;
	public GetBulbList.OnBulbListReturnedListener bulbListenerFragment;
	private NfcAdapter nfcAdapter;
	
	
	public void tests(){
		/** bitSet to encoding test **/
		{
			BitSet b = new BitSet();
			for(int i= 0; i<10000; i++){
				if(Math.random()<.5)
					b.set(i);
			}
			byte[] intermediate= com.kuxhausen.huemore.nfc.HueNfcEncoder.fromBitSet(b, 10000);
			BitSet b2 = com.kuxhausen.huemore.nfc.HueNfcEncoder.toBitSet(intermediate);
			for(int i = 0; i<10000; i++)
				if(b.get(i)!=b2.get(i))
					System.out.println(i+" "+b.get(i)+" "+b2.get(i)+" "+intermediate[i/8]);
			System.out.println("bitSet-Byte[] testComplete");
		}
		{
			Integer[] bulbs = {1, 4, 8, 3, 43};
			BulbState[] bsRay = new BulbState[4];
			BulbState one = new BulbState();
			one.on= true;
			one.bri=0;
			one.ct=500;
			bsRay[0]=one;
			BulbState two = new BulbState();
			two.on= false;
			bsRay[1]=two;
			BulbState three = new BulbState();
			three.on= true;
			three.sat=255;
			three.bri=255;
			three.hue=0;
			bsRay[2]=three;
			BulbState four = new BulbState();
			four.on= false;
			four.bri=0;
			//four.transitiontime =0;
			bsRay[3]=four;
			String interm = com.kuxhausen.huemore.nfc.HueNfcEncoder.encode(bulbs, bsRay);
			System.out.println(interm);
			Pair<Integer[], BulbState[]> results = com.kuxhausen.huemore.nfc.HueNfcEncoder.decode(interm);
			System.out.println("resultSize"+results.first.length+"  "+results.second.length);
			
			for(int i: results.first){
				System.out.print(i+"  ");
			}
			System.out.println();
			for(BulbState j: results.second){
				if(j!=null)
					System.out.println(j);
				else
					System.out.println("wtf-null ");
			}
		}
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("asdf", "onCreate");

		setContentView(R.layout.hue_more);
		m = this;
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
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
				GroupBulbPagingFragment firstFragment = new GroupBulbPagingFragment();
				// GroupsFragment firstFragment = new GroupsFragment();

				// In case this activity was started with special instructions
				// from
				// an Intent,
				// pass the Intent's extras to the fragment as arguments
				firstFragment.setArguments(getIntent().getExtras());

				// Add the fragment to the 'fragment_container' FrameLayout
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, firstFragment,
								GroupBulbPagingFragment.class.getName())
						.commit();
			}

		}

		// (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
		// this.getActionBar().setDisplayHomeAsUpEnabled(true)
		// : System.out.println("wtf");
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!settings.contains(PreferencesKeys.THIRD_UPDATE)) {
			databaseHelper.updatedPopulate();
			// Mark no longer first update in preferences cache
			Editor edit = settings.edit();
			edit.putBoolean(PreferencesKeys.THIRD_UPDATE, false);
			edit.commit();
		}
		if (!settings.contains(PreferencesKeys.FIRST_RUN)) {
			databaseHelper.initialPopulate();// initialize database

			// Mark no longer first run in preferences cache
			Editor edit = settings.edit();
			edit.putBoolean(PreferencesKeys.FIRST_RUN, false);
			edit.putInt(PreferencesKeys.BULBS_UNLOCKED,
					PreferencesKeys.ALWAYS_FREE_BULBS);// TODO load from
			// google store
			edit.commit();
		}

		if (!settings.contains(PreferencesKeys.DEFAULT_TO_GROUPS)) {
			Editor edit = settings.edit();
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_GROUPS, false);
			edit.commit();
		}
		if (!settings.contains(PreferencesKeys.DEFAULT_TO_MOODS)) {
			Editor edit = settings.edit();
			edit.putBoolean(PreferencesKeys.DEFAULT_TO_MOODS, true);
			edit.commit();
		}

		// check to see if the bridge IP address is setup yet
		if (!settings.contains(PreferencesKeys.BRIDGE_IP_ADDRESS)) {
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			rwhdf.show(this.getSupportFragmentManager(), "dialog");
		}
		String firstChunk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPUhHgGEdnpyPMAWgP3Xw/jHkReU1O0n6d4rtcULxOrVl/hcZlOsVyByMIZY5wMD84gmMXjbz8pFb4RymFTP7Yp8LSEGiw6DOXc7ydNd0lbZ4WtKyDEwwaio1wRbRPxdU7/4JBpMCh9L6geYx6nYLt0ExZEFxULV3dZJpIlEkEYaNGk/64gc0l34yybccYfORrWzu8u+";
		String secondChunk = "5YxJ5k1ikIJJ2I7/2Rp5AXkj2dWybmT+AGx83zh8+iMGGawEQerGtso9NUqpyZWU08EO9DcF8r2KnFwjmyWvqJ2JzbqCMNt0A08IGQNOrd16/C/65GE6J/EtsggkNIgQti6jD7zd3b2NAQIDAQAB";
		String base64EncodedPublicKey = firstChunk + secondChunk;
		// compute your public key and store it in base64EncodedPublicKey
		mPlayHelper = new IabHelper(this, base64EncodedPublicKey);
		Log.d("asdf", "mPlayHelperCreated" + (mPlayHelper != null));
		mPlayHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					// Log.d("asdf", "Problem setting up In-app Billing: "+
					// result);
				} else {
					// Hooray, IAB is fully set up!
					mPlayHelper.queryInventoryAsync(mGotInventoryListener);
					if (m.bulbListenerFragment != null) {
						GetBulbList pushGroupMood = new GetBulbList(m,
								m.bulbListenerFragment);
						pushGroupMood.execute();
					}
				}
			}
		});
		tests();
	}

	// Listener that's called when we finish querying the items and
	// subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {

			// Log.d("asdf", "Query inventory finished.");
			if (result.isFailure()) {
				// handle error
				return;
			} else {
				// Log.d("asdf", "Query inventory was successful.");
				lastQuerriedInventory = inventory;
				int numUnlocked = PreferencesKeys.ALWAYS_FREE_BULBS;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_1))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_2))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_3))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_4))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_5))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_6))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_7))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.FIVE_BULB_UNLOCK_8))
					numUnlocked += 5;
				if (inventory.hasPurchase(PlayItems.BUY_ME_A_BULB_DONATION_1))
					numUnlocked = Math.max(50, numUnlocked);
				// update UI accordingly

				// Get preferences cache
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(m);
				int previousMax = settings.getInt(
						PreferencesKeys.BULBS_UNLOCKED,
						PreferencesKeys.ALWAYS_FREE_BULBS);
				if (numUnlocked > previousMax) {
					// Update the number held in settings
					Editor edit = settings.edit();
					edit.putInt(PreferencesKeys.BULBS_UNLOCKED, numUnlocked);
					edit.commit();

					databaseHelper.addBulbs(previousMax, numUnlocked);// initialize
																		// database
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void initializeActionBar(Boolean value) {
		try {
			this.getActionBar().setDisplayHomeAsUpEnabled(value);
		} catch (Error e) {
		}
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("asdf", "onDestroy");
		if (mPlayHelper != null)
			mPlayHelper.dispose();
		mPlayHelper = null;
		Log.d("asdf", "mPlayHelperDestroyed" + (mPlayHelper == null));
	}

	@Override
	public void onGroupBulbSelected(Integer[] bulb) {
		bulbS = bulb;

		// Capture the article fragment from the activity layout
		MoodManualPagingFragment moodFrag = (MoodManualPagingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.moods_fragment);

		if (moodFrag != null) {
			// If article frag is available, we're in two-pane layout...

			// Call a method in the ArticleFragment to update its content
			moodFrag.reset();

		} else {
			// If the frag is not available, we're in the one-pane layout and
			// must swap frags...

			// Create fragment and give it an argument for the selected article
			MoodManualPagingFragment newFragment = new MoodManualPagingFragment();
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, newFragment,
					MoodManualPagingFragment.class.getName());
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				initializeActionBar(true);

			}
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			initializeActionBar(false);
		}
	}

	private void moveToGroupBulb() {
		MoodManualPagingFragment moodFrag = (MoodManualPagingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.moods_fragment);

		if (moodFrag == null || !moodFrag.isVisible()) {
			this.onBackPressed();

		}
	}

	@Override
	public void onMoodSelected(String moodParam) {
		mood = moodParam;
		pushMoodGroup();
	}

	public void onBrightnessChanged(String brightnessState[]) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				brightnessState);
		pushGroupMood.execute();
	}

	/*
	 * test mood by applying to json states array to these bulbs
	 * 
	 * @param states
	 */
	/*
	 * public void testMood(Integer[] bulbs, String[] states) {
	 * TransmitGroupMood pushGroupMood = new TransmitGroupMood();
	 * pushGroupMood.execute(this, bulbs, states); }
	 */

	/**
	 * test mood by applying to json states array to previously selected moods
	 * 
	 * @param states
	 */
	public void testMood(String[] states) {
		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				states);
		pushGroupMood.execute();
	}

	private void pushMoodGroup() {
		if (bulbS == null || mood == null)
			return;

		String[] moodColumns = { MoodColumns.STATE };
		String[] mWereClause = { mood };
		Cursor cursor = getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use the
																// default
																// content URI
																// for the
																// provider.
				moodColumns, // Return the note ID and title for each note.
				MoodColumns.MOOD + "=?", // selection clause
				mWereClause, // election clause args
				null // Use the default sort order.
				);

		ArrayList<String> moodStates = new ArrayList<String>();
		while (cursor.moveToNext()) {
			moodStates.add(cursor.getString(0));
		}
		String[] moodS = moodStates.toArray(new String[moodStates.size()]);

		TransmitGroupMood pushGroupMood = new TransmitGroupMood(this, bulbS,
				moodS);
		pushGroupMood.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		if(nfcAdapter==null){
			//hide nfc link if nfc not supported
			MenuItem menuItem = menu.findItem(R.id.action_nfc);
			menuItem.setEnabled(false);
			menuItem.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			moveToGroupBulb();
			return true;
		case R.id.action_register_with_hub:
			RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			rwhdf.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.action_settings:
			Settings settings = new Settings();
			settings.show(getSupportFragmentManager(), "dialog");
			return true;
		case R.id.action_unlocks:
			Unlocks unlocks = new Unlocks();
			unlocks.show(getSupportFragmentManager(), "dialog");
		case R.id.action_nfc:
			if(!nfcAdapter.isEnabled()){
				//startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
				Toast t = new Toast(this);
				t.makeText(this, "NFC not enabled. Turn on in device settings.", Toast.LENGTH_SHORT);
				t.show();
			}else{
			Intent i = new Intent(this, NfcWriterActivity.class);
			this.startActivity(i);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
