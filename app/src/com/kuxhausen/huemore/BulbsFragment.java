package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.kuxhausen.huemore.network.GetBulbList;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.Bulb;

public class BulbsFragment extends SherlockListFragment implements
		GetBulbList.OnBulbListReturnedListener {

	public TextView selected, longSelected; // updated on long click
	private int selectedPos = -1;
	private GodObject gbpfCallback;

	private GodObject parrentActivity;

	ArrayList<String> bulbNameList;
	ArrayAdapter<String> rayAdapter;
	Bulb[] bulbArray;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// We need to use a different list item layout for devices older than
		// Honeycomb
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.bulb_view, container, false);

		bulbNameList = new ArrayList<String>();
		rayAdapter = new ArrayAdapter<String>(this.getActivity(), layout,
				bulbNameList);
		setListAdapter(rayAdapter);
		parrentActivity.setBulbListenerFragment(this);
		refreshList();
		return myView;
	}

	public void refreshList() {
		GetBulbList pushGroupMood = new GetBulbList(getActivity(), this,
				parrentActivity);
		pushGroupMood.execute();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		gbpfCallback = (GodObject) activity;
		parrentActivity = (GodObject) activity;
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected
		// list item
		// (We do this during onStart because at the point the listview is
		// available.)
		// if (getFragmentManager().findFragmentById(R.id.groups_fragment) !=
		// null) {
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		// }
	}

	public void invalidateSelection() {
		// Set the previous selected item as checked to be unhighlighted when in
		// two-pane layout
		if (selected != null && selectedPos > -1)
			getListView().setItemChecked(selectedPos, false);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		longSelected = (TextView) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView;

		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_bulb, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		if (longSelected == null)
			return false;

		switch (item.getItemId()) {

		case R.id.contextgroupmenu_rename: // <-- your custom menu item id here
			EditBulbDialogFragment ngdf = new EditBulbDialogFragment();
			Bundle args = new Bundle();
			args.putString(InternalArguments.BULB_NAME,
					(String) (longSelected).getText());
			args.putInt(InternalArguments.BULB_NUMBER, 1 + rayAdapter
					.getPosition((String) (longSelected).getText()));
			ngdf.setArguments(args);
			ngdf.setBulbsFragment(this);
			ngdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		selected = ((TextView) (v));
		selectedPos = position;

		// Notify the parent activity of selected item
		Integer[] iPos = { position + 1 };
		gbpfCallback
				.onSelected(iPos, selected.getText().toString(), null, this);

		// Set the item as checked to be highlighted when in two-pane layout
		getListView().setItemChecked(selectedPos, true);
	}

	@Override
	public void onListReturned(Bulb[] result) {
		if (result == null)
			return;
		bulbArray = result;

		rayAdapter.clear();
		for (int i = 0; i < bulbArray.length; i++) {
			// bulbNameList.add(bulb.name);
			Bulb bulb = bulbArray[i];
			bulb.number = i + 1;
			rayAdapter.add(bulb.name);
		}

		registerForContextMenu(getListView());
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
	}
}
