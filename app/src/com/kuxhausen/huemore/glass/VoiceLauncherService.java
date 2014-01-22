package com.kuxhausen.huemore.glass;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.kuxhausen.huemore.CommunityDialogFragment;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.registration.DiscoverHubDialogFragment;
import com.kuxhausen.huemore.registration.WelcomeDialogFragment;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.state.Mood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class VoiceLauncherService extends NetworkManagedSherlockFragmentActivity {

	
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
		ArrayList<String> voiceResults = getIntent().getExtras()
	            .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
	    // ...
	    
	    Pattern p = Pattern.compile("[0-9]+");
	    
	    GroupMoodBrightness gmb = new GroupMoodBrightness();
	    gmb.group = "ALL";
	    
	    if(voiceResults!=null){
		    for(String s : voiceResults)
		    	if(s!=null){
		    		Log.d("voice",s);
		    		Matcher m = p.matcher(s);
		    	    if (m.find()) {
		    	    	try{
                            int percent = Integer.parseInt(m.group());
                            if(percent>=0 && percent <=100)
                                    gmb.brightness = ((percent*255)/100);
                            		gmb.mood = "ON";
    		    	    			Log.d("voice", "parsed: "+gmb.brightness);
                            }
		    	    	catch (Error e){
                        }
		    	    } else if(s.contains("off")){
		    	    	gmb.mood = "OFF";
		    	    }
		    	}
	    }else{
	    	Log.d("voice","no voice results");
	    }
	    
	    
	    if(gmb.mood== null)
	    	return;
	    
	    
	    /** Cut down verison of Alarm Reciver code **/
        // TODO merge

        // Look up bulbs for that mood from database
        String[] groupColumns = { GroupColumns.BULB };
        String[] gWhereClause = { gmb.group };
        Cursor groupCursor = this.getContentResolver()
                        .query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
                                        groupColumns, GroupColumns.GROUP + "=?",
                                        gWhereClause, null);

        ArrayList<Integer> groupStates = new ArrayList<Integer>();
        while (groupCursor.moveToNext()) {
                groupStates.add(groupCursor.getInt(0));
        }
        Integer[] bulbS = groupStates.toArray(new Integer[groupStates
                        .size()]);

        Mood m = Utils.getMoodFromDatabase(gmb.mood, this);
        
        Intent trasmitter = new Intent(this, MoodExecuterService.class);
        trasmitter.putExtra(InternalArguments.ENCODED_MOOD, HueUrlEncoder.encode(m,bulbS,gmb.brightness));
        trasmitter.putExtra(InternalArguments.MOOD_NAME, gmb.mood);
        trasmitter.putExtra(InternalArguments.GROUP_NAME, gmb.group);
        startService(trasmitter); 
	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
              openOptionsMenu();
              return true;
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
	
	
	
	
	
	
	/** borrowed from MainActivity //TODO restructure so no duplication **/
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
