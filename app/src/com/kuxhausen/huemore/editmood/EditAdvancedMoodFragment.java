package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.widget.GridLayout;

public class EditAdvancedMoodFragment extends SherlockFragment implements OnClickListener, OnCheckedChangeListener {

	Gson gson = new Gson();
	GridLayout grid;
	View contextView;
	
	ArrayList<MoodRow> dataRay = new ArrayList<MoodRow>();
	ArrayList<Spinner> timeslotSpinners = new ArrayList<Spinner>();
	Button addChannel, addTimeslot;
	EditText moodName;
	CheckBox loop;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.edit_advanced_mood, null);
		
		moodName = (EditText)myView.findViewById(R.id.moodNameEditText);
		
		loop = (CheckBox)myView.findViewById(R.id.loopCheckBox);
		loop.setOnCheckedChangeListener(this);
		
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
		grid.setColumnCount(initialCols+2);
		grid.setRowCount(initialRows);
		
		Log.e("colrow",grid.getColumnCount()+" "+grid.getRowCount());
		
		addRow();
		addRow();
		addRow();
	    
	    redrawGrid();
	    
	    return myView;
	}
	
	private MoodRow generateDefaultMoodRow(){
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		mr.hs = example;
		return mr;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		dataRay.get(requestCode).color = resultCode;
		dataRay.get(requestCode).hs = gson.fromJson(
				data.getStringExtra(InternalArguments.HUE_STATE),
				BulbState.class);

		redrawGrid();
	}
	
	private void preview(){
		
		Log.e("encodedMood",HueUrlEncoder.encode(getMood()));
		Utils.transmit(this.getActivity(), InternalArguments.ENCODED_MOOD, getMood(), ((GodObject)this.getActivity()).getBulbs(), moodName.getText().toString());
		
	}
	
	private Mood getMood() {
		//todo calculate dynamically for each timeslot
		int transitionTime = 10;
		
		Mood m = new Mood();
		m.usesTiming = true; //TODO not always the case...
		m.numChannels = gridCols();
		m.timeAddressingRepeatPolicy = false;
		m.setInfiniteLooping(loop.isChecked());
		
		ArrayList<Event> events = new ArrayList<Event>();
		for(int i = 0; i< dataRay.size(); i++){
			MoodRow mr = dataRay.get(i);
			Log.e("mr",mr.hs.toString());
			
			if(mr.hs!=null && !mr.hs.toString().equals("")){
				int row = i / gridCols();
				int col = i % gridCols();
				
				Event e = new Event();
				e.channel = col;
				e.time = row * transitionTime; //TODO actually calculate this with compounding transition times
				e.state = mr.hs;
				events.add(e);
			}
		}
		Event[] eRay = new Event[events.size()];
		for(int i = 0; i<eRay.length; i++)
			eRay[i] = events.get(i);
		
		m.events = eRay;
		return m;
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
			vg.setGravity(Gravity.CENTER);
			grid.addView(timeslotSpinners.get(r), vg);
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v =inflater.inflate(R.layout.grid_col_channels_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, this.gridCols());
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(v, vg);
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(v, vg);
		}
		{
			LayoutInflater inflater = this.getActivity().getLayoutInflater();
			ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_vertical_seperator, null);

			ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
			rowView.setImageDrawable(cd);
			rowView.setMinimumWidth(1);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(1);
			vg.rowSpec = GridLayout.spec(0, initialRows+gridRows());
			vg.setGravity(Gravity.FILL_VERTICAL);
			grid.addView(rowView, vg);
		}
		{
			LayoutInflater inflater = this.getActivity().getLayoutInflater();
			ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_horizontal_seperator, null);

			ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
			rowView.setImageDrawable(cd);
			rowView.setMinimumHeight(1);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0,initialCols+gridCols());
			vg.rowSpec = GridLayout.spec(1);
			vg.setGravity(Gravity.FILL_HORIZONTAL);
			grid.addView(rowView, vg);
		}
		grid.invalidate();
		preview();
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
	private final int initialRows = 2;
	private final int initialCols = 2 ;
	private final int gridRows(){
		return grid.getRowCount()-initialRows;
	}
	private final int gridCols(){
		return grid.getColumnCount()-initialCols;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		
	}
}
