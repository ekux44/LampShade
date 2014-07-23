package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
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

import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.state.Group;

public class GroupListFragment extends ListFragment implements
                                                    LoaderManager.LoaderCallbacks<Cursor> {

  // Identifies a particular Loader being used in this component
  private static final int GROUPS_LOADER = 0;
  public CursorAdapter dataSource;
  public TextView selected, longSelected; // updated on long click
  public int selectedPos = -1;
  private NetworkManagedActivity gbpfCallback;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    // create ContextThemeWrapper from the original Activity Context with the custom theme
    final Context contextThemeWrapper =
        new ContextThemeWrapper(this.getActivity(), R.style.GreenWidgets);
    // clone the inflater using the ContextThemeWrapper
    LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

    // We need to use a different list item layout for devices older than Honeycomb
    int layout =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        ? android.R.layout.simple_list_item_activated_1
        : android.R.layout.simple_list_item_1;

    // Inflate the layout for this fragment
    View myView = localInflater.inflate(R.layout.groups_list_fragment, null);

    /*
     * Initializes the CursorLoader. The GROUPS_LOADER value is eventually passed to
     * onCreateLoader().
     */
    getLoaderManager().initLoader(GROUPS_LOADER, null, this);

    String[] columns = {GroupColumns.GROUP, BaseColumns._ID};

    dataSource =
        new SimpleCursorAdapter(contextThemeWrapper, layout, null, columns,
                                new int[]{android.R.id.text1}, 0);

    setListAdapter(dataSource);

    setHasOptionsMenu(true);
    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();
    this.setHasOptionsMenu(true);
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
    gbpfCallback = (NetworkManagedActivity) activity;
  }

  public void invalidateSelection() {
    // Set the previous selected item as checked to be unhighlighted when in
    // two-pane layout
    if (selected != null && selectedPos > -1) {
      getListView().setItemChecked(selectedPos, false);
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    longSelected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;
    if (longSelected.getText().toString().equals(this.getActivity().getString(R.string.cap_all))) {
      return;
    }
    android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
    inflater.inflate(R.menu.context_group, menu);
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {

    if (longSelected == null) {
      return false;
    }

    switch (item.getItemId()) {

      case R.id.contextgroupmenu_delete: // <-- your custom menu item id here
        String groupSelect = GroupColumns.GROUP + "=?";
        String[] groupArg = {longSelected.getText().toString()};
        getActivity().getContentResolver().delete(Definitions.GroupColumns.GROUPBULBS_URI,
                                                  groupSelect, groupArg);
        return true;
      case R.id.contextgroupmenu_edit: // <-- your custom menu item id here
        EditGroupDialogFragment ngdf = new EditGroupDialogFragment();
        Bundle args = new Bundle();
        args.putString(InternalArguments.GROUP_NAME, longSelected.getText().toString());
        ngdf.setArguments(args);
        ngdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    selected = ((TextView) (v));
    selectedPos = position;

    // Notify the parent activity of selected bulbs
    gbpfCallback.setGroup(Group.loadFromDatabase(selected.getText().toString(), this.gbpfCallback));

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
        String[] columns = {GroupColumns.GROUP, BaseColumns._ID};
        return new CursorLoader(getActivity(), // Parent activity context
                                Definitions.GroupColumns.GROUPS_URI, // Table
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
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    // unregisterForContextMenu(getListView());
    dataSource.changeCursor(null);
  }
}
