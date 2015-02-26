package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;

public class MoodListFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  NavigationDrawerActivity mParent;

  // Identifies a particular Loader being used in this component
  private static final int MOODS_LOADER = 0;
  private MoodRowAdapter mDataSource;

  private View mSelected, mLongSelected; // updated on long click
  private int mSelectedPos = -1;
  private ShareActionProvider mShareActionProvider;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    mParent = (NavigationDrawerActivity) this.getActivity();

    getLoaderManager().initLoader(MOODS_LOADER, null, this);

    String[] columns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID, MoodColumns.COL_MOOD_VALUE};
    mDataSource =
        new MoodRowAdapter(this, this.getActivity(), R.layout.mood_row, null, columns,
                           new int[]{android.R.id.text1}, 0);

    setListAdapter(mDataSource);
    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.moods_list_fragment, container, false);

    setHasOptionsMenu(true);
    getActivity().supportInvalidateOptionsMenu();
    return myView;
  }

  /**
   * Returns a share intent
   */
  private Intent getDefaultShareIntent(String mood) {
    String encodedMood = HueUrlEncoder.encode(Utils.getMoodFromDatabase(mood, this.getActivity()));

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    // intent.putExtra(Intent.EXTRA_SUBJECT, "SUBJECT");
    intent.putExtra(Intent.EXTRA_TEXT, mood + " #LampShadeIO http://lampshade.io/share?"
                                       + encodedMood);
    return intent;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.action_mood, menu);

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
        >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
      MenuItem unlocksItem = menu.findItem(R.id.action_add_mood);
      unlocksItem.setEnabled(false);
      unlocksItem.setVisible(false);
    }
    if (mSelectedPos > -1 && mSelected != null) {
      /** Getting the actionprovider associated with the menu item whose id is share */
      mShareActionProvider =
          (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));

      /** Getting the target intent */
      Intent intent = getDefaultShareIntent("" + mDataSource.getTextFromRowView(mSelected));

      /** Setting a share intent */
      if (intent != null) {
        mShareActionProvider.setShareIntent(intent);
      }
    } else {
      MenuItem shareItem = menu.findItem(R.id.action_share);
      shareItem.setEnabled(false);
      shareItem.setVisible(false);
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_add_mood:
        mParent.showEditMood(null);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
  }

  @Override
  public void onResume() {
    super.onResume();
    this.invalidateSelection();
  }

  public void invalidateSelection() {
    // Set the previous selected item as checked to be unhighlighted when in
    // two-pane layout
    if (mSelected != null && mSelectedPos > -1) {
      getListView().setItemChecked(mSelectedPos, false);
    }
    mSelectedPos = -1;
    mSelected = null;
    if (getActivity() != null) {
      getActivity().supportInvalidateOptionsMenu();
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    mLongSelected = ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;

    android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
    inflater.inflate(R.menu.context_mood, menu);

    if (mDataSource.getRowFromView(mLongSelected).isStared()) {
      menu.findItem(R.id.contextmoodmenu_star).setVisible(false);
      menu.findItem(R.id.contextmoodmenu_unstar).setVisible(true);
    } else {
      menu.findItem(R.id.contextmoodmenu_star).setVisible(true);
      menu.findItem(R.id.contextmoodmenu_unstar).setVisible(false);
    }
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {

    switch (item.getItemId()) {
      case R.id.contextmoodmenu_star:
        mDataSource.getRowFromView(mLongSelected).starChanged(this.getActivity(), true);
        getLoaderManager().restartLoader(MOODS_LOADER, null, this);
        return true;
      case R.id.contextmoodmenu_unstar:
        mDataSource.getRowFromView(mLongSelected).starChanged(this.getActivity(), false);
        getLoaderManager().restartLoader(MOODS_LOADER, null, this);
        return true;
      case R.id.contextmoodmenu_delete:
        String moodSelect = MoodColumns.COL_MOOD_NAME + "=?";
        String[] moodArg = {mDataSource.getTextFromRowView(mLongSelected)};
        getActivity().getContentResolver().delete(Definitions.MoodColumns.MOODS_URI,
                                                  moodSelect, moodArg);
        return true;
      case R.id.contextmoodmenu_edit:
        mParent.showEditMood(mDataSource.getTextFromRowView(mLongSelected));
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  /**
   * Callback that's invoked when the system has initialized the Loader and is ready to start the
   * query. This usually happens when initLoader() is called. The loaderID argument contains the ID
   * value passed to the initLoader() call.
   */
  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    /*
     * Takes action based on the ID of the Loader that's being created
     */
    switch (loaderID) {
      case MOODS_LOADER:
        // Returns a new CursorLoader
        String[]
            columns =
            {MoodColumns.COL_MOOD_NAME, BaseColumns._ID, MoodColumns.COL_MOOD_VALUE,
             MoodColumns.COL_MOOD_LOWERCASE_NAME, MoodColumns.COL_MOOD_PRIORITY};
        return new CursorLoader(getActivity(), // Parent activity context
                                Definitions.MoodColumns.MOODS_URI, // Table
                                columns, // Projection to return
                                null, // No selection clause
                                null, // No selection arguments
                                null // Default sort order
        );
      default:
        // An invalid id was passed in
        return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
    /*
     * Moves the query results into the adapter, causing the ListView fronting this adapter to
     * re-display
     */
    mDataSource.changeCursor(cursor);
    registerForContextMenu(getListView());
    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    // unregisterForContextMenu(getListView());
    mDataSource.changeCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    mSelected = v;
    mSelectedPos = position;

    getListView().setItemChecked(mSelectedPos, true);

    // Notify the parent activity of selected item
    String moodName = mDataSource.getTextFromRowView(mSelected);
    ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();

    if (service.getDeviceManager().getSelectedGroup() != null) {
      service.getMoodPlayer().playMood(service.getDeviceManager().getSelectedGroup(),
                                       Utils.getMoodFromDatabase(moodName, getActivity()), moodName,
                                       null);
    }

    getActivity().supportInvalidateOptionsMenu();
  }
}
