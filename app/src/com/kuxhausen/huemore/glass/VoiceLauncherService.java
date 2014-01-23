package com.kuxhausen.huemore.glass;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;
import alt.android.os.CountDownTimer;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class VoiceLauncherService extends NetworkManagedSherlockFragmentActivity {

	CountDownTimer pageTimer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.glass_card_main);
		
		
		initializationDatabaseChecks();
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();

        pageTimer = new CountDownTimer(4000,100) {
			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				Log.e("voice","finished called");
				if(VoiceLauncherService.this.isFinishing()!=true)
					VoiceLauncherService.this.finish();
			}
		};
		pageTimer.start();
	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
              if(pageTimer!=null)
            	  pageTimer.cancel();
        	  
        	  openOptionsMenu();
              return true;
          } else if (keyCode == KeyEvent.KEYCODE_BACK){
        	  this.finish();
          }
          
          return false;
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.glass_card_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection. Menu items typically start another
        // activity, start a service, or broadcast another intent.
        switch (item.getItemId()) {
            case R.id.glass_reconnect_menu_item:
            	DiscoverHubDialogFragment dhdf = new DiscoverHubDialogFragment();
				dhdf.show(this.getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	
	
	
	
	
	/** tweaked from MainActivity //TODO restructure so no duplication **/
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
			DiscoverHubDialogFragment wdf = new DiscoverHubDialogFragment();
			wdf.show(this.getSupportFragmentManager(),
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
