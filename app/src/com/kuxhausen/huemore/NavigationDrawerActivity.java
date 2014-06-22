package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.kuxhausen.huemore.billing.BillingManager;
import com.kuxhausen.huemore.billing.UnlocksDialogFragment;
import com.kuxhausen.huemore.editmood.EditMoodFragment;
import com.kuxhausen.huemore.net.ConnectionListFragment;
import com.kuxhausen.huemore.nfc.NfcWriterFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.PreferenceInitializer;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Group;
import com.kuxhausen.huemore.timing.AlarmsListFragment;

public class NavigationDrawerActivity extends NetworkManagedActivity implements
    OnBackStackChangedListener {

  private DrawerLayout mDrawerLayout;
  private View mDrawerView;
  private ListView mDrawerList, mNotificationList;
  private ActionBarDrawerToggle mDrawerToggle;
  private NotificationRowAdapter mNotificationAdapter;

  private CharSequence mDrawerTitle;
  private CharSequence mTitle;
  private String[] mDrawerTitles;
  // private Fragment[] mFragments = new Fragment[mDrawerTitles.length];

  public int mSelectedItemPosition;
  public final static int BULB_FRAG = 0, GROUP_FRAG = 1, CONNECTIONS_FRAG = 2, SETTINGS_FRAG = 3,
      HELP_FRAG = 4, ALARM_FRAG = 5, NFC_FRAG = 6;
  public final static String BASE_FRAG_TAG = "FragTag";

  private BillingManager mBillingManager;

  private SharedPreferences mSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_navigation_drawer);

    mBillingManager = new BillingManager(this);

    if (Utils.hasProVersion(this)) {
      mDrawerTitles = this.getResources().getStringArray(R.array.navigation_drawer_pro_titles);
    } else {
      mDrawerTitles = this.getResources().getStringArray(R.array.navigation_drawer_titles);
    }

    mTitle = mDrawerTitle = getTitle();

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.drawer_list);

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener
    mDrawerList
        .setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerTitles));
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    mDrawerView = findViewById(R.id.left_drawer);

    mNotificationList = (ListView) findViewById(R.id.notification_list);


    // enable ActionBar app icon to behave as action to toggle nav drawer
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the sliding drawer and the action bar app icon
    mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
    mDrawerLayout, /* DrawerLayout object */
    R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
    R.string.drawer_open, /* "open drawer" description for accessibility */
    R.string.drawer_close /* "close drawer" description for accessibility */
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

    Bundle b = getIntent().getExtras();
    mSettings = PreferenceManager.getDefaultSharedPreferences(this);

    getSupportFragmentManager().addOnBackStackChangedListener(this);


    PreferenceInitializer.initializedPreferencesAndShowDialogs(this);


    if (b != null && b.containsKey(InternalArguments.PROMPT_UPGRADE)
        && b.getBoolean(InternalArguments.PROMPT_UPGRADE)) {
      UnlocksDialogFragment unlocks = new UnlocksDialogFragment();
      unlocks.show(getSupportFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
    }
  }

  public void onResume() {
    super.onResume();
    Bundle b = getIntent().getExtras();
    if (b != null && b.containsKey(InternalArguments.NAV_DRAWER_PAGE)) {
      selectItem(b.getInt(InternalArguments.NAV_DRAWER_PAGE), b);
      b.remove(InternalArguments.NAV_DRAWER_PAGE);
    } else {
      if (mSettings.getBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false)) {
        selectItem(GROUP_FRAG, b);
      } else {
        selectItem(BULB_FRAG, b);
      }
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
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerView);
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
    switch (item.getItemId()) {
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /* The click listner for ListView in the navigation drawer */
  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      selectItem(position, null);
    }
  }

  private void selectItem(int position, Bundle b) {
    if (b == null)
      b = new Bundle();

    // record the groupbulb tab state and pass in bundle to main
    if (position == GROUP_FRAG) {
      b.putInt(InternalArguments.GROUPBULB_TAB, GroupBulbPagerAdapter.GROUP_LOCATION);
      saveTab(GroupBulbPagerAdapter.GROUP_LOCATION);
    } else if (position == BULB_FRAG) {
      b.putInt(InternalArguments.GROUPBULB_TAB, GroupBulbPagerAdapter.BULB_LOCATION);
      saveTab(GroupBulbPagerAdapter.BULB_LOCATION);
    }

    // this allows Bulb & Group to show up twice in NavBar but share fragment
    int actualPosition = position;
    if (actualPosition == GROUP_FRAG)
      actualPosition = BULB_FRAG;

    mSelectedItemPosition = position;
    cleanUpActionBar();

    String selectedFragTag = BASE_FRAG_TAG + actualPosition;

    Fragment selectedFrag = getSupportFragmentManager().findFragmentByTag(selectedFragTag);

    if (selectedFrag == null) {
      switch (actualPosition) {
        case BULB_FRAG:
          selectedFrag = new MainFragment();
          break;
        case CONNECTIONS_FRAG:
          selectedFrag = new ConnectionListFragment();
          break;
        case SETTINGS_FRAG:
          selectedFrag = new SettingsFragment();
          break;
        case HELP_FRAG:
          selectedFrag = new HelpFragment();
          break;
        case ALARM_FRAG:
          selectedFrag = new AlarmsListFragment();
          break;
        case NFC_FRAG:
          selectedFrag = new NfcWriterFragment();
          break;
      }
      selectedFrag.setArguments(b);
    } else {
      // if can't pass groupbulb tab data in bundle because frag already exists, pass directly
      if (actualPosition == BULB_FRAG) {
        ((MainFragment) selectedFrag).setTab(b.getInt(InternalArguments.GROUPBULB_TAB));
      }
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    fragmentManager.beginTransaction().replace(R.id.content_frame, selectedFrag, selectedFragTag)
        .commit();

    // update selected item and title, then close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(mDrawerTitles[position]);
    mDrawerLayout.closeDrawer(mDrawerView);
  }

  @Override
  public void onServiceConnected() {
    mNotificationAdapter = new NotificationRowAdapter(this, getService().getMoodPlayer());
    mNotificationList.setAdapter(mNotificationAdapter);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getSupportActionBar().setTitle(mTitle);
  }

  /**
   * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and
   * onConfigurationChanged()...
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

  private void cleanUpActionBar() {
    getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
  }

  @Override
  public void onBackStackChanged() {
    cleanUpActionBar();
    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      mDrawerToggle.setDrawerIndicatorEnabled(false);
      mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    } else {
      mDrawerToggle.setDrawerIndicatorEnabled(true);
      mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
  }

  public void showHelp(String pageName) {
    Bundle b = new Bundle();
    b.putString(InternalArguments.HELP_PAGE, pageName);
    selectItem(HELP_FRAG, b);

    // TODO find a way of showing help page without clearning back stack yet still enabling nav
    // drawer
    /*
     * HelpActivity frag = new HelpActivity(); if(pageName!=null){ Bundle b = new Bundle();
     * b.putString(InternalArguments.HELP_PAGE, pageName); frag.setArguments(b); } FragmentManager
     * fragmentManager = getSupportFragmentManager();
     * fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame,
     * frag).commit();
     */
  }

  public void showEditMood(String moodName) {
    EditMoodFragment frag = new EditMoodFragment();
    if (moodName != null) {
      Bundle b = new Bundle();
      b.putString(InternalArguments.MOOD_NAME, moodName);
      frag.setArguments(b);
    }
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().addToBackStack("mood").replace(R.id.content_frame, frag)
        .commit();

  }

  @Override
  public void setGroup(Group g) {
    super.setGroup(g);

    MainFragment frag = null;
    if (mSelectedItemPosition == BULB_FRAG || mSelectedItemPosition == GROUP_FRAG) {
      frag =
          ((MainFragment) getSupportFragmentManager().findFragmentByTag(BASE_FRAG_TAG + BULB_FRAG));
    }
    if (frag != null) {
      if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
        frag.invalidateSelection();
      } else if (boundToService()) {
        SecondaryFragment drillDownFrag = new SecondaryFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addToBackStack("group")
            .replace(R.id.content_frame, drillDownFrag).commit();

      }
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
      NfcWriterFragment frag =
          ((NfcWriterFragment) getSupportFragmentManager().findFragmentByTag(
              BASE_FRAG_TAG + NFC_FRAG));
      if (frag != null) {
        frag.setMyTag(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
      }
      Toast.makeText(this, this.getString(R.string.nfc_tag_detected), Toast.LENGTH_SHORT).show();
    }
  }

  public void markSelected(int pagerPosition) {
    int drawerPosition;
    if (pagerPosition == GroupBulbPagerAdapter.BULB_LOCATION) {
      drawerPosition = NavigationDrawerActivity.BULB_FRAG;
    } else {
      drawerPosition = NavigationDrawerActivity.GROUP_FRAG;
    }
    mDrawerList.setItemChecked(drawerPosition, true);
    setTitle(mDrawerTitles[drawerPosition]);
    saveTab(pagerPosition);
  }

  public void saveTab(int position) {
    Editor edit = mSettings.edit();
    switch (position) {
      case GroupBulbPagerAdapter.BULB_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, false);
        break;
      case GroupBulbPagerAdapter.GROUP_LOCATION:
        edit.putBoolean(PreferenceKeys.DEFAULT_TO_GROUPS, true);
        break;
    }
    edit.commit();

  }

  @Override
  public void onConnectionStatusChanged() {
    this.supportInvalidateOptionsMenu();
  }


  @Override
  public void onDestroy() {
    mNotificationAdapter.onDestroy();
    if (mBillingManager != null)
      mBillingManager.onDestroy();

    super.onDestroy();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (mBillingManager != null)
      mBillingManager.onActivityResult(requestCode, resultCode, data);
  }

  public BillingManager getBillingManager() {
    return mBillingManager;
  }
}
