package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

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

public class SampleStatesFragment extends SherlockFragment implements OnCreateColorListener, OnClickListener{

	private GridView g;
	private View lastSelection;
	private StateCell lastSelectedRow;
	Gson gson = new Gson();
	EditStatePagerDialogFragment statePager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		
		ArrayList<StateCell> list = new ArrayList<StateCell>();
		
		String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea1", "Deep Sea2", "Fruit1", "Fruit2", "Fruit3"};
		float[] simpleX = {0.4571f, 0.5119f, 0.368f, 0.3151f, 0.1859f, 0.6367f, 0.5089f, 0.5651f, 0.4081f};
		float[] simpleY = {0.4123f, 0.4147f, 0.3686f, 0.3252f, 0.0771f, 0.3349f, 0.438f, 0.3306f, 0.518f};
		    
		for(int i = 0; i<simpleX.length; i++){
			BulbState hs = new BulbState();
			Float[] conversionXY = {simpleX[i], simpleY[i]};
			hs.xy = conversionXY;
	    	hs.on=true;
	    	hs.effect="none";
	    	
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
