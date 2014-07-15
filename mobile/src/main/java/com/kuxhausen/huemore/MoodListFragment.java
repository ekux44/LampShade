package com.kuxhausen.huemore;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
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
import android.widget.TextView;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;

public class MoodListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  NavigationDrawerActivity parrentA;

  // Identifies a particular Loader being used in this component
  private static final int MOODS_LOADER = 0;
  public MoodRowAdapter dataSource;

  public TextView selected, longSelected; // updated on long click
  private int selectedPos = -1;
  private ShareActionProvider mShareActionProvider;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    parrentA = (NavigationDrawerActivity) this.getActivity();

    int layout =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
            : android.R.layout.simple_list_item_1;

    /*
     * Initializes the CursorLoader. The GROUPS_LOADER value is eventually passed to
     * onCreateLoader().
     */
    getLoaderManager().initLoader(MOODS_LOADER, null, this);

    String[] columns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID, MoodColumns.COL_MOOD_VALUE};
    dataSource =
        new MoodRowAdapter(this, this.getActivity(), layout, null, columns,
            new int[] {android.R.id.text1}, 0);

    setListAdapter(dataSource);
    // Inflate the layout for this fragment
    View myView = inflater.inflate(R.layout.moods_list_fragment, container, false);

    setHasOptionsMenu(true);
    getActivity().supportInvalidateOptionsMenu();
    return myView;
  }

  /** Returns a share intent */
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

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
      MenuItem unlocksItem = menu.findItem(R.id.action_add_mood);
      unlocksItem.setEnabled(false);
      unlocksItem.setVisible(false);
    }
    if (selectedPos > -1 && selected != null
        && !selected.getText().equals(this.getActivity().getString(R.string.cap_off))
        && !selected.getText().equals(this.getActivity().getString(R.string.cap_on))
        && !selected.getText().equals(this.getActivity().getString(R.string.cap_random))) {
      /** Getting the actionprovider associated with the menu item whose id is share */
      mShareActionProvider =
          (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));

      /** Getting the target intent */
      Intent intent = getDefaultShareIntent("" + selected.getText());

      /** Setting a share intent */
      if (intent != null)
        mShareActionProvider.setShareIntent(intent);
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
        parrentA.showEditMood(null);
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
    if (selected != null && selectedPos > -1)
      getListView().setItemChecked(selectedPos, false);
    selectedPos = -1;
    selected = null;
    if (getActivity() != null)
      getActivity().supportInvalidateOptionsMenu();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    longSelected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
    if (longSelected.getText().equals(this.getActivity().getString(R.string.cap_off))
        || longSelected.getText().equals(this.getActivity().getString(R.string.cap_on))
        || longSelected.getText().equals(this.getActivity().getString(R.string.cap_random))) {
      return;
    }
    android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
    inflater.inflate(R.menu.context_mood, menu);
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {

    switch (item.getItemId()) {

      case R.id.contextmoodmenu_delete:
        String moodSelect = MoodColumns.COL_MOOD_NAME + "=?";
        String[] moodArg = {longSelected.getText().toString()};
        getActivity().getContentResolver().delete(DatabaseDefinitions.MoodColumns.MOODS_URI,
            moodSelect, moodArg);
        return true;
      case R.id.contextmoodmenu_edit:
        parrentA.showEditMood(longSelected.getText().toString());
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
        String[] columns = {MoodColumns.COL_MOOD_NAME, BaseColumns._ID, MoodColumns.COL_MOOD_VALUE};
        return new CursorLoader(getActivity(), // Parent activity context
            DatabaseDefinitions.MoodColumns.MOODS_URI, // Table
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
    dataSource.changeCursor(cursor);
    registerForContextMenu(getListView());
    getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    // unregisterForContextMenu(getListView());
    dataSource.changeCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {

    selected = ((TextView) (v));
    selectedPos = position;

    if (dataSource.getRow(selectedPos).m.isSimple()) {
      // set the selected Item to -1 to clear any existing selection
      getListView().setItemChecked(-1, true);
    } else {
      getListView().setItemChecked(selectedPos, true);
    }

    // Notify the parent activity of selected item
    String moodName = selected.getText().toString();
    ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();

    if (service.getDeviceManager().getSelectedGroup() != null)
      service.getMoodPlayer().playMood(service.getDeviceManager().getSelectedGroup(),
          Utils.getMoodFromDatabase(moodName, getActivity()), moodName, null, null);

    getActivity().supportInvalidateOptionsMenu();
  }

}
