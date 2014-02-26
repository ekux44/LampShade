package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodActivity.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.GridLayout;

public class EditMoodStateGridFragment extends SherlockFragment implements OnClickListener, OnCreateMoodListener {

	Gson gson = new Gson();
	GridLayout grid;
	Pair<Integer, Integer> contextSpot;
	
	public ArrayList<StateRow> moodRows = new ArrayList<StateRow>();
	
	RelativeStartTimeslot loopTimeslot;
	
	ImageButton addChannel, addTimeslot;
	
	static String priorName;
	static Mood priorMood;
	
	public final static int SIMPLE_PAGE = 0, RELATIVE_START_TIME_PAGE=1, DAILY_PAGE = 2;
	
	private int pageType;
	
	public EditMoodActivity pager;
	
	public void setMoodMode(int spinnerPos){
		if(pageType!=spinnerPos){
			if(spinnerPos==SIMPLE_PAGE)
				setGridRows(1);
			
			pageType = spinnerPos;
			redrawGrid();
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        pager = (EditMoodActivity) activity;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.edit_mood_state_grid_fragment, null);
		
		addTimeslot = (ImageButton) inflater.inflate(R.layout.edit_mood_down_arrow, null);
		addTimeslot.setOnClickListener(this);
		
		addChannel = (ImageButton) inflater.inflate(R.layout.edit_mood_right_arrow, null);
		addChannel.setOnClickListener(this);
		
		grid = (GridLayout) myView.findViewById(R.id.advancedGridLayout);
		grid.removeAllViews();
		moodRows.clear();
		grid.setColumnCount(initialCols+1+endingCols);
		grid.setRowCount(initialRows+endingRows);
		
		addRow();
		
	    
		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			
			//load prior mood
			priorName = args.getString(InternalArguments.MOOD_NAME);
			loadMood(Utils.getMoodFromDatabase(priorName, this.getActivity()));
		}
	    
		loopTimeslot = new RelativeStartTimeslot(this,getSpinnerId(),0);
		redrawGrid();
	    
	    return myView;
	}
	
	private StateCell generateDefaultStateCell(){
		StateCell mr = new StateCell(this.getActivity());
		BulbState example = new BulbState();
		mr.hs = example;
		return mr;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getCell(new Pair<Integer, Integer>(data.getIntExtra(InternalArguments.ROW,-1)
				,data.getIntExtra(InternalArguments.COLUMN,-1))).hs = gson.fromJson(
						data.getStringExtra(InternalArguments.HUE_STATE),
						BulbState.class);

		redrawGrid();
	}
	
	private void stopPreview(){
		((NetworkManagedSherlockFragmentActivity)this.getActivity()).stopMood();
	}
	@Override
	public void preview(){
		((NetworkManagedSherlockFragmentActivity)this.getActivity()).startMood(getMood(), pager.getName());
		
	}
	
	public static int calculateMoodType(Mood m){
		if (m.timeAddressingRepeatPolicy==true){
			return DAILY_PAGE;
		}else if(!m.usesTiming) {
			return SIMPLE_PAGE;
		}else
			return RELATIVE_START_TIME_PAGE;
	}

	
	private void loadMood(Mood mFromDB) {
		//calculate & set the mood type
		pageType = EditMoodStateGridFragment.calculateMoodType(mFromDB);
		
		//calculate & set the number of grid rows
		this.setGridCols(mFromDB.getNumChannels());
		
		//calculate & set number of rows, and fill with times
		int rows = 0;
		int time = -1;
		for(Event e: mFromDB.events){
			if(e.time!=time){
				rows++;
				time=e.time;
			}
		}
		setGridRows(rows);
		
		int row = -1;
		time = -1;
		for(Event e: mFromDB.events){
			if(e.time!=time){
				row++;
				time = e.time;
				if(pageType == DAILY_PAGE || pageType == RELATIVE_START_TIME_PAGE){
					moodRows.get(row).dailyTimeslot.setStartTime(e.time);
					moodRows.get(row).relativeTimeslot.setStartTime(e.time);
				}
			}
			moodRows.get(row).cellRay.get(e.channel).hs = e.state;
		}
		
		//set loop button
		pager.setChecked(mFromDB.isInfiniteLooping());
		
		loopTimeslot.setStartTime(mFromDB.loopIterationTimeLength);
		
		redrawGrid();
	}
	private Mood getMood() {		
		Mood m = new Mood();
		if(pageType == DAILY_PAGE || pageType == RELATIVE_START_TIME_PAGE)
			m.usesTiming = true;
		else
			m.usesTiming = false;
		m.setNumChannels(gridCols());
		if(pageType == SIMPLE_PAGE || pageType == DAILY_PAGE)
			m.timeAddressingRepeatPolicy=true;
		else
			m.timeAddressingRepeatPolicy = false;
		m.setInfiniteLooping(pager.isChecked());
		
		ArrayList<Event> events = new ArrayList<Event>();
		for(int r = 0; r<moodRows.size(); r++){
			for(int c = 0; c< moodRows.get(r).cellRay.size(); c++){
				StateCell mr = moodRows.get(r).cellRay.get(c);
				if(mr.hs!=null && !mr.hs.toString().equals("")){
					Event e = new Event();
					e.channel = c;
					e.time = getTime(r);
					e.state = mr.hs;
					events.add(e);
				}
			}
		}
		Event[] eRay = new Event[events.size()];
		for(int i = 0; i<eRay.length; i++)
			eRay[i] = events.get(i);
		
		m.events = eRay;
		m.loopIterationTimeLength = loopTimeslot.getStartTime();
		return m;
	}
	private int getTime(int row){
		if(row>-1 && row<moodRows.size()){
			if(pageType == DAILY_PAGE)
					return moodRows.get(row).dailyTimeslot.getStartTime();
			else if(pageType == RELATIVE_START_TIME_PAGE)
					return moodRows.get(row).relativeTimeslot.getStartTime();
		}
		return 0;
	}
	/** compute Minimum Value at my position**/
	public int computeMinimumValue(int position){
		position = Math.min(position, moodRows.size());
		if(position <=0){
			return 0;
		} else{
			if(pageType == DAILY_PAGE)
				return moodRows.get(position-1).dailyTimeslot.getStartTime()+600;
			else if (pageType==RELATIVE_START_TIME_PAGE)
				return moodRows.get(position-1).relativeTimeslot.getStartTime()+10;
		}
		return 0;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.clickable_layout:
			stopPreview();
			contextSpot = (Pair<Integer, Integer>) v.getTag();
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			cpdf.setParrentMood(this);
			Bundle args = new Bundle();
			args.putString(InternalArguments.PREVIOUS_STATE, gson.toJson(this.getCell(contextSpot).hs));
			args.putInt(InternalArguments.ROW, contextSpot.first);
			args.putInt(InternalArguments.COLUMN, contextSpot.second);
			cpdf.setArguments(args);
			cpdf.setTargetFragment(this, -1);
			cpdf.show(getFragmentManager(),
					InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			break;
		case R.id.rightButton:
			addCol();
			redrawGrid();
			break;
		case R.id.downButton:
			addRow();
			redrawGrid();
			break;
		}
		
	}

	public void redrawGrid() {
		grid.removeAllViews();
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		for(int r = 0; r< moodRows.size(); r++)
			for(int c = 0; c<moodRows.get(r).cellRay.size(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c+initialCols);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				View v = moodRows.get(r).cellRay.get(c).getView(grid, this, this);
				v.setTag(this.generateTag(r, c));
				grid.addView(v, vg);
			}
		int gridStateRows = this.gridRows();
		int gridStateCols = this.gridCols();
		
		//add timeslot button
		if(pageType == RELATIVE_START_TIME_PAGE || pageType == DAILY_PAGE){
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows-1);
			vg.setGravity(Gravity.CENTER);
			grid.addView(addTimeslot, vg);
		}
		//add channel button
		{
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols+gridStateCols+endingCols-1);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(addChannel, vg);
		}
		
		//timedTimeslotDuration views
		if(pageType == RELATIVE_START_TIME_PAGE) {
			for(int r = 0; r<moodRows.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = moodRows.get(r).relativeTimeslot.getView(r);
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				
				grid.addView(v, vg);
			}
		}
		//dailytimeslotDuration views
		if (pageType == DAILY_PAGE){
			for(int r = 0; r<moodRows.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = moodRows.get(r).dailyTimeslot.getView(r);
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				
				grid.addView(v, vg);
			}
		}
		
		//channel label
		{
			Button b =(Button) inflater.inflate(R.layout.grid_col_channels_label, null);
			b.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Mood showChanM = new Mood();
					showChanM.usesTiming = true;
					showChanM.setNumChannels(gridCols());
					
					//flash each channel 1.5 seconds apart
					Event[] eRay = new Event[showChanM.getNumChannels()];
					for(int i = 0; i< showChanM.getNumChannels(); i++){
						BulbState bs = new BulbState();
						bs.alert = "select";
						bs.on = true;
						
						Event e = new Event();
						e.channel = i;
						e.time = 15*i;
						e.state = bs;
						eRay[i]=e;
					}
					showChanM.events = eRay;
					showChanM.loopIterationTimeLength = 15*showChanM.getNumChannels();
					
					((NetworkManagedSherlockFragmentActivity)pager).startMood(showChanM, null);
				}
				
			});
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(b, vg);
		}
		//timeslot label
		if(pageType == RELATIVE_START_TIME_PAGE || pageType == DAILY_PAGE) {
			View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(v, vg);
		}
		//vertical separator
		if(pageType == RELATIVE_START_TIME_PAGE || pageType == DAILY_PAGE) {
			ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_vertical_seperator, null);

			ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
			rowView.setImageDrawable(cd);
			rowView.setMinimumWidth(1);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(1);
			vg.rowSpec = GridLayout.spec(0, initialRows+gridStateRows);
			vg.setGravity(Gravity.FILL_VERTICAL);

			grid.addView(rowView, vg);		
		}
		//horizontal separator
		{
			ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_horizontal_seperator, null);
			
			ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
			rowView.setImageDrawable(cd);
			rowView.setMinimumHeight(1);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0,initialCols+gridStateCols);
			vg.rowSpec = GridLayout.spec(1);
			vg.setGravity(Gravity.FILL_HORIZONTAL);
			
			grid.addView(rowView, vg);
		}
		//loop banner that sits beside loop timeslot
		if(pageType == RELATIVE_START_TIME_PAGE && pager.isChecked()){
			View v =inflater.inflate(R.layout.grid_timeslot_loop_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
			vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows);
			vg.setGravity(Gravity.CENTER);
			grid.addView(v, vg);
		}

		//loop timeslot view
		if(pageType == RELATIVE_START_TIME_PAGE && pager.isChecked()){
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows);
			vg.setGravity(Gravity.CENTER);
			
			View v = loopTimeslot.getView(moodRows.size());
			if(v.getParent()!=null)
				((ViewGroup)v.getParent()).removeView(v);
			
			grid.addView(v, vg);
		}
		
		
		grid.invalidate();
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		contextSpot = (Pair<Integer, Integer>) v.getTag();
		
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
		
		android.view.MenuItem deleteTimeslot = menu.findItem(R.id.contextstatemenu_delete_timeslot);
		if(pageType == SIMPLE_PAGE){
			deleteTimeslot.setEnabled(false);
			deleteTimeslot.setVisible(false);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.contextstatemenu_edit:
				stopPreview();
				EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
				cpdf.setParrentMood(this);
				Bundle args = new Bundle();
				args.putString(InternalArguments.PREVIOUS_STATE,gson.toJson(getCell(contextSpot).hs));
				args.putInt(InternalArguments.ROW, contextSpot.first);
				args.putInt(InternalArguments.COLUMN, contextSpot.second);
				cpdf.setArguments(args);
				cpdf.show(getFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			case R.id.contextstatemenu_delete:
				getCell(contextSpot).hs = new BulbState();
				redrawGrid();
				return true;
			case R.id.contextstatemenu_delete_timeslot:
				deleteRow(contextSpot.first);
				redrawGrid();
				return true;
			case R.id.contextstatemenu_delete_channel:
				deleteCol(contextSpot.second);
				redrawGrid();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	public StateCell getCell(Pair<Integer, Integer> tag){
		int r = tag.first;
		int c = tag.second;
		return moodRows.get(r).cellRay.get(c);
	}
	public Pair<Integer,Integer> generateTag(int r, int c){
		return new Pair<Integer,Integer>(r, c);
	}
	
	private void deleteRow(int row){
		if(row>-1 && row < moodRows.size()){
			moodRows.remove(row);
			grid.setRowCount(initialRows+endingRows + gridRows()-1);
			redrawGrid();
		}
	}
	private void deleteCol(int col){
		if(col>-1 && !moodRows.isEmpty() && col < moodRows.get(0).cellRay.size()){
			for(StateRow sr: moodRows){
				sr.cellRay.remove(col);
			}
			grid.setColumnCount(endingCols+initialCols+gridCols()-1);
		}
		redrawGrid();
	}
	private void addRow(){
		if(gridRows()<=64){
			grid.setRowCount(initialRows+endingRows + gridRows()+1);
			
			StateRow newRow = new StateRow();
			for(int i = gridCols(); i>0; i--){
				newRow.cellRay.add(generateDefaultStateCell());
			}
			newRow.dailyTimeslot = new TimeOfDayTimeslot(this, getSpinnerId(), gridRows()-1);
			newRow.relativeTimeslot = new RelativeStartTimeslot(this, getSpinnerId(), gridRows()-1);
			
			moodRows.add(newRow);
		}else{
			Toast t = Toast.makeText(getActivity(), R.string.advanced_timeslot_limit, Toast.LENGTH_LONG);
			t.show();
		}
	}
	private void addCol(){
		if(gridCols()<64){
			int width = gridCols();
			grid.setColumnCount(1+width+initialCols+endingCols);
			for(StateRow sr : moodRows){
				sr.cellRay.add(generateDefaultStateCell());
			}
		}else{
			Toast t = Toast.makeText(getActivity(), R.string.advanced_channel_limit, Toast.LENGTH_LONG);
			t.show();
		}
	}
	private final int initialRows = 2;
	private final int initialCols = 2;
	private final int endingRows = 2;
	private final int endingCols = 1;
	
	private final int gridRows(){
		return grid.getRowCount()-initialRows-endingRows;
	}
	private final int gridCols(){
		return grid.getColumnCount()-initialCols-endingCols;
	}
	private final void setGridRows(int num){
		while(gridRows()!=num){
			if(gridRows()<num)
				addRow();
			else if(gridRows()>num)
				deleteRow(gridRows()-1);
		}
	}
	private final void setGridCols(int num){
		while(gridCols()!=num){
			if(gridCols()<num)
				addCol();
			else if(gridCols()>num)
				deleteCol(gridCols()-1);
		}
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
	
	public class StateRow{
		ArrayList<StateCell> cellRay = new ArrayList<StateCell>();
		TimeOfDayTimeslot dailyTimeslot;
		RelativeStartTimeslot relativeTimeslot;
	}
}
