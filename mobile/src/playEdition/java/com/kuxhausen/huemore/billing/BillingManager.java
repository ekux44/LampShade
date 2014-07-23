package com.kuxhausen.huemore.billing;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.kuxhausen.huemore.billing.googleplay.IabHelper;
import com.kuxhausen.huemore.billing.googleplay.IabResult;
import com.kuxhausen.huemore.billing.googleplay.Inventory;
import com.kuxhausen.huemore.billing.googleplay.Purchase;
import com.kuxhausen.huemore.persistence.Definitions.PlayItems;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;

public class BillingManager {

  protected IabHelper mPlayHelper;
  protected Inventory lastQuerriedInventory;
  protected Context mContext;

  public BillingManager(Context c) {
    mContext = c;
    initializeBillingCode();
  }

  public void onDestroy() {
    if (mPlayHelper != null) {
      try {
        mPlayHelper.dispose();
      } catch (IllegalArgumentException e) {
      }
    }
    mPlayHelper = null;
    // Log.d("asdf", "mPlayHelperDestroyed" + (mPlayHelper == null));

  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
    // + data);

    // Pass on the activity result to the helper for handling
    if (!mPlayHelper.handleActivityResult(requestCode, resultCode, data)) {
      // not handled, so handle it ourselves (here's where you'd
      // perform any handling of activity results not related to in-app
      // billing...

    } else {
      // Log.d(TAG, "onActivityResult handled by IABUtil.");
    }
  }



  private void initializeBillingCode() {
    String firstChunk =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPUhHgGEdnpyPMAWgP3Xw/jHkReU1O0n6d4rtcULxOrVl/hcZlOsVyByMIZY5wMD84gmMXjbz8pFb4RymFTP7Yp8LSEGiw6DOXc7ydNd0lbZ4WtKyDEwwaio1wRbRPxdU7/4JBpMCh9L6geYx6nYLt0ExZEFxULV3dZJpIlEkEYaNGk/64gc0l34yybccYfORrWzu8u+";
    String secondChunk =
        "5YxJ5k1ikIJJ2I7/2Rp5AXkj2dWybmT+AGx83zh8+iMGGawEQerGtso9NUqpyZWU08EO9DcF8r2KnFwjmyWvqJ2JzbqCMNt0A08IGQNOrd16/C/65GE6J/EtsggkNIgQti6jD7zd3b2NAQIDAQAB";
    String base64EncodedPublicKey = firstChunk + secondChunk;
    // compute your public key and store it in base64EncodedPublicKey
    mPlayHelper = new IabHelper(mContext, base64EncodedPublicKey);
    // Log.d("asdf", "mPlayHelperCreated" + (mPlayHelper != null));
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
  protected IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
      new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

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
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
            int previousMax =
                settings.getInt(PreferenceKeys.BULBS_UNLOCKED, PreferenceKeys.ALWAYS_FREE_BULBS);
            if (numUnlocked > previousMax) {
              // Update the number held in settings
              Editor edit = settings.edit();
              edit.putInt(PreferenceKeys.BULBS_UNLOCKED, numUnlocked);
              edit.commit();

            }
          }
          /*
           * Check for items we own. Notice that for each purchase, we check the developer payload
           * to see if it's correct! See verifyDeveloperPayload().
           */
          /*
           * // Do we have the premium upgrade? Purchase premiumPurchase =
           * inventory.getPurchase(SKU_PREMIUM); mIsPremium = (premiumPurchase != null &&
           * verifyDeveloperPayload(premiumPurchase)); Log.d(TAG, "User is " + (mIsPremium ?
           * "PREMIUM" : "NOT PREMIUM"));
           * 
           * 
           * updateUi(); setWaitScreen(false); Log.d(TAG,
           * "Initial inventory query finished; enabling main UI.");
           */
        }
      };

  /** Verifies the developer payload of a purchase. */
  private boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();
    /*
     * TODO: verify that the developer payload of the purchase is correct. It will be the same one
     * that you sent when initiating the purchase.
     * 
     * WARNING: Locally generating a random string when starting a purchase and verifying it here
     * might seem like a good approach, but this will fail in the case where the user purchases an
     * item on one device and then uses your app on a different device, because on the other device
     * you will not have access to the random string you originally generated.
     * 
     * So a good developer payload has these characteristics:
     * 
     * 1. If two different users purchase an item, the payload is different between them, so that
     * one user's purchase can't be replayed to another user.
     * 
     * 2. The payload must be such that you can verify it even when the app wasn't the one who
     * initiated the purchase flow (so that items purchased by the user on one device work on other
     * devices owned by the user).
     * 
     * Using your own server to store and verify developer payloads across app installations is
     * recommended.
     */
    return true;
  }
}
