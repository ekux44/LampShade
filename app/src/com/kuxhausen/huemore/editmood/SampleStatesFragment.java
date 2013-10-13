package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
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

public class SampleStatesFragment extends SherlockFragment implements OnCreateColorListener, OnClickListener{

	private GridView g;
	StateCellAdapter adapter;
	private int lastSelectedPosition = -1;
	Gson gson = new Gson();
	EditStatePagerDialogFragment statePager;
	ArrayList<StateCell> list;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		list = new ArrayList<StateCell>();
		
		String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea1", "Deep Sea2", "Fruit1", "Fruit2", "Fruit3"};
		float[] simpleX = {0.4571f, 0.5119f, 0.368f, 0.3151f, 0.1859f, 0.6367f, 0.5089f, 0.5651f, 0.4081f};
		float[] simpleY = {0.4123f, 0.4147f, 0.3686f, 0.3252f, 0.0771f, 0.3349f, 0.438f, 0.3306f, 0.518f};
		    
		for(int i = 0; i<simpleX.length; i++){
			BulbState hs = new BulbState();
			Float[] conversionXY = {simpleX[i], simpleY[i]};
			hs.xy = conversionXY;
	    	hs.on=true;
	    	hs.effect="none";
	    	hs.transitiontime = 4; //TODO remove hard coding of all this stuff
	    	
	    	StateCell mr = new StateCell(this.getActivity());
	    	mr.hs = hs;
	    	mr.name = simpleNames[i];
	    	list.add(mr);
    	
		}
		{
			BulbState hs = new BulbState();
			hs.on=true;
	    	hs.effect="none";
			
			StateCell mr = new StateCell(this.getActivity());
			mr.hs = hs;
			mr.name = this.getActivity().getResources().getString(R.string.cap_on);
			list.add(mr);
		}
		{
			BulbState hs = new BulbState();
			hs.on=false;
	    	hs.effect="none";
			
			StateCell mr = new StateCell(this.getActivity());
			mr.hs = hs;
			mr.name = this.getActivity().getResources().getString(R.string.cap_off);
			list.add(mr);
		}
		
		
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
		if(list==null)
			return false;
		
		if(lastSelectedPosition > -1){
			list.get(lastSelectedPosition).selected = false;
			adapter.notifyDataSetChanged();
		}
		
		for(int i = 0; i< list.size(); i++){
			StateCell cell = list.get(i);
			if(cell.hs.toString().equals(statePager.getState().toString())){
				
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
