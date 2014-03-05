package com.kuxhausen.huemore.editmood;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
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
				mFrag.mStateGrid.setTimeslotSelectionByTag(v);
				// Instantiates the drag shadow builder.
			    View.DragShadowBuilder myTimeslotShadow = new TimeslotShadowBuilder(v,mFrag.getGridWidth(), mFrag.getActivity());
			    // Starts the drag
			    v.startDrag(null, myTimeslotShadow, mViewType, 0);
				break;
			}
		    //enter action mode
		    mFrag.mActionMode = mFrag.getSherlockActivity().startActionMode(new StateGridActionMode(mFrag,mViewType));
		}
		return true;
	}
	
	
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class TimeslotShadowBuilder extends View.DragShadowBuilder {

		    // The drag shadow image, defined as a drawable thing
		    private static Drawable shadow;
		    private static ShapeDrawable outline;
		    private static int mWidth;
		    
		        // Defines the constructor for myDragShadowBuilder
		        public TimeslotShadowBuilder(View v, int pxWidth, Context c) {

		            // Stores the View parameter passed to myDragShadowBuilder.
		            super(v);

		            // Creates a draggable image that will fill the Canvas provided by the system.
		            shadow = new ColorDrawable(Color.LTGRAY);
		            
		            
		            RectShape outlineShape = new RectShape();
		            outline = new ShapeDrawable(outlineShape);
		            outline.getPaint().setColor(Color.LTGRAY);
		            outline.getPaint().setStyle(Style.STROKE);
		            outline.getPaint().setStrokeWidth(4 *  c.getResources().getDisplayMetrics().density);
		            
		            mWidth = pxWidth;
		        }

		        // Defines a callback that sends the drag shadow dimensions and touch point back to the
		        // system.
		        @Override
		        public void onProvideShadowMetrics (Point size, Point touch){
		            // Defines local variables
		            int width, height;

		            // Sets the width of the shadow to half the width of the original View
		            width = getView().getWidth();// / 2;

		            // Sets the height of the shadow to half the height of the original View
		            height = getView().getHeight();// / 2;

		            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
		            // Canvas that the system will provide. As a result, the drag shadow will fill the
		            // Canvas.
		            shadow.setBounds(width, 0, width+mWidth, height);
		            outline.setBounds(0, 0, width, height);
		            // Sets the size parameter's width and height values. These get back to the system
		            // through the size parameter.
		            size.set(width+mWidth, height);
 
		            // Sets the touch point's position to be in the middle of the drag shadow
		            touch.set(width / 2, height / 2);
		        }

		        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
		        // from the dimensions passed in onProvideShadowMetrics().
		        @Override
		        public void onDrawShadow(Canvas canvas) {
		        	super.onDrawShadow(canvas);
		            // Draws the ColorDrawable in the Canvas passed in from the system.
		            shadow.draw(canvas);
		            outline.draw(canvas);
		        }
	}
	
}
