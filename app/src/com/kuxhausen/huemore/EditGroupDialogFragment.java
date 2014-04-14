package com.kuxhausen.huemore;

import java.util.ArrayList;
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
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;

public class EditGroupDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int BULBS_LOADER = 0;
	private static final String[] columns = { NetBulbColumns.NAME_COLUMN, NetBulbColumns.DEVICE_ID_COLUMN, BaseColumns._ID };

	public CursorAdapter dataSource;

	ListView bulbsListView;
	EditText nameEditText;
	
	Boolean[] preChecked;
	String initialName;

	private NetworkManagedSherlockFragmentActivity parrentActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			parrentActivity = (NetworkManagedSherlockFragmentActivity) activity;
		} catch (ClassCastException e) {
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_group_dialog,
				null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		bulbsListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		
		getLoaderManager().initLoader(BULBS_LOADER, null, this);

		dataSource = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_list_item_multiple_choice, null,
				columns, new int[] { android.R.id.text1 }, 0);

		bulbsListView.setAdapter(dataSource);

		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);

		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.GROUP_NAME)) {
			String groupName = args.getString(InternalArguments.GROUP_NAME);

			// Look up bulbs for that mood from database
			String[] groupColumns = { GroupColumns.BULB };
			String[] gWhereClause = { groupName };
			Cursor groupCursor = this.getActivity().getContentResolver()
					.query(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use
																			// the
																			// default
																			// content
																			// URI
																			// for
																			// the
																			// provider.
							groupColumns, // Return the note ID and title for
											// each note.
							GroupColumns.GROUP + "=?", // selection clause
							gWhereClause, // selection clause args
							null // Use the default sort order.
					);

			ArrayList<Integer> groupStates = new ArrayList<Integer>();
			while (groupCursor.moveToNext()) {
				groupStates.add(groupCursor.getInt(0));
			}
			Integer[] bulbS = groupStates.toArray(new Integer[groupStates
					.size()]);
			preChecked = new Boolean[50];
			for (int checkedSpot : bulbS) {
				preChecked[checkedSpot - 1] = true;// have to account by the off
													// by one in bulb Numbers
			}
			nameEditText.setText(groupName);
			initialName = groupName;
		}

		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {

						// if there was a previous mood we're editing, remove it
						if (initialName != null) {
							String groupSelect = GroupColumns.GROUP + "=?";
							String[] groupArg = { initialName };
							getActivity()
									.getContentResolver()
									.delete(DatabaseDefinitions.GroupColumns.GROUPBULBS_URI,
											groupSelect, groupArg);
						}

						ArrayList<Integer> checkedBulbs = new ArrayList<Integer>();
						SparseBooleanArray set = bulbsListView.getCheckedItemPositions();
						for (int i = 0; i < dataSource.getCount(); i++) {
							if (set.get(i)) {
								checkedBulbs.add(1+i);
							}
						}

						String groupName = nameEditText.getText().toString();
						if(groupName==null || groupName.length()<1){
							SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(parrentActivity);
							int unnamedNumber = 1+settings.getInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, 0);
							Editor edit = settings.edit();
							edit.putInt(PreferenceKeys.UNNAMED_GROUP_NUMBER, unnamedNumber);
							edit.commit();
							groupName = parrentActivity.getResources().getString(R.string.unnamed_group)+" "+unnamedNumber;
						}
						
						for (int i = 0; i < checkedBulbs.size(); i++) {
							
							// Defines an object to contain the new values to
							// insert
							ContentValues mNewValues = new ContentValues();

							/*
							 * Sets the values of each column and inserts the
							 * word. The arguments to the "put" method are
							 * "column name" and "value"
							 */
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.GROUP,
									groupName);
							mNewValues.put(
									DatabaseDefinitions.GroupColumns.BULB,
									checkedBulbs.get(i));
							mNewValues
									.put(DatabaseDefinitions.GroupColumns.PRECEDENCE,
											i);

							getActivity().getContentResolver()
									.insert(DatabaseDefinitions.GroupColumns.GROUPS_URI,
											mNewValues // the values to insert
									);

						}

					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
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
		case BULBS_LOADER:
			// Returns a new CursorLoader
			return new CursorLoader(getActivity(), // Parent activity context
					DatabaseDefinitions.NetBulbColumns.URI, // Table
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
		 * Moves the query results into the adapter, causing the ListView
		 * fronting this adapter to re-display
		 */
		dataSource.changeCursor(cursor);
		
		for (int i = 0; i < cursor.getCount(); i++) {
			if (preChecked != null && preChecked[i] != null && preChecked[i] == true)
				bulbsListView.setItemChecked(i, true);

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		/*
		 * Clears out the adapter's reference to the Cursor. This prevents
		 * memory leaks.
		 */
		dataSource.changeCursor(null);
	}
}
