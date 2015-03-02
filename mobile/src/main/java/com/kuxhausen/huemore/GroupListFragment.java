package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.state.DatabaseGroup;

public class GroupListFragment extends ListFragment implements
                                                    LoaderManager.LoaderCallbacks<Cursor> {

  private static final int GROUPS_LOADER = 0;
  public DatabaseGroupsAdapter mDataSource;
  private int mSelectedPos, mLongSelectedPos = -1; // updated on click, long click
  private NetworkManagedActivity mParent;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    getLoaderManager().initLoader(GROUPS_LOADER, null, this);
    mDataSource =
        new DatabaseGroupsAdapter(getActivity(), R.layout.mood_row, null, 0);
    setListAdapter(mDataSource);

    View myView = inflater.inflate(R.layout.groups_list_fragment, null);

    setHasOptionsMenu(true);

    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    this.setHasOptionsMenu(true);
    this.invalidateSelection();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.action_group, menu);

    if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
        >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
      MenuItem unlocksItem = menu.findItem(R.id.action_add_group);
      unlocksItem.setEnabled(false);
      unlocksItem.setVisible(false);

    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_add_group:
        EditGroupDialogFragment ngdf = new EditGroupDialogFragment();
        ngdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
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
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mParent = (NetworkManagedActivity) activity;
  }

  public void invalidateSelection() {
    // Set the previous selected item as checked to be unhighlighted when in
    // two-pane layout
    if (mSelectedPos > -1) {
      getListView().setItemChecked(mSelectedPos, false);
      mSelectedPos = -1;
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    mLongSelectedPos = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;

    DatabaseGroup longSelected = mDataSource.getRow(mLongSelectedPos);

    android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
    inflater.inflate(R.menu.context_group, menu);

    if (longSelected.isStared()) {
      menu.findItem(R.id.contextgroupmenu_star).setVisible(false);
      menu.findItem(R.id.contextgroupmenu_unstar).setVisible(true);
    } else {
      menu.findItem(R.id.contextgroupmenu_star).setVisible(true);
      menu.findItem(R.id.contextgroupmenu_unstar).setVisible(false);
    }

    if (longSelected.isALL()) {
      menu.findItem(R.id.contextgroupmenu_edit).setVisible(false);
      menu.findItem(R.id.contextgroupmenu_delete).setVisible(false);
    }
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    DatabaseGroup longSelected = mDataSource.getRow(mLongSelectedPos);

    switch (item.getItemId()) {
      case R.id.contextgroupmenu_star:
        longSelected.starChanged(this.getActivity(), true);
        getLoaderManager().restartLoader(GROUPS_LOADER, null, this);
        break;
      case R.id.contextgroupmenu_unstar:
        longSelected.starChanged(this.getActivity(), false);
        getLoaderManager().restartLoader(GROUPS_LOADER, null, this);
        break;
      case R.id.contextgroupmenu_delete:
        longSelected.deleteSelf(mParent);
        break;
      case R.id.contextgroupmenu_edit: // <-- your custom menu item id here
        EditGroupDialogFragment ngdf = new EditGroupDialogFragment();
        Bundle args = new Bundle();
        args.putLong(InternalArguments.GROUP_ID, longSelected.getId());
        ngdf.setArguments(args);
        ngdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        break;
      default:
        return super.onContextItemSelected(item);
    }

    return true;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    mSelectedPos = position;

    // Notify the parent activity of selected bulbs
    mParent.setGroup(mDataSource.getRow(mSelectedPos));
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
      case GROUPS_LOADER:
        // Returns a new CursorLoader
        return new CursorLoader(getActivity(), // Parent activity context
                                GroupColumns.URI, // Table
                                DatabaseGroup.GROUP_QUERY_COLUMNS, // Projection to return
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
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    // unregisterForContextMenu(getListView());
    mDataSource.changeCursor(null);
  }
}
