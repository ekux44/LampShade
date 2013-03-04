package com.kuxhausen.huemore;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.kuxhausen.huemore.DatabaseDefinitions.GroupColumns;

public class GroupsFragment extends ListFragment implements OnClickListener {
	OnHeadlineSelectedListener mCallback;

	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnHeadlineSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onArticleSelected(int position);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		String[] columns = { GroupColumns.GROUP, GroupColumns._ID };
		CursorAdapter dataSource = new SimpleCursorAdapter(this.getActivity(),
				R.layout.group_row,
				((MainActivity) this.getActivity()).helper.getGroupCursor(),
				columns, new int[] { R.id.groupTextView }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		setListAdapter(dataSource);

		View myView = inflater.inflate(R.layout.group_view, container, false);

		Button newGroup = (Button) myView.findViewById(R.id.newGroupButton);
		newGroup.setOnClickListener(this);

		// Inflate the layout for this fragment
		return myView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		if (getFragmentManager().findFragmentById(R.id.groups_fragment) != null) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception.
		try {
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Notify the parent activity of selected item
		mCallback.onArticleSelected(position);

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(position, true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.newGroupButton:

			NewGroupDialogFragment ngdf = new NewGroupDialogFragment();
			ngdf.show(getFragmentManager(), "dialog");
			
			//RegisterWithHubDialogFragment rwhdf = new RegisterWithHubDialogFragment();
			//rwhdf.show(getFragmentManager(), "dialog");
			break;
		}
	}
}