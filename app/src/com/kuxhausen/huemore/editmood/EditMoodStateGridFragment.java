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
	int contextSpot;
	
	public ArrayList<StateCell> dataRay = new ArrayList<StateCell>();
	ArrayList<TimeslotStartTime> timedTimeslotDuration = new ArrayList<TimeslotStartTime>();
	ArrayList<TimeslotStartTime> dailyTimeslotDuration = new ArrayList<TimeslotStartTime>();
	private final static int defaultDuration = 10;
	
	ImageButton addChannel, addTimeslot;
	
	static String priorName;
	static Mood priorMood;
	
	public final static int SIMPLE_PAGE = 0, TIMED_PAGE=1, DAILY_PAGE = 2;
	
	private int pageType;
	
	public EditMoodActivity pager;
	
	public void setMoodMode(int spinnerPos){
		if(pageType!=spinnerPos){
			if(spinnerPos==SIMPLE_PAGE)
				setGridRows(1,0);
			
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
		timedTimeslotDuration.clear();
		dailyTimeslotDuration.clear();
		dataRay.clear();
		grid.setColumnCount(initialCols+1+endingCols);
		grid.setRowCount(initialRows+endingRows);
		
		addRow(defaultDuration);
		
	    redrawGrid();
	    
		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			
			//load prior mood
			priorName = args.getString(InternalArguments.MOOD_NAME);
			loadMood(Utils.getMoodFromDatabase(priorName, this.getActivity()));
		}
	    
	    return myView;
	}
	
	private StateCell generateDefaultMoodRow(){
		StateCell mr = new StateCell(this.getActivity());
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
		((NetworkManagedSherlockFragmentActivity)this.getActivity()).stopMood();
	}
	@Override
	public void preview(){
		((NetworkManagedSherlockFragmentActivity)this.getActivity()).startMood(getMood(), pager.getName());
		
	}
	
	public static int calculateMoodType(Mood m){
		if (m.timeAddressingRepeatPolicy==true){
			return DAILY_PAGE;
		}else if(!m.usesTiming && m.events.length == 1) {
			return SIMPLE_PAGE;
		}else
			return TIMED_PAGE;
	}

	
	private void loadMood(Mood mFromDB) {
		//calculate & set the mood type
		pageType = EditMoodStateGridFragment.calculateMoodType(mFromDB);
		
		//calculate & set the number of grid rows
		this.setGridCols(mFromDB.getNumChannels());
		
		//calculate & set number of rows, and fill with times
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
				if(pageType == DAILY_PAGE)
					dailyTimeslotDuration.get(row+1).setStartTime(e.time);
				else if(time!=-1)
					timedTimeslotDuration.get(row).setStartTime(e.time-time);
				
				row++;
				time = e.time;
			}
			dataRay.get(gridCols()*row + e.channel).hs = e.state;
		}
		
		//set loop button
		pager.setChecked(mFromDB.isInfiniteLooping());
		
		redrawGrid();
	}
	private Mood getMood() {		
		Mood m = new Mood();
		m.usesTiming = true; //TODO not always the case...
		m.setNumChannels(gridCols());
		if(pageType == SIMPLE_PAGE || pageType == DAILY_PAGE)
			m.timeAddressingRepeatPolicy=true;
		else
			m.timeAddressingRepeatPolicy = false;
		m.setInfiniteLooping(pager.isChecked());
		
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
		if(pageType == DAILY_PAGE){
			if(row<dailyTimeslotDuration.size())
				return dailyTimeslotDuration.get(row).getStartTime();
			else
				return 0;
		}
		else if (pageType == TIMED_PAGE){
			int time = 0;
			for(int i = row-1; i>=0; i--){
				time+=timedTimeslotDuration.get(i).getStartTime();
			}
			return time;
		}
		else
			return 0;
	}
	
	public int computeMinimumValue(int position){
		if(position <=0){
			return 0;
		} else{
			if(pageType == DAILY_PAGE)
				return (dailyTimeslotDuration.get(position-1)).getStartTime();
			else if(pageType == TIMED_PAGE)
				return (timedTimeslotDuration.get(position-1)).getStartTime();
			else
				return 0;
		}
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
		case R.id.rightButton:
			addCol();
			redrawGrid();
			break;
		case R.id.downButton:
			addRow(defaultDuration);
			redrawGrid();
			break;
		}
		
	}

	public void redrawGrid() {
		grid.removeAllViews();
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		for(int r = 0; r< gridRows(); r++)
			for(int c = 0; c<gridCols(); c++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(c+initialCols);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				View v = dataRay.get(r*gridCols()+c).getView((r*gridCols()+c), grid, this, this);
				v.setTag(r*this.gridCols()+c);
				grid.addView(v, vg);
			}
		//add timeslot button
		if(pageType == TIMED_PAGE || pageType == DAILY_PAGE){
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(0);
			vg.rowSpec = GridLayout.spec(initialRows+this.gridRows()+endingRows-1);
			vg.setGravity(Gravity.CENTER);
			grid.addView(addTimeslot, vg);
		}
		//add channel button
		{
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols+this.gridCols()+endingCols-1);
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(addChannel, vg);
		}
		
		//timedTimeslotDuration views
		if(pageType == TIMED_PAGE) {
			for(int r = 0; r<timedTimeslotDuration.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = timedTimeslotDuration.get(r).getView();
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				
				grid.addView(v, vg);
			}
		}
		//dailytimeslotDuration views
		if (pageType == DAILY_PAGE){
			for(int r = 0; r<dailyTimeslotDuration.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = dailyTimeslotDuration.get(r).getView();
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
			vg.columnSpec = GridLayout.spec(initialCols, this.gridCols());
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(b, vg);
		}
		//vertical separator
		if(pageType == TIMED_PAGE || pageType == DAILY_PAGE) {
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
		//horizontal separator
		{
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
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		contextSpot = (Integer)v.getTag();
		
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
		if(gridRows()>1){
			int row = (item / gridCols());//-1?
			ArrayList<StateCell> toRemove = new ArrayList<StateCell>();
			for(int i = 0; i<gridCols(); i++){
				toRemove.add(dataRay.get(i + row*gridCols()));
			}
			for(StateCell kill : toRemove)
				dataRay.remove(kill);
			
			timedTimeslotDuration.remove(row);
			dailyTimeslotDuration.remove(row);
			
			grid.setRowCount(initialRows+endingRows + gridRows()-1);
		}
		redrawGrid();
	}
	private void deleteCol(int item){
		if(gridCols()>1){
			int col = item % gridCols();
			ArrayList<StateCell> toRemove = new ArrayList<StateCell>();
			for(int i = 0; i<gridRows(); i++){
				toRemove.add(dataRay.get(col + i*gridCols()));
			}
			for(StateCell kill : toRemove)
				dataRay.remove(kill);
			grid.setColumnCount(endingCols+initialCols+gridCols()-1);
		}
		redrawGrid();
	}
	private void addRow(int duration){
		if(gridRows()<=64){
			grid.setRowCount(initialRows+endingRows + gridRows()+1);
			
			
			TimeslotStartTime tdTimed, tdDaily;
			tdDaily = new TimeOfDayTimeslot(this, getSpinnerId(), gridRows()-1);
			tdTimed = new OffsetTimeslot(this, getSpinnerId());
			tdTimed.setStartTime(duration);
			
			timedTimeslotDuration.add(tdTimed);
			dailyTimeslotDuration.add(tdDaily);
			
			for(int i = gridCols(); i>0; i--){
				addState();
			}
		}else{
			Toast t = Toast.makeText(getActivity(), R.string.advanced_timeslot_limit, Toast.LENGTH_LONG);
			t.show();
		}
	}
	private void addCol(){
		if(gridCols()<64){
			int width = gridCols();
			grid.setColumnCount(1+width+initialCols+endingCols);
			for(int i = dataRay.size(); i>0; i-=width){
				addState(i);
			}
		}else{
			Toast t = Toast.makeText(getActivity(), R.string.advanced_channel_limit, Toast.LENGTH_LONG);
			t.show();
		}
	}
	private final int initialRows = 2;
	private final int initialCols = 2;
	private final int endingRows = 1;
	private final int endingCols = 1;
	private final int gridRows(){
		return grid.getRowCount()-initialRows-endingCols;
	}
	private final int gridCols(){
		return grid.getColumnCount()-initialCols-endingCols;
	}
	private final void setGridRows(int num, int duration){
		while(gridRows()!=num){
			if(gridRows()<num)
				addRow(duration);
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
}
