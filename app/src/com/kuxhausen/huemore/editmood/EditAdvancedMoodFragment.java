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
import android.widget.Spinner;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.support.v7.widget.GridLayout.Spec;

public class EditAdvancedMoodFragment extends SherlockFragment implements OnClickListener {

	Gson gson = new Gson();
	GridLayout grid;
	View contextView;
	
	ArrayList<MoodRow> dataRay = new ArrayList<MoodRow>();
	ArrayList<Spinner> timeslotSpinners = new ArrayList<Spinner>();
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
		grid.setColumnCount(3);
		grid.setRowCount(1);
		
		Log.e("colrow",grid.getColumnCount()+" "+grid.getRowCount());
		
		addRow();
		addRow();
	    
	    redrawGrid();
	    
	    return myView;
	}
	
	private MoodRow generateDefaultMoodRow(){
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		return mr;
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
			redrawGrid();
			break;
		case R.id.addTimeslotButton:
			addRow();
			redrawGrid();
			break;
		}
	}

	private void redrawGrid() {
		grid.removeAllViews();
		for(int r = 0; r< gridRows(); r++)
			for(int c = 0; c<gridCols(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c+initialCols);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				
				grid.addView(dataRay.get(r*gridCols()+c).getView((r*gridCols()+c), grid, this, this), vg);
			}
		for(int r = 0; r<timeslotSpinners.size(); r++){
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(r+initialRows);
			
			grid.addView(timeslotSpinners.get(r), vg);
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v =inflater.inflate(R.layout.grid_col_channels_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, this.gridCols());
			vg.rowSpec = GridLayout.spec(0);
			grid.addView(v, vg);
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(0);
			grid.addView(v, vg);
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
		case R.id.contextstatemenu_edit:
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			cpdf.setTargetFragment(this, (Integer) contextView.getTag());
			cpdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			return true;
		case R.id.contextstatemenu_delete:
			delete((Integer)contextView.getTag());
			redrawGrid();
			return true;
		case R.id.contextstatemenu_delete_timeslot:
			deleteRow((Integer)contextView.getTag());
			redrawGrid();
			return true;
		case R.id.contextstatemenu_delete_channel:
			deleteCol((Integer)contextView.getTag());
			redrawGrid();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	private void addState() {
		dataRay.add(generateDefaultMoodRow());
	}
	private void addState(int item) {
		dataRay.add(item, generateDefaultMoodRow());
	}
	private void delete(int item){
		dataRay.set(item, generateDefaultMoodRow());
	}
	
	private void deleteRow(int item){
		int row = item / gridCols();
		ArrayList<MoodRow> toRemove = new ArrayList<MoodRow>();
		for(int i = 0; i<gridCols(); i++){
			toRemove.add(dataRay.get(i + row*gridCols()));
		}
		for(MoodRow kill : toRemove)
			dataRay.remove(kill);
		
		timeslotSpinners.remove(row);
		
		grid.setRowCount(initialRows + gridRows()-1);
	}
	private void deleteCol(int item){
		int col = item % gridCols();
		ArrayList<MoodRow> toRemove = new ArrayList<MoodRow>();
		for(int i = 0; i<gridRows(); i++){
			Log.e("omg", col+" "+i*gridCols());
			toRemove.add(dataRay.get(col + i*gridCols()));
		}
		for(MoodRow kill : toRemove)
			dataRay.remove(kill);
		grid.setColumnCount(initialCols+gridCols()-1);
	}
	private void addRow(){
		grid.setRowCount(initialRows + gridRows()+1);
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		timeslotSpinners.add((Spinner)inflater.inflate(R.layout.timeslot_spinner, null));
				
		for(int i = gridCols(); i>0; i--){
			addState();
		}
	}
	private void addCol(){
		int width = gridCols();
		grid.setColumnCount(1+width+initialCols);
		for(int i = dataRay.size(); i>0; i-=width){
			addState(i);
		}
	}
	private final int initialRows = 1;
	private final int initialCols = 1 ;
	private final int gridRows(){
		return grid.getRowCount()-initialRows;
	}
	private final int gridCols(){
		return grid.getColumnCount()-initialCols;
	}
}
