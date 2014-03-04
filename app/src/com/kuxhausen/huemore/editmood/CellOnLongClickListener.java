package com.kuxhausen.huemore.editmood;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Pair;
import android.view.View;
import android.view.View.OnLongClickListener;

public class CellOnLongClickListener implements OnLongClickListener{
	private EditMoodStateGridFragment mFrag;
	
	public CellOnLongClickListener(EditMoodStateGridFragment editMoodStateGridFragment) {
		mFrag = editMoodStateGridFragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onLongClick(View v){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mFrag.contextSpot = (Pair<Integer, Integer>) v.getTag();
			// Instantiates the drag shadow builder.
		    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
	
		    // Starts the drag
		    v.startDrag(null,		 // the data to be dragged
                        myShadow,	// the drag shadow builder
                        null,		// no need to use local data
                        0			// flags (not currently used, set to 0)
            );
		}
		return true;
	}
}
