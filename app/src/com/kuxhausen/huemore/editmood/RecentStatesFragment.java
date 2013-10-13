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
	private View lastSelection;
	private StateCell lastSelectedRow;
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
		g.setAdapter(new StateCellAdapter(this, list, this));
		
		return myView;
	}	

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();

		v.setBackgroundColor(0xFFFFBB33);
		if(lastSelection!=null)
			lastSelection.setBackgroundColor(0);
		
		lastSelection = v;
		lastSelectedRow = (StateCell)g.getAdapter().getItem(position);
		statePager.setState(lastSelectedRow.hs, this);
	}
	

	@Override
	public boolean stateChanged() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setStatePager(EditStatePagerDialogFragment statePage) {
		statePager = statePage;		
	}
}
