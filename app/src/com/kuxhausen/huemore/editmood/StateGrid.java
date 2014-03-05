package com.kuxhausen.huemore.editmood;

import com.kuxhausen.huemore.editmood.EditMoodStateGridFragment.PageType;

import android.util.Pair;
import android.view.View;

public class StateGrid {

	public StateGridDisplay mStateGridDisplay;
	private Pair<Integer, Integer> mSelectedCellRowCol;
	private int mSelectedTimeslot;
	private int mSelectedChannel;
	
	
	public StateGrid(StateGridDisplay stateGridDisplay) {
		mStateGridDisplay = stateGridDisplay;
	}

	interface StateGridDisplay {
		public abstract PageType getPageType();
		public abstract void redrawGrid();
	}
	
	public void tagStateCell(View v, int r, int c){
		v.setTag(new Pair<Integer,Integer>(r, c));
	}
	public Pair<Integer, Integer> getSelectedCellRowCol(){
		return mSelectedCellRowCol;
	}
	public void setStateSelectionByTag(View v){
		mSelectedCellRowCol = (Pair<Integer, Integer>) v.getTag();
	}
	public int getSelectedCellRow(){
		if(mSelectedCellRowCol!=null)
			return mSelectedCellRowCol.first;
		return 0;
	}
	public int getSelectedCellCol(){
		if(mSelectedCellRowCol!=null)
			return mSelectedCellRowCol.second;
		return 0;
	}
	
	
	public void tagTimeslot(View v, int r){
		v.setTag(r);
	}
	public void setTimeslotSelectionByTag(View v){
		mSelectedTimeslot = (Integer) v.getTag();
	}
	public int getSelectedTimeslotRow(){
		return mSelectedTimeslot;
	}
}
