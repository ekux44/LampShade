package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.widget.GridLayout;

public class EditAdvancedMoodFragment extends SherlockFragment implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener, OnCreateMoodListener {

	Gson gson = new Gson();
	GridLayout grid;
	int contextSpot;
	
	public ArrayList<StateCell> dataRay = new ArrayList<StateCell>();
	ArrayList<TimeslotDuration> timeslotDuration = new ArrayList<TimeslotDuration>();
	HashMap<Integer, TimeslotDuration> timeslotDurationById = new HashMap<Integer, TimeslotDuration>();
	int[] timeslotValues;
	Button addChannel, addTimeslot;
	EditText moodName;
	CheckBox loop;
	static String priorName;
	static Mood priorMood;
	
	private boolean timedMode;
	private boolean multiMode;
	
	public void setTimedMode(){
		timedMode = true;
	}
	public void setMultiMode(){
		multiMode = true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.edit_advanced_mood, null);
		
		timeslotValues = getActivity().getResources().getIntArray(
				R.array.timeslot_values_array);
		
		moodName = (EditText)myView.findViewById(R.id.moodNameEditText);
		
		loop = (CheckBox)myView.findViewById(R.id.loopCheckBox);
		
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
		grid.removeAllViews();
		timeslotDuration.clear();
		timeslotDurationById.clear();
		dataRay.clear();
		grid.setColumnCount(initialCols+1);
		grid.setRowCount(initialRows);
		
		Log.e("colrow",grid.getColumnCount()+" "+grid.getRowCount());
		addRow(timeslotValues[0]);
		if(!multiMode){
			addRow(timeslotValues[0]);
			addRow(timeslotValues[0]);
		} else{
			myView.findViewById(R.id.addTimeslotButton).setVisibility(View.GONE);
		}
		
		if(!timedMode){
			addCol();
		} else{
			loop.setChecked(true);
			myView.findViewById(R.id.addChannelButton).setVisibility(View.GONE);
		}
	    
		loop.setOnCheckedChangeListener(this);
	    redrawGrid();
	    
	    if(timedMode || multiMode){
	    	myView.findViewById(R.id.confirmationBar).setVisibility(View.GONE);
	    	myView.findViewById(R.id.advancedLinearLayout).setVisibility(View.GONE);
	    }
	    else
	    {
		    Button cancelButton = (Button) myView.findViewById(R.id.cancel);
			cancelButton.setOnClickListener(this);
			Button okayButton = (Button) myView.findViewById(R.id.okay);
			okayButton.setOnClickListener(this);
	    }
		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			//load prior mood
			loadMood(Utils.getMoodFromDatabase(args.getString(InternalArguments.MOOD_NAME), this.getActivity()));
		}
	    
	    return myView;
	}
	
	private StateCell generateDefaultMoodRow(){
		StateCell mr = new StateCell();
		BulbState example = new BulbState();
		mr.hs = example;
		return mr;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		dataRay.get(requestCode).hs = gson.fromJson(
				data.getStringExtra(InternalArguments.HUE_STATE),
				BulbState.class);

		redrawGrid();
	}
	
	private void stopPreview(){
		Mood m = new Mood();
		Utils.transmit(this.getActivity(), InternalArguments.ENCODED_MOOD, m, ((GodObject)this.getActivity()).getBulbs(), "");
	}
	private void preview(){
		Utils.transmit(this.getActivity(), InternalArguments.ENCODED_MOOD, getMood(), ((GodObject)this.getActivity()).getBulbs(), moodName.getText().toString());
		
	}
	
	private void loadMood(Mood mFromDB) {
		Log.e("loadMood", mFromDB.numChannels+" "+ mFromDB.events.length);
		
		this.setGridCols(mFromDB.numChannels);
		int row = -1;
		int time = -1;
		for(Event e: mFromDB.events){
			if(e.time!=time){
				row++;
				setGridRows(row+1, e.time - time);
				time=e.time;
			}
		}
		if(mFromDB.usesTiming)
			setGridRows(row+1, mFromDB.loopIterationTimeLength - time);
		
		row = -1;
		time = -1;
		for(Event e: mFromDB.events){
			if(e.time!=time){
				row++;
				time = e.time;
			}
			dataRay.get(gridCols()*row + e.channel).hs = e.state;
		}
		redrawGrid();
	}
	private Mood getMood() {		
		Mood m = new Mood();
		m.usesTiming = true; //TODO not always the case...
		m.numChannels = gridCols();
		m.timeAddressingRepeatPolicy = false;
		m.setInfiniteLooping(loop.isChecked());
		
		ArrayList<Event> events = new ArrayList<Event>();
		for(int i = 0; i< dataRay.size(); i++){
			StateCell mr = dataRay.get(i);
			
			if(mr.hs!=null && !mr.hs.toString().equals("")){
				int row = i / gridCols();
				int col = i % gridCols();
				
				Event e = new Event();
				e.channel = col;
				e.time = getTime(row);
				e.state = mr.hs;
				events.add(e);
			}
		}
		Event[] eRay = new Event[events.size()];
		for(int i = 0; i<eRay.length; i++)
			eRay[i] = events.get(i);
		
		m.events = eRay;
		m.loopIterationTimeLength = getTime(this.gridRows());
		return m;
	}
	private int getTime(int row){
		int time = 0;
		for(int i = row-1; i>=0; i--){
			time+=timeslotDuration.get(i).duration;
		}
		return time;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.clickable_layout:
			stopPreview();
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			cpdf.setParrentMood(this);
			Bundle args = new Bundle();
			args.putString(InternalArguments.PREVIOUS_STATE,
					gson.toJson(dataRay.get((Integer) v.getTag()).hs));
			cpdf.setArguments(args);
			cpdf.setTargetFragment(this, (Integer) v.getTag());
			cpdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			break;
		case R.id.addChannelButton:
			addCol();
			redrawGrid();
			break;
		case R.id.addTimeslotButton:
			addRow(timeslotValues[0]);
			redrawGrid();
			break;
		case R.id.okay:
			if (priorName != null) {
				// delete old mood
				String moodSelect = MoodColumns.MOOD + "=?";
				String[] moodArg = { priorName };
				this.getActivity().getContentResolver().delete(
						DatabaseDefinitions.MoodColumns.MOODS_URI,
						moodSelect, moodArg);
			}
			this.onCreateMood(moodName.getText().toString());
			getActivity().onBackPressed();
			break;
		case R.id.cancel:
			getActivity().onBackPressed();
			break;
		}
		
	}

	private void redrawGrid() {
		Log.e("redraw","redraw grid");
		
		grid.removeAllViews();
		for(int r = 0; r< gridRows(); r++)
			for(int c = 0; c<gridCols(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c+initialCols);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				View v = dataRay.get(r*gridCols()+c).getView((r*gridCols()+c), grid, this, this);
				v.setTag(r*this.gridCols()+c);
				grid.addView(v, vg);
			}
		if(!multiMode){
			for(int r = 0; r<timeslotDuration.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = timeslotDuration.get(r).spin;
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				grid.addView(v, vg);
				
				
			}
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
		if(!multiMode){
			{
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(0);
				vg.setGravity(Gravity.CENTER);
				grid.addView(v, vg);
			}
		}
		if(!multiMode){
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

		contextSpot = (Integer)v.getTag();
		Log.e("cv!=null",""+(v!=null));
		Log.e("cv tag!=null",""+(v.getTag()!=null));
		Log.e("cv tag",v.getTag().toString());
		
		if(timedMode){
			android.view.MenuInflater inflater = this.getActivity()
					.getMenuInflater();
			inflater.inflate(R.menu.context_timed_state, menu);
		} else if (multiMode){
			android.view.MenuInflater inflater = this.getActivity()
					.getMenuInflater();
			inflater.inflate(R.menu.context_multi_state, menu);
		} else{
		android.view.MenuInflater inflater = this.getActivity()
				.getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if(timedMode){
			switch (item.getItemId()) {
			case R.id.contexttimedmenu_edit:
				stopPreview();
				EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
				cpdf.setParrentMood(this);
				Bundle args = new Bundle();
				args.putString(InternalArguments.PREVIOUS_STATE,
						gson.toJson(dataRay.get(contextSpot).hs));
				cpdf.setArguments(args);
				cpdf.setTargetFragment(this, contextSpot);
				cpdf.show(getFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			case R.id.contexttimedmenu_delete:
				delete(contextSpot);
				redrawGrid();
				return true;
			case R.id.contexttimedmenu_delete_timeslot:
				deleteRow(contextSpot);
				redrawGrid();
				return true;
			default:
				return super.onContextItemSelected(item);
			}
		}else if(multiMode){
			switch (item.getItemId()) {
			case R.id.contextmultimenu_edit:
				stopPreview();
				EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
				cpdf.setParrentMood(this);
				Bundle args = new Bundle();
				args.putString(InternalArguments.PREVIOUS_STATE,
						gson.toJson(dataRay.get(contextSpot).hs));
				cpdf.setArguments(args);
				cpdf.setTargetFragment(this, contextSpot);
				cpdf.show(getFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			case R.id.contextmultimenu_delete:
				delete(contextSpot);
				redrawGrid();
				return true;
			case R.id.contextmultimenu_delete_channel:
				deleteCol(contextSpot);
				redrawGrid();
				return true;
			default:
				return super.onContextItemSelected(item);
			}
		}else{
			switch (item.getItemId()) {
			case R.id.contextstatemenu_edit:
				stopPreview();
				EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
				cpdf.setParrentMood(this);
				Bundle args = new Bundle();
				args.putString(InternalArguments.PREVIOUS_STATE,
						gson.toJson(dataRay.get(contextSpot).hs));
				cpdf.setArguments(args);
				cpdf.setTargetFragment(this, contextSpot);
				cpdf.show(getFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			case R.id.contextstatemenu_delete:
				delete(contextSpot);
				redrawGrid();
				return true;
			case R.id.contextstatemenu_delete_timeslot:
				deleteRow(contextSpot);
				redrawGrid();
				return true;
			case R.id.contextstatemenu_delete_channel:
				deleteCol(contextSpot);
				redrawGrid();
				return true;
			default:
				return super.onContextItemSelected(item);
			}
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
		Log.e("deleteRow", item+" "+gridRows());
		if(gridRows()>1){
			int row = (item / gridCols())-1;//-1?
			ArrayList<StateCell> toRemove = new ArrayList<StateCell>();
			for(int i = 0; i<gridCols(); i++){
				toRemove.add(dataRay.get(i + row*gridCols()));
			}
			for(StateCell kill : toRemove)
				dataRay.remove(kill);
			
			timeslotDurationById.remove(timeslotDuration.get(row).id);
			timeslotDuration.remove(row);
			
			grid.setRowCount(initialRows + gridRows()-1);
		}
	}
	private void deleteCol(int item){
		//Log.e("deleteCol",item + "  "+gridCols());
		
		if(gridCols()>1){
			int col = item % gridCols();
			ArrayList<StateCell> toRemove = new ArrayList<StateCell>();
			for(int i = 0; i<gridRows(); i++){
				Log.e("omg", col+" "+i*gridCols());
				toRemove.add(dataRay.get(col + i*gridCols()));
			}
			for(StateCell kill : toRemove)
				dataRay.remove(kill);
			grid.setColumnCount(initialCols+gridCols()-1);
		}
	}
	private void addRow(int duration){
		if(gridRows()<=8){
			grid.setRowCount(initialRows + gridRows()+1);
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			TimeslotDuration td = new TimeslotDuration();
			td.spin = (Spinner)inflater.inflate(R.layout.timeslot_spinner, null);
			td.id = getSpinnerId();
			td.spin.setId(td.id);
			td.duration = duration;
			timeslotDuration.add(td);
			timeslotDurationById.put(td.id, td);
			td.spin.setOnItemSelectedListener(this);
			
			for(int i = gridCols(); i>0; i--){
				addState();
			}
		}
	}
	private void addCol(){
		if(gridCols()<8){
			int width = gridCols();
			grid.setColumnCount(1+width+initialCols);
			for(int i = dataRay.size(); i>0; i-=width){
				addState(i);
			}
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
	private final void setGridRows(int num, int duration){
		Log.e("setGridRows",num+"num , duration"+duration);
		
		while(gridRows()!=num){
			Log.e("gridRows",gridRows()+"gr num"+num);
			if(gridRows()<num)
				addRow(duration);
			else if(gridRows()>num)
				deleteRow(gridRows()-1);
		}
	}
	private final void setGridCols(int num){
		Log.e("setGridCols",num+"");
		while(gridCols()!=num){
			Log.e("gridCols",gridCols()+"gc num"+num);
			if(gridCols()<num)
				addCol();
			else if(gridCols()>num)
				deleteCol(gridCols()-1);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		preview();
	}

	@Override
	public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
		if(position<timeslotValues.length-1){
			Log.e("position",""+position);
			Log.e("id",""+parent.getId());
			TimeslotDuration td = timeslotDurationById.get(parent.getId());
			Log.e("td=null?",""+(td==null));
			td.duration=(timeslotValues[position]);
		}
		else{
			//TODO launch custom time dialog
		}
			
	}

	@Override
	public void onNothingSelected (AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	private int getSpinnerId(){
		return usedSpinnerIDs++;
	}
	private int usedSpinnerIDs = 0;

	@Override
	public void onCreateMood(String moodname) {
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, moodname);
		mNewValues.put(DatabaseDefinitions.MoodColumns.STATE, HueUrlEncoder.encode(getMood()));
		
		getActivity().getContentResolver().insert(
				DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues);
	}
}
