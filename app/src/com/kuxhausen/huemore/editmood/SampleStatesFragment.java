package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class SampleStatesFragment extends SherlockFragment implements OnCreateColorListener, OnClickListener{

	private GridView g;
	private View lastSelection;
	private StateCell lastSelectedRow;
	Gson gson = new Gson();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		
		ArrayList<StateCell> list = new ArrayList<StateCell>();
		
		String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea", "Deep Sea", "Fruit", "Fruit", "Fruit"};
		int[] simpleSat = {144, 211 ,49, 232, 253, 230, 244, 254, 173};
		int[] simpleHue = {15331, 13122, 33863, 34495, 45489, 1111, 15483, 25593, 64684};
		    
		for(int i = 0; i<simpleSat.length; i++){
			BulbState hs = new BulbState();
	    	hs.sat=(short)simpleSat[i];
	    	hs.hue=simpleHue[i];
	    	hs.on=true;
	    	hs.effect="none";
	    	
	    	StateCell mr = new StateCell();
	    	mr.hs = hs;
	    	mr.name = simpleNames[i];
	    	list.add(mr);
    	
		}
		
		
		g = (GridView) myView.findViewById(R.id.myGrid);
		g.setAdapter(new StateCellAdapter(this, list, this));
		
		//set up an initial selection
		//onClick(lastSelectedRow.getView(0, null, this, this));
		
		return myView;
	}
	
	@Override
	public Intent onCreateColor(Integer transitionTime) {
		if(lastSelectedRow!=null){
			lastSelectedRow.hs.transitiontime = transitionTime;
			Intent i = new Intent();
			i.putExtra(InternalArguments.HUE_STATE, gson.toJson(lastSelectedRow.hs));
		return i;
		}
		return null;
	}
	

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();

		v.setBackgroundColor(0xFFFFBB33);
		if(lastSelection!=null)
			lastSelection.setBackgroundColor(0);
		
		lastSelection = v;
		lastSelectedRow = (StateCell)g.getAdapter().getItem(position);
		preview();
	}
	
	public void preview() {
		if(lastSelectedRow!=null){
			Mood m = Utils.generateSimpleMood(lastSelectedRow.hs);	
			Utils.transmit(this.getActivity(), InternalArguments.ENCODED_TRANSIENT_MOOD, m, ((GodObject) getActivity()).getBulbs(), null);
		}
	}
}
