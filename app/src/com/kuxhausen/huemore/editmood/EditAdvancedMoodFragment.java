package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.kuxhausen.huemore.BulbListFragment;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.GroupListFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.menu;
import com.kuxhausen.huemore.network.GetBulbList.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.support.v7.widget.GridLayout.Spec;

public class EditAdvancedMoodFragment extends SherlockFragment implements OnClickListener {

	Gson gson = new Gson();
	GridLayout grid;
	View contextView;
	
	ArrayList<MoodRow> dataRay = new ArrayList<MoodRow>();
	Button addChannel, addTimeslot;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.activity_edit_advanced_mood, null);
		
		
		addTimeslot = (Button) myView.findViewById(R.id.addTimeslotButton);
		addTimeslot.setOnClickListener(this);
		
		addChannel = (Button) myView.findViewById(R.id.addChannelButton);
		//int h = addChannel.getHeight();
		//int w = addChannel.getWidth();
		//addChannel.setRotation(270);
		//addChannel.setHeight(w);
		//addChannel.setWidth(h);
		addChannel.setOnClickListener(this);
		
		grid = (GridLayout) myView.findViewById(R.id.advancedGridLayout);
		grid.setColumnCount(2);
		grid.setRowCount(2);
		
		Log.e("colrow",grid.getColumnCount()+" "+grid.getRowCount());
		
		//rayAdapter = new MoodRowAdapter(this.getActivity(), new ArrayList<MoodRow>(), this, this);
	
		addState();
	    addState();
	    addState();
	    addState();
	    
	    redrawGrid();
	    
	    return myView;
	}
	
	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		dataRay.add(mr);
	}
	private void addState(int i) {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		dataRay.add(i, mr);
	}
	
	private void populateGrid(int index){
		GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
		vg.columnSpec = GridLayout.spec(index % grid.getColumnCount());
		vg.rowSpec = GridLayout.spec(index / grid.getRowCount());
		
		grid.addView(dataRay.get(index).getView(index, grid, this, this), vg);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		dataRay.get(requestCode).color = resultCode;
		dataRay.get(requestCode).hs = gson.fromJson(
				data.getStringExtra(InternalArguments.HUE_STATE),
				BulbState.class);

		/*String[] states = new String[moodRowArray.size()];
		for (int i = 0; i < moodRowArray.size(); i++) {
			states[i] = gson.toJson(moodRowArray.get(i).hs);
		}*/
		
		//Utils.transmit(this.getActivity(), InternalArguments.ENCODED_TRANSIENT_MOOD, getMood(), ((GodObject)this.getActivity()).getBulbs(), null);
		redrawGrid();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.clickable_layout:
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			//Bundle args = new Bundle();
			//args.putString(InternalArguments.PREVIOUS_STATE,
			//		gson.toJson(rayAdapter.getItem((Integer) v.getTag()).hs));
			//cpdf.setArguments(args);
			//cpdf.show(this.getFragmentManager(), "dialog");
			cpdf.setTargetFragment(this, (Integer) v.getTag());
			cpdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			break;
		case R.id.addChannelButton:
			addCol();
			break;
		case R.id.addTimeslotButton:
			addRow();
			break;
		}
	}

	private void redrawGrid() {
		grid.removeAllViews();
		for(int r = 0; r< grid.getRowCount(); r++)
			for(int c = 0; c<grid.getColumnCount(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c);
				vg.rowSpec = GridLayout.spec(r);
				
				grid.addView(dataRay.get(r*grid.getColumnCount()+c).getView((r*grid.getColumnCount()+c), grid, this, this), vg);
			}
		
		grid.invalidate();
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		contextView = v;
		
		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contextmoodmenu_edit:
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			cpdf.setTargetFragment(this, (Integer) contextView.getTag());
			cpdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.contextmoodmenu_delete:
			return true;
		case R.id.contextstatemenu_delete_timeslot:
			deleteRow((Integer)contextView.getTag());
			return true;
		case R.id.contextstatemenu_delete_channel:
			deleteCol((Integer)contextView.getTag());
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	/*public int locate(View v){
		for (int i = 0; i<dataRay.size(); i++){
			if(dataRay.get(i).getView(i, grid, this, this).equals(v))
					return i;
		}
		return -1;
	}*/
	
	private void deleteRow(int item){
		int row = item / grid.getColumnCount();
		int col = item % grid.getColumnCount();
		Log.e("blar", "i"+ item +" r"+ row + " c"+col);
		ArrayList<MoodRow> toRemove = new ArrayList<MoodRow>();
		for(int i = 0; i<grid.getColumnCount(); i++){
			toRemove.add(dataRay.get(i + row*grid.getColumnCount()));
		}
		for(MoodRow kill : toRemove)
			dataRay.remove(kill);
		grid.setRowCount(grid.getRowCount()-1);
		redrawGrid();
	}
	private void deleteCol(int item){
		int row = item / grid.getColumnCount();
		int col = item % grid.getColumnCount();
		Log.e("blar", "i"+ item +" r"+ row + " c"+col);
		ArrayList<MoodRow> toRemove = new ArrayList<MoodRow>();
		for(int i = 0; i<grid.getRowCount(); i++){
			Log.e("omg", col+" "+i*grid.getColumnCount());
			toRemove.add(dataRay.get(col + i*grid.getColumnCount()));
		}
		for(MoodRow kill : toRemove)
			dataRay.remove(kill);
		grid.setRowCount(grid.getColumnCount()-1);
		redrawGrid();
	}
	private void addRow(){
		grid.setRowCount(grid.getRowCount()+1);
		for(int i = grid.getColumnCount(); i>0; i--){
			addState();
		}
		redrawGrid();
	}
	private void addCol(){
		int width = grid.getColumnCount();
		grid.setColumnCount(1+width);
		for(int i = dataRay.size(); i>0; i-=width){
			addState(i);
		}
		redrawGrid();
	}
}
