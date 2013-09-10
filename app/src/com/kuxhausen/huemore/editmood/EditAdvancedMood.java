package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.menu;
import com.kuxhausen.huemore.state.api.BulbState;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.support.v7.widget.GridLayout.Spec;

public class EditAdvancedMood extends Activity implements OnItemClickListener {

	GridLayout grid;
	
	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_advanced_mood);
		
		grid = (GridLayout) this.findViewById(R.id.advancedGridLayout);
		grid.setColumnCount(2);
		grid.setRowCount(2);
		
		Log.e("colrow",grid.getColumnCount()+" "+grid.getRowCount());
		
		moodRowArray = new ArrayList<MoodRow>();
		rayAdapter = new MoodRowAdapter(this, moodRowArray);
	
		addState();
	    addState();
	    addState();
	    addState();
	}
	
	private void addState() {
		MoodRow mr = new MoodRow();
		mr.color = 0xff000000;
		BulbState example = new BulbState();
		example.on = false;
		mr.hs = example;
		moodRowArray.add(mr);
		rayAdapter.add(mr);
		
		populateGrid(rayAdapter.getCount()-1);
	}
	
	private void populateGrid(int index){
		GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
		vg.columnSpec = GridLayout.spec(index % grid.getColumnCount());
		vg.rowSpec = GridLayout.spec(index / grid.getRowCount());
		
		grid.addView(rayAdapter.getView(index, null, grid), vg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_advanced_mood, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
	}

}
