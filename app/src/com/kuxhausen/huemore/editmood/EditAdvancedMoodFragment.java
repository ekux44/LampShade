package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Toast;
import android.support.v7.widget.GridLayout;

public class EditAdvancedMoodFragment extends SherlockFragment implements OnClickListener, OnCreateMoodListener {

	Gson gson = new Gson();
	GridLayout grid;
	int contextSpot;
	
	public ArrayList<StateCell> dataRay = new ArrayList<StateCell>();
	ArrayList<TimeslotDuration> timeslotDuration = new ArrayList<TimeslotDuration>();
	private final static int defaultDuration = 10;
	
	Button addChannel, addTimeslot;
	
	static String priorName;
	static Mood priorMood;
	
	private int pageType;
	private boolean dailyMode;
	
	public EditMoodPagerDialogFragment pager;
	
	public void setTimedMode(EditMoodPagerDialogFragment p){
		pageType = EditMoodPagerDialogFragment.TIMED_PAGE;
		pager = p;
	}
	public void setDailyMode(EditMoodPagerDialogFragment p){
		pageType = EditMoodPagerDialogFragment.DAILY_PAGE;
		pager = p;
		dailyMode = true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View myView = inflater.inflate(R.layout.edit_advanced_mood, null);
		
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
		dataRay.clear();
		grid.setColumnCount(initialCols+1);
		grid.setRowCount(initialRows);
		
		addRow(defaultDuration);
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
	void preview(){
		if(pageType == EditMoodPagerDialogFragment.currentPage && grid!=null)
			((NetworkManagedSherlockFragmentActivity)this.getActivity()).startMood(getMood(), pager.getName());
		
	}
	
	private void loadMood(Mood mFromDB) {
		this.setGridCols(mFromDB.getNumChannels());
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
				if(pageType == EditMoodPagerDialogFragment.DAILY_PAGE)
					timeslotDuration.get(row+1).setDuration(e.time);
				else if(time!=-1)
					timeslotDuration.get(row).setDuration(e.time-time);
				
				row++;
				time = e.time;
			}
			dataRay.get(gridCols()*row + e.channel).hs = e.state;
		}
		
		pager.setChecked(mFromDB.isInfiniteLooping());
		redrawGrid();
	}
	private Mood getMood() {		
		Mood m = new Mood();
		m.usesTiming = true; //TODO not always the case...
		m.setNumChannels(gridCols());
		if(pageType == EditMoodPagerDialogFragment.DAILY_PAGE)
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
		if(pageType == EditMoodPagerDialogFragment.DAILY_PAGE){
			if(row<timeslotDuration.size())
				return timeslotDuration.get(row).getDuration();
			else
				return 0;
		}
		else {
			int time = 0;
			for(int i = row-1; i>=0; i--){
				time+=timeslotDuration.get(i).getDuration();
			}
			return time;
		}
	}
	
	public Calendar computeMinimumValue(int position){
		if(position <=0){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c;
		} else{
			return ((TimeOfDayTimeslot)timeslotDuration.get(position-1)).getCal();
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
		case R.id.addChannelButton:
			addCol();
			redrawGrid();
			break;
		case R.id.addTimeslotButton:
			addRow(defaultDuration);
			redrawGrid();
			break;
		}
		
	}

	public void redrawGrid() {
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
		{
			for(int r = 0; r<timeslotDuration.size(); r++){
				GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
				vg.columnSpec = GridLayout.spec(0);
				vg.rowSpec = GridLayout.spec(r+initialRows);
				vg.setGravity(Gravity.CENTER);
				
				View v = timeslotDuration.get(r).getView();
				if(v.getParent()!=null)
					((ViewGroup)v.getParent()).removeView(v);
				grid.addView(v, vg);
				
				
			}
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
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
					
					if(pageType == EditMoodPagerDialogFragment.currentPage && grid!=null)
						((NetworkManagedSherlockFragmentActivity)pager).startMood(showChanM, null);
				}
				
			});
			GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
			vg.columnSpec = GridLayout.spec(initialCols, this.gridCols());
			vg.rowSpec = GridLayout.spec(0);
			vg.setGravity(Gravity.CENTER);
			grid.addView(b, vg);
		}
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v =inflater.inflate(R.layout.grid_col_timeslot_label, null);
			if(pageType==EditMoodPagerDialogFragment.DAILY_PAGE)
				((TextView)v.findViewById(R.id.textLabel)).setText(R.string.daily_start_time);
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

		contextSpot = (Integer)v.getTag();
		
		if (dailyMode){
			android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_daily_state, menu);
		} else{
		android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_state, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if(dailyMode){
			switch (item.getItemId()) {
			case R.id.contextdailymenu_edit:
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
			case R.id.contextdailymenu_delete:
				delete(contextSpot);
				redrawGrid();
				return true;
			case R.id.contextdailymenu_delete_timeslot:
				deleteRow(contextSpot);
				redrawGrid();
				return true;
			case R.id.contextdailymenu_delete_channel:
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
		if(gridRows()>1){
			int row = (item / gridCols());//-1?
			ArrayList<StateCell> toRemove = new ArrayList<StateCell>();
			for(int i = 0; i<gridCols(); i++){
				toRemove.add(dataRay.get(i + row*gridCols()));
			}
			for(StateCell kill : toRemove)
				dataRay.remove(kill);
			
			timeslotDuration.remove(row);
			
			grid.setRowCount(initialRows + gridRows()-1);
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
			grid.setColumnCount(initialCols+gridCols()-1);
		}
		redrawGrid();
	}
	private void addRow(int duration){
		if(gridRows()<=64){
			grid.setRowCount(initialRows + gridRows()+1);
			
			
			TimeslotDuration td;
			if(pageType == EditMoodPagerDialogFragment.DAILY_PAGE){
				td = new TimeOfDayTimeslot(this, getSpinnerId(), gridRows()-1);
			}
			else{
				td = new OffsetTimeslot(this, getSpinnerId());
			}
			td.setDuration(duration);
			timeslotDuration.add(td);
			
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
			grid.setColumnCount(1+width+initialCols);
			for(int i = dataRay.size(); i>0; i-=width){
				addState(i);
			}
		}else{
			Toast t = Toast.makeText(getActivity(), R.string.advanced_channel_limit, Toast.LENGTH_LONG);
			t.show();
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
