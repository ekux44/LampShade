package com.kuxhausen.huemore.net;

import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.OnServiceConnectedListener;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.ui.ConfigureHubDialogFragment;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class ConnectionListFragment extends ListFragment implements OnConnectionStatusChangedListener, OnServiceConnectedListener{

	private NavigationDrawerActivity mParent;
	private ArrayAdapter<Connection> aa;
	private int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
	private Connection selectedRow;
	
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
		registerForContextMenu(getListView());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		View connectionHolder = ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView.findViewById(R.id.mainTextView);
		selectedRow = ((Connection) connectionHolder.getTag());
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_connections, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.contextconnectionsmenu_edit:
			if(selectedRow instanceof HubConnection){
				ConfigureHubDialogFragment editFrag = new ConfigureHubDialogFragment();
				editFrag.setPriorConnection((HubConnection) selectedRow);
				editFrag.show(getFragmentManager(),InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			} else{
				
				//TODO hook into other supported systems
			}
			
			return true;
		case R.id.contextconnectionsmenu_delete:
			//TODO
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
