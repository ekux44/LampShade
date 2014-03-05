package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.google.gson.Gson;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodActivity.OnCreateMoodListener;
import com.kuxhausen.huemore.editmood.StateGrid.StateGridDisplay;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
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

public class EditMoodStateGridFragment extends SherlockFragment implements OnClickListener, OnCreateMoodListener, StateGridDisplay {

	Gson gson = new Gson();
	private GridLayout grid;
	public ArrayList<StateRow> moodRows = new ArrayList<StateRow>();
	private RelativeStartTimeslot loopTimeslot;
	private ImageButton addChannel, addTimeslot;
	private String priorName;
	private PageType pageType = PageType.SIMPLE_PAGE;
	public EditMoodActivity mEditMoodActivity;
	private CellOnLongClickListener mCellLongListener = new CellOnLongClickListener(this, ViewType.StateCell);
	CellOnDragListener mCellDragListener = new CellOnDragListener(this, ViewType.StateCell);
	private CellOnLongClickListener mChannelLongListener = new CellOnLongClickListener(this, ViewType.Channel);
	CellOnDragListener mChannelDragListener = new CellOnDragListener(this, ViewType.Channel);
	private CellOnLongClickListener mTimeslotLongListener = new CellOnLongClickListener(this, ViewType.Timeslot);
	CellOnDragListener mTimeslotDragListener = new CellOnDragListener(this, ViewType.Timeslot);
	
	
	ActionMode mActionMode;
	StateGrid mStateGrid;
	
	public enum PageType {SIMPLE_PAGE, RELATIVE_PAGE, DAILY_PAGE};
	
	public void setMoodMode(int spinnerPos){
		if(pageType.ordinal()!=spinnerPos){
			if(spinnerPos==PageType.SIMPLE_PAGE.ordinal())
				setGridRows(1);
			
			pageType = PageType.values()[spinnerPos];
			redrawGrid();
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mEditMoodActivity = (EditMoodActivity) activity;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.edit_mood_state_grid_fragment, null);
		
		mStateGrid = new StateGrid(this);
		
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
		
		loopTimeslot = new RelativeStartTimeslot(this,0);
		
		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			
			//load prior mood
			priorName = args.getString(InternalArguments.MOOD_NAME);
			loadMood(Utils.getMoodFromDatabase(priorName, this.getActivity()));
		}
	    
		redrawGrid();
	    
	    return myView;
	}
	
	public int getGridWidth(){
		if(grid!=null)
			return grid.getWidth();
		return 0;
	}
	
	public void validate(){
		for(StateRow s : moodRows){
			s.dailyTimeslot.validate();
			s.relativeTimeslot.validate();
		}
		loopTimeslot.validate();
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
		String name = mEditMoodActivity.getName();
		if(name==null || name.length()<1){
			name = mEditMoodActivity.getString(R.string.hint_mood_name);
		}
		((NetworkManagedSherlockFragmentActivity)this.getActivity()).startMood(getMood(), name);
		
	}
	
	public static PageType calculateMoodType(Mood m){
		if(!m.usesTiming) {
			return PageType.SIMPLE_PAGE;
		} else if (m.timeAddressingRepeatPolicy==true){
			return PageType.DAILY_PAGE;
		} else
			return PageType.RELATIVE_PAGE;
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
				if(pageType == PageType.DAILY_PAGE || pageType == PageType.RELATIVE_PAGE){
					moodRows.get(row).dailyTimeslot.setStartTime(e.time);
					moodRows.get(row).relativeTimeslot.setStartTime(e.time);
				}
			}
			moodRows.get(row).cellRay.get(e.channel).hs = e.state;
		}
		
		//set loop button
		mEditMoodActivity.setChecked(mFromDB.isInfiniteLooping());
		
		loopTimeslot.setStartTime(mFromDB.loopIterationTimeLength);
		
		redrawGrid();
	}
	private Mood getMood() {		
		Mood m = new Mood();
		if(pageType == PageType.DAILY_PAGE || pageType == PageType.RELATIVE_PAGE)
			m.usesTiming = true;
		else
			m.usesTiming = false;
		m.setNumChannels(gridCols());
		if(pageType == PageType.SIMPLE_PAGE || pageType == PageType.DAILY_PAGE)
			m.timeAddressingRepeatPolicy=true;
		else
			m.timeAddressingRepeatPolicy = false;
		m.setInfiniteLooping(mEditMoodActivity.isChecked());
		
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
			if(pageType == PageType.DAILY_PAGE)
					return moodRows.get(row).dailyTimeslot.getStartTime();
			else if(pageType == PageType.RELATIVE_PAGE)
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
			if(pageType == PageType.DAILY_PAGE)
				return moodRows.get(position-1).dailyTimeslot.getStartTime()+600;
			else if (pageType==PageType.RELATIVE_PAGE)
				return moodRows.get(position-1).relativeTimeslot.getStartTime()+10;
		}
		return 0;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.clickable_layout:
			stopPreview();
			mStateGrid.setStateSelectionByTag(v);
			EditStatePagerDialogFragment cpdf = new EditStatePagerDialogFragment();
			cpdf.setParrentMood(this);
			Bundle args = new Bundle();
			args.putString(InternalArguments.PREVIOUS_STATE, gson.toJson(this.getCell(mStateGrid.getSelectedCellRowCol()).hs));
			args.putInt(InternalArguments.ROW, mStateGrid.getSelectedCellRow());
			args.putInt(InternalArguments.COLUMN, mStateGrid.getSelectedCellCol());
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

	public void switchCells(Pair<Integer, Integer> first, Pair<Integer, Integer> second){
		StateCell temp = moodRows.get(first.first).cellRay.get(first.second);
		moodRows.get(first.first).cellRay.set(first.second, moodRows.get(second.first).cellRay.get(second.second));
		moodRows.get(second.first).cellRay.set(second.second, temp);
		redrawGrid();
	}

	public void switchTimeslots(int position1, int position2) {
		StateRow temp = moodRows.get(position1);
		moodRows.set(position1, moodRows.get(position2));
		moodRows.set(position2, temp);
		redrawGrid();
	}
	public void deleteTimeslot(int position){
		moodRows.remove(position);
		redrawGrid();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void redrawGrid() {
		if(grid==null)
			return;
		grid.removeAllViews();
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		for(int r = 0; r< moodRows.size(); r++)
			for(int c = 0; c<moodRows.get(r).cellRay.size(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c+initialCols);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				View v = moodRows.get(r).cellRay.get(c).getView(grid, this, this,(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)?mCellLongListener:null);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					v.setOnDragListener(mCellDragListener);
				mStateGrid.tagStateCell(v, r, c);
				grid.addView(v, vg);
			}
		int gridStateRows = this.gridRows();
		int gridStateCols = this.gridCols();
		
		//add timeslot button
		if(pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE){
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
		if(pageType == PageType.RELATIVE_PAGE) {
			for(int r = 0; r<moodRows.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = moodRows.get(r).relativeTimeslot.getView(r);
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					v.setOnLongClickListener(mTimeslotLongListener);
					v.setOnDragListener(mTimeslotDragListener);
				}
				mStateGrid.tagTimeslot(v, r);
				grid.addView(v, vg);
			}
		}
		//dailytimeslotDuration views
		if (pageType == PageType.DAILY_PAGE){
			for(int r = 0; r<moodRows.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = moodRows.get(r).dailyTimeslot.getView(r);
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					v.setOnLongClickListener(mTimeslotLongListener);
					v.setOnDragListener(mTimeslotDragListener);
				}
				mStateGrid.tagTimeslot(v, r);
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
					
					((NetworkManagedSherlockFragmentActivity)mEditMoodActivity).startMood(showChanM, null);
				}
				
			});
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(b, vg);
		}
		//timeslot label
		if(pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE) {
			View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(v, vg);
		}
		//vertical separator
		if(pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE) {
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
		//loop related stuff
		if(pageType == PageType.RELATIVE_PAGE && mEditMoodActivity.isChecked()){
			//loop banner that sits beside loop timeslot
			
			{
				View v =inflater.inflate(R.layout.grid_timeslot_loop_label, null);
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
				vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows);
				vg.setGravity(Gravity.CENTER);
				grid.addView(v, vg);
			}
	
			//loop timeslot view
			{
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = loopTimeslot.getView(moodRows.size());
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				
				grid.addView(v, vg);
			}
			//vertical separator extended down to loop
			{
				ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_vertical_seperator, null);
	
				ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
				rowView.setImageDrawable(cd);
				rowView.setMinimumWidth(1);
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(1);
				vg.rowSpec = GridLayout.spec(initialRows+gridStateRows+endingRows);
				vg.setGravity(Gravity.FILL_VERTICAL);
	
				grid.addView(rowView, vg);		
			}
		}
		
		grid.invalidate();
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		mStateGrid.setStateSelectionByTag(v);
		
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
		
		android.view.MenuItem deleteTimeslot = menu.findItem(R.id.contextstatemenu_delete_timeslot);
		if(pageType == PageType.SIMPLE_PAGE){
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
				args.putString(InternalArguments.PREVIOUS_STATE,gson.toJson(getCell(mStateGrid.getSelectedCellRowCol()).hs));
				args.putInt(InternalArguments.ROW, mStateGrid.getSelectedCellRow());
				args.putInt(InternalArguments.COLUMN, mStateGrid.getSelectedCellCol());
				cpdf.setArguments(args);
				cpdf.show(getFragmentManager(),
						InternalArguments.FRAG_MANAGER_DIALOG_TAG);
				return true;
			case R.id.contextstatemenu_delete:
				deleteCell(mStateGrid.getSelectedCellRowCol());
				return true;
			case R.id.contextstatemenu_delete_timeslot:
				deleteRow(mStateGrid.getSelectedCellRow());
				redrawGrid();
				return true;
			case R.id.contextstatemenu_delete_channel:
				deleteCol(mStateGrid.getSelectedCellCol());
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
	
	public void deleteCell(Pair<Integer, Integer> tag){
		getCell(tag).hs = new BulbState();
		redrawGrid();
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
				newRow.cellRay.add(new StateCell(this.getActivity()));
			}
			newRow.dailyTimeslot = new TimeOfDayTimeslot(this, gridRows()-1);
			newRow.relativeTimeslot = new RelativeStartTimeslot(this, gridRows()-1);
			
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
				sr.cellRay.add(new StateCell(this.getActivity()));
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
	
	@Override
	public void onCreateMood(String moodname) {
		ContentValues mNewValues = new ContentValues();
		mNewValues.put(DatabaseDefinitions.MoodColumns.MOOD, moodname);
		mNewValues.put(DatabaseDefinitions.MoodColumns.STATE, HueUrlEncoder.encode(getMood()));
		
		getActivity().getContentResolver().insert(
				DatabaseDefinitions.MoodColumns.MOODS_URI, mNewValues);
	}

	@Override
	public PageType getPageType() {
		return pageType;
	}
}
