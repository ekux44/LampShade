package com.kuxhausen.huemore.net;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class ConnectionListFragment extends ListFragment {

	private NavigationDrawerActivity mParent;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParent = (NavigationDrawerActivity) this.getActivity();
		
		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;

		String[] test123 = {"test 1", "test 2", "test 3"};
		ArrayAdapter<String> aa = new ArrayAdapter<String>(mParent, layout, test123);
		setListAdapter(aa);

		View myView = inflater.inflate(R.layout.connections_list_fragment, null);

		setHasOptionsMenu(true);
		getActivity().supportInvalidateOptionsMenu();
		
		mParent.getSupportActionBar().setHomeButtonEnabled(false);
		
		return myView;
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.action_connections, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_connection:
				NewConnectionFragment dialogFrag = new NewConnectionFragment();
				dialogFrag.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
