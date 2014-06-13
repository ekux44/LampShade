package com.kuxhausen.huemore.net;

import java.util.ArrayList;

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
import com.kuxhausen.huemore.OnServiceConnectedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class ConnectionListFragment extends ListFragment implements OnConnectionStatusChangedListener, OnServiceConnectedListener{

	private NavigationDrawerActivity mParent;
	private ArrayAdapter<Connection> aa;
	private int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParent = (NavigationDrawerActivity) this.getActivity();
		
		View myView = inflater.inflate(R.layout.connections_list_fragment, null);
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

	@Override
	public void onResume(){
		super.onResume();
		mParent.registerOnServiceConnectedListener(this);
		
		this.setHasOptionsMenu(true);
		getActivity().supportInvalidateOptionsMenu();
		mParent.getSupportActionBar().setHomeButtonEnabled(false);
		
	}
	@Override
	public void onServiceConnected() {
		mParent.getService().getDeviceManager().addOnConnectionStatusChangedListener(this);
		this.onConnectionStatusChanged();
	}
	public void onPause(){
		super.onPause();
		if(mParent.boundToService())
			mParent.getService().getDeviceManager().removeOnConnectionStatusChangedListener(this);
	}

	@Override
	public void onConnectionStatusChanged() {
		ArrayList<Connection> connections = mParent.getService().getDeviceManager().getConnections();
		if(connections == null)
			return;
		aa = new ConnectionRowAdapter(mParent, layout, connections);
		setListAdapter(aa);

	}

}
