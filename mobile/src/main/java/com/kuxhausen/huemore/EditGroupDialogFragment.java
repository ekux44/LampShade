package com.kuxhausen.huemore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Definitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.PreferenceKeys;
import com.kuxhausen.huemore.state.DatabaseGroup;

import java.util.ArrayList;

public class EditGroupDialogFragment extends DialogFragment implements
                                                            LoaderManager.LoaderCallbacks<Cursor> {

  private static final int NET_BULBS_LOADER = 0;
  private static final String[] net_columns = {NetBulbColumns.NAME_COLUMN,
                                               NetBulbColumns.DEVICE_ID_COLUMN, NetBulbColumns._ID};

  public CursorAdapter dataSource;
  private ListView mBulbsListView;
  private EditText mNameEditText;
  private DatabaseGroup initialGroup;
  private NetworkManagedActivity mParent;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception.
    try {
      mParent = (NetworkManagedActivity) activity;
    } catch (ClassCastException e) {
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View groupDialogView = inflater.inflate(R.layout.edit_group_dialog, null);
    mBulbsListView = ((ListView) groupDialogView.findViewById(R.id.listView1));
    mBulbsListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

    getLoaderManager().initLoader(NET_BULBS_LOADER, null, this);

    dataSource =
        new SimpleCursorAdapter(this.getActivity(),
                                android.R.layout.simple_list_item_multiple_choice, null,
                                net_columns, new int[]{android.R.id.text1}, 0);

    mBulbsListView.setAdapter(dataSource);

    builder.setView(groupDialogView);

    mNameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.GROUP_ID)) {
      initialGroup = DatabaseGroup.load(args.getLong(InternalArguments.GROUP_ID), mParent);
      mNameEditText.setText(initialGroup.getName());
    }

    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {

        String groupName = mNameEditText.getText().toString();
        if (groupName == null || groupName.length() < 1) {
          SharedPreferences settings =
              PreferenceManager.getDefaultSharedPreferences(mParent);
          int unnamedNumber = 1 + settings.getInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, 0);
          Editor edit = settings.edit();
          edit.putInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, unnamedNumber);
          edit.commit();
          groupName =
              mParent.getResources().getString(R.string.unnamed_group) + " "
              + unnamedNumber;
        }

        if(initialGroup==null){
          initialGroup = DatabaseGroup.createGroup(groupName, mParent);
        }

        initialGroup.setName(groupName, mParent);

        ArrayList<Long> bulbIds = new ArrayList<Long>();
        SparseBooleanArray set = mBulbsListView.getCheckedItemPositions();
        Cursor cursor = dataSource.getCursor();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
          if (set.get(i)) {
            bulbIds.add(cursor.getLong(2));
          }
          cursor.moveToNext();
        }
        cursor.close();

        initialGroup.setNetBulbDatabaseIds(bulbIds, mParent);
      }
    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });
    // Create the AlertDialog object and return it
    return builder.create();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    /*
     * Takes action based on the ID of the Loader that's being created
     */
    switch (loaderID) {
      case NET_BULBS_LOADER:
        // Returns a new CursorLoader
        return new CursorLoader(getActivity(), // Parent activity context
                                NetBulbColumns.URI, // Table
                                net_columns, // Projection to return
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

    if (initialGroup != null) {

      cursor.moveToFirst();
      for (int i = 0; i < cursor.getCount(); i++) {

        if (initialGroup.getNetworkBulbDatabaseIds().contains(cursor.getLong(2))) {
          mBulbsListView.setItemChecked(i, true);
        } else {
          mBulbsListView.setItemChecked(i, false);
        }

        cursor.moveToNext();
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    /*
     * Clears out the adapter's reference to the Cursor. This prevents memory leaks.
     */
    dataSource.changeCursor(null);
  }
}
