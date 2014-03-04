package com.kuxhausen.huemore.editmood;

import com.kuxhausen.huemore.R;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.util.Pair;
import android.view.DragEvent;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class CellOnDragListener implements View.OnDragListener {
	
	private EditMoodStateGridFragment mFrag;
	private ViewType mViewType;
	
	public CellOnDragListener(EditMoodStateGridFragment editMoodStateGridFragment, ViewType viewType) {
		mFrag = editMoodStateGridFragment;
		mViewType = viewType;
	}
	
    // This is the method that the system calls when it dispatches a drag event to the listener.
    public boolean onDrag(View v, DragEvent event) {

        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();

        // Handles each of the expected events
        switch(action) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data
                if(((ViewType)event.getLocalState()).equals(mViewType))
                	return true;
                return false;
            case DragEvent.ACTION_DRAG_ENTERED: 
                // Applies a background around the View. The return value is ignored.
                v.setBackgroundColor(mFrag.getResources().getColor(R.color.blue_color));
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
            	// Ignore the event
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                //Re-sets the color tint. The return value is ignored.
                v.setBackgroundColor(Color.TRANSPARENT);
                return true;
            case DragEvent.ACTION_DROP:
                v.setBackgroundColor(Color.TRANSPARENT);
            	switch(v.getId()){
	            	case R.id.discardImageButton:
	            		mFrag.deleteCell(mFrag.mStateGrid.getSelectedCellRowCol());
	            		mFrag.mActionMode.finish();
	            		return true;
            	}
                //todo move into a switch case one all state cells have same id
                Pair<Integer, Integer> cellInDrag=mFrag.mStateGrid.getSelectedCellRowCol();
            	Pair<Integer, Integer> cellRecievingDrop = (Pair<Integer, Integer>) v.getTag();
            	mFrag.switchCells(cellInDrag, cellRecievingDrop);
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                // Turns off any color tinting. the return value is ignored.
            	v.setBackgroundColor(Color.TRANSPARENT);
            	if(mFrag.mActionMode!=null)
            		mFrag.mActionMode.finish();
                return true;
        };
		return false;
    };
}
