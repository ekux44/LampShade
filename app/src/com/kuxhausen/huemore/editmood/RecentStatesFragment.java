package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;
import java.util.HashSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.api.BulbState;

public class RecentStatesFragment extends SherlockFragment implements OnCreateColorListener, OnClickListener{

	private GridView g;
	StateCellAdapter adapter;
	private int lastSelectedPosition = -1;
	Gson gson = new Gson();
	ArrayList<StateCell> list;
	EditStatePagerDialogFragment statePager;
	
	public boolean loadPrevious(BulbState bs, ArrayList<StateCell> cells){
		list = new ArrayList<StateCell>();
		HashSet<String> bulbStateHash = new HashSet<String>();
		for(StateCell cell : cells){
			if(!bulbStateHash.contains(cell.hs.toString())){
				bulbStateHash.add(cell.hs.toString());
				list.add(cell);
			}
		}
		if(bulbStateHash.contains(bs.toString()))
			return true;
		return false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		g = (GridView) myView.findViewById(R.id.myGrid);
		adapter = new StateCellAdapter(this, list, this);
		g.setAdapter(adapter);
		
		if(lastSelectedPosition>-1)
			onClick(g.getAdapter().getView(lastSelectedPosition, null, g));
		
		return myView;
	}	

	@Override
	public void onClick(View v) {
		if(lastSelectedPosition > -1)
			list.get(lastSelectedPosition).selected = false;
		lastSelectedPosition = (Integer) v.getTag();
		list.get(lastSelectedPosition).selected = true;
		adapter.notifyDataSetChanged();
		statePager.setState(list.get(lastSelectedPosition).hs, this);
	}
	

	@Override
	public boolean stateChanged() {
		for(int i = 0; i< list.size(); i++){
			StateCell cell = list.get(i);
			if(cell.hs.toString().equals(statePager.getState().toString())){
				if(lastSelectedPosition > -1)
					list.get(lastSelectedPosition).selected = false;
				lastSelectedPosition = i;
				list.get(lastSelectedPosition).selected = true;
				
				if(adapter!=null)
					adapter.notifyDataSetChanged();
				return true;
			}
		}
			
		return false;
	}
	@Override
	public void setStatePager(EditStatePagerDialogFragment statePage) {
		statePager = statePage;		
	}
}
