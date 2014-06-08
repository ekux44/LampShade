package com.kuxhausen.huemore.editmood;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.kuxhausen.huemore.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class StateGridActionMode implements ActionMode.Callback {
    
	EditMoodStateGridFragment mFrag;
	private ImageButton mDiscardButton;
	private ViewType mViewType;
	
	public StateGridActionMode(EditMoodStateGridFragment editMoodStateGridFragment, ViewType viewType) {
		mFrag = editMoodStateGridFragment;
		mViewType = viewType;
	}

	@Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    	
		mDiscardButton = (ImageButton) LayoutInflater.from(mFrag.getActivity()).inflate(R.layout.discard_image_button, null);
		switch(mViewType){
			case StateCell:
				mDiscardButton.setOnDragListener(mFrag.mCellDragListener);
				break;
			case Channel:
				mDiscardButton.setOnDragListener(mFrag.mChannelDragListener);
				break;
			case Timeslot:
				mDiscardButton.setOnDragListener(mFrag.mTimeslotDragListener);
				break;
		
		}
		mode.setCustomView(mDiscardButton);
    	
    	return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        
    	mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

}
