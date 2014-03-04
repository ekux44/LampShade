package com.kuxhausen.huemore.editmood;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.View.OnLongClickListener;

public class CellOnLongClickListener implements OnLongClickListener{
	
	private EditMoodStateGridFragment mFrag;
	private ViewType mViewType;
	
	public CellOnLongClickListener(EditMoodStateGridFragment editMoodStateGridFragment, ViewType viewType) {
		mFrag = editMoodStateGridFragment;
		mViewType = viewType;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onLongClick(View v){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			switch(mViewType){
			case StateCell :
				mFrag.mStateGrid.setStateSelectionByTag(v);
				// Instantiates the drag shadow builder.
			    View.DragShadowBuilder myCellShadow = new View.DragShadowBuilder(v);
			    // Starts the drag
			    v.startDrag(null, myCellShadow, mViewType, 0);
			    break;
			case Channel:
				break;
			case Timeslot:
				// Instantiates the drag shadow builder.
			    View.DragShadowBuilder myTimeslotShadow = new View.DragShadowBuilder(v);
			    // Starts the drag
			    v.startDrag(null, myTimeslotShadow, mViewType, 0);
				break;
			}
		    //enter action mode
		    mFrag.mActionMode = mFrag.getSherlockActivity().startActionMode(new StateGridActionMode(mFrag,mViewType));
		}
		return true;
	}
}
