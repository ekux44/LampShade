package com.kuxhausen.huemore;

import com.kuxhausen.huemore.DatabaseDefinitions.GroupColumns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class GroupSelectorDialogFragment extends DialogFragment implements
		OnClickListener {

	public interface OnGroupSelectedListener {
		void groupSelected(String group);
	}

	// Identifies a particular Loader being used in this component
	private static final int GROUPS_LOADER = 0;
	public CursorAdapter dataSource;
	ListView listView;
	Cursor cursor;
	private OnGroupSelectedListener mListener;

	public void setOnGroupSelectedListener(OnGroupSelectedListener listener) {
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		String[] columns = { GroupColumns.GROUP, GroupColumns._ID };

		cursor = getActivity().getContentResolver().query(
				DatabaseDefinitions.GroupColumns.GROUPS_URI, // Use the default
																// content URI
																// for the
																// provider.
				columns, // Return the note ID and title for each note.
				null, // No where clause, return all records.
				null, // No where clause, therefore no where column values.
				null // Use the default sort order.
				);

		CursorAdapter dataSource = new SimpleCursorAdapter(this.getActivity(),
				android.R.layout.simple_list_item_1, cursor, columns,
				new int[] { android.R.id.text1 }, 0);
		builder.setAdapter(dataSource, this);
		builder.setTitle("Pick a group to preview with");

		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		cursor.moveToFirst();
		cursor.move(which);
		mListener.groupSelected(cursor.getString(0));
	}

}
