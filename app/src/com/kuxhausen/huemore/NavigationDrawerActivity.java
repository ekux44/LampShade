package com.kuxhausen.huemore;

import com.kuxhausen.huemore.billing.BillingManager;
import com.kuxhausen.huemore.billing.UnlocksDialogFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.PreferenceInitializer;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.timing.AlarmsListFragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavigationDrawerActivity extends NetworkManagedSherlockFragmentActivity implements OnBackStackChangedListener{

	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;
    //private Fragment[] mFragments = new Fragment[mDrawerTitles.length];
    
    public int mSelectedItemPosition;
    public final static int BULB_FRAG = 0, GROUP_FRAG=1, CONNECTIONS_FRAG = 2, SETTINGS_FRAG = 3, HELP_FRAG = 4, ALARM_FRAG = 5, NFC_FRAG = 6;
    public final static String BASE_FRAG_TAG = "FragTag";
    
    private BillingManager mBillingManager;
	
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        mBillingManager = new BillingManager(this);
        

        
		if(Utils.hasProVersion(this)){
			mDrawerTitles = this.getResources().getStringArray(R.array.navigation_drawer_pro_titles);
		}
		else{
			mDrawerTitles = this.getResources().getStringArray(R.array.navigation_drawer_titles);
		}
		
        mTitle = mDrawerTitle = getTitle();
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        
        
        PreferenceInitializer.initializedPreferencesAndShowDialogs(this);
        
        Bundle b = this.getIntent().getExtras();
		if (b != null && b.containsKey(InternalArguments.PROMPT_UPGRADE)
				&& b.getBoolean(InternalArguments.PROMPT_UPGRADE)) {
			UnlocksDialogFragment unlocks = new UnlocksDialogFragment();
			unlocks.show(getSupportFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	//TODO find better hack
    	if(position == GROUP_FRAG)
    		position = BULB_FRAG;
    	
    	mSelectedItemPosition = position;
    	this.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    	
    	// update the main content by replacing fragments
    	
    	String selectedFragTag = BASE_FRAG_TAG+position;
    	
    	Fragment selectedFrag = getSupportFragmentManager().findFragmentByTag(selectedFragTag);
    	
    	if(selectedFrag==null){
	    	switch(position){
	    		case BULB_FRAG:
	    			selectedFrag = new MainActivity();
	    			break;
	    		case GROUP_FRAG:
	    			selectedFrag = new MainActivity();
	    			break;
	    		case SETTINGS_FRAG:
	    			selectedFrag = new SettingsActivity();
	    			break;
	    		case HELP_FRAG:
	    			selectedFrag = new HelpActivity();
	    			break;
	    		case ALARM_FRAG:
	    			selectedFrag = new AlarmsListFragment();
	    	}
    	}
    	
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, selectedFrag,selectedFragTag).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggals
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    

	@Override
	public void onBackStackChanged() {
		if(getSupportFragmentManager().getBackStackEntryCount()>0){
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}else{
			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
		
	}
    
    @Override
	public void setGroup(Group g){
    	super.setGroup(g);
    	
    	MainActivity frag = null;
    	if(mSelectedItemPosition == BULB_FRAG){
    		frag = ((MainActivity)getSupportFragmentManager().findFragmentByTag(BASE_FRAG_TAG+BULB_FRAG));
    	} else if(mSelectedItemPosition == GROUP_FRAG){
    		frag = ((MainActivity)getSupportFragmentManager().findFragmentByTag(BASE_FRAG_TAG+GROUP_FRAG));
    	}
    	if(frag!=null){
			if ((getResources().getConfiguration().screenLayout &
					 Configuration.SCREENLAYOUT_SIZE_MASK) >=
					 Configuration.SCREENLAYOUT_SIZE_LARGE){
				frag.invalidateSelection();
			 }else if(boundToService()){
				 SecondActivity drillDownFrag = new SecondActivity();
				 
				 FragmentManager fragmentManager = getSupportFragmentManager();
			     fragmentManager.beginTransaction().addToBackStack("prevoius").replace(R.id.content_frame, drillDownFrag).commit();

			 }
		}
    }
    
    @Override
	public void onConnectionStatusChanged(){
		this.supportInvalidateOptionsMenu();
	}
    
    
    @Override
	public void onDestroy() {
		if(mBillingManager!=null)
			mBillingManager.onDestroy();
		
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mBillingManager!=null)
			mBillingManager.onActivityResult(requestCode,resultCode,data);
	}
	
	public BillingManager getBillingManager(){
		return mBillingManager;
	}
}
