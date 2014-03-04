package com.kuxhausen.huemore.editmood;

import android.util.Pair;
import android.view.View;

public class StateGrid {

	public StateGridDisplay mStateGridDisplay;
	private Pair<Integer, Integer> mSelectedCellRowCol;
	
	public StateGrid(StateGridDisplay stateGridDisplay) {
		mStateGridDisplay = stateGridDisplay;
	}

	interface StateGridDisplay {
		public abstract int getPageType();
		public abstract void redrawGrid();
	}
	public void setSelectionByTag(View v){
		mSelectedCellRowCol =(Pair<Integer, Integer>) v.getTag();
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
	public Pair<Integer, Integer> getSelectedCellRowCol(){
		return mSelectedCellRowCol;
	}
}
