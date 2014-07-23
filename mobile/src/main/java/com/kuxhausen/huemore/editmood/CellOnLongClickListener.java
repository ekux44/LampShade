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
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnLongClickListener;

public class CellOnLongClickListener implements OnLongClickListener {

  private EditMoodStateGridFragment mFrag;
  private ViewType mViewType;

  public CellOnLongClickListener(EditMoodStateGridFragment editMoodStateGridFragment,
                                 ViewType viewType) {
    mFrag = editMoodStateGridFragment;
    mViewType = viewType;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public boolean onLongClick(View v) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      switch (mViewType) {
        case StateCell:
          mFrag.mStateGrid.setStateSelectionByTag(v);
          // Instantiates the drag shadow builder.
          View.DragShadowBuilder myCellShadow = new View.DragShadowBuilder(v);
          // Starts the drag
          v.startDrag(null, myCellShadow, mViewType, 0);
          break;
        case Channel:
          mFrag.mStateGrid.setChannelSelectionByTag(v);
          View.DragShadowBuilder myChannelShadow =
              new ChannelShadowBuilder(v, mFrag.getGridHeight(), mFrag.getActivity());
          v.startDrag(null, myChannelShadow, mViewType, 0);
          break;
        case Timeslot:
          mFrag.mStateGrid.setTimeslotSelectionByTag(v);
          View.DragShadowBuilder myTimeslotShadow =
              new TimeslotShadowBuilder(v, mFrag.getGridWidth(), mFrag.getActivity());
          v.startDrag(null, myTimeslotShadow, mViewType, 0);
          break;
      }
      // enter action mode
      mFrag.mActionMode =
          ((ActionBarActivity) mFrag.getActivity()).startSupportActionMode(new StateGridActionMode(
              mFrag, mViewType));
    }
    return true;
  }


  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private static class TimeslotShadowBuilder extends View.DragShadowBuilder {

    private static Drawable shadow;
    private static ShapeDrawable outline;
    private static int mWidth;

    public TimeslotShadowBuilder(View v, int pxWidth, Context c) {

      // Stores the View parameter passed to myDragShadowBuilder.
      super(v);

      // Creates a draggable image that will fill the Canvas provided by the system.
      shadow = new ColorDrawable(Color.LTGRAY);

      RectShape outlineShape = new RectShape();
      outline = new ShapeDrawable(outlineShape);
      outline.getPaint().setColor(Color.LTGRAY);
      outline.getPaint().setStyle(Style.STROKE);
      outline.getPaint().setStrokeWidth(4 * c.getResources().getDisplayMetrics().density);

      mWidth = pxWidth;
    }

    // Defines a callback that sends the drag shadow dimensions and touch point back to the system.
    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
      int width = getView().getWidth();
      int height = getView().getHeight();

      // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
      // Canvas that the system will provide. As a result, the drag shadow will fill the Canvas.
      shadow.setBounds(width, 0, width + mWidth, height);
      outline.setBounds(0, 0, width, height);

      // Sets the size parameter's width and height values. These get back to the system
      // through the size parameter.
      size.set(width + mWidth, height);

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

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private static class ChannelShadowBuilder extends View.DragShadowBuilder {

    private static Drawable shadow;
    private static ShapeDrawable outline;
    private static int mHeight;

    public ChannelShadowBuilder(View v, int pxHeight, Context c) {

      // Stores the View parameter passed to myDragShadowBuilder.
      super(v);

      // Creates a draggable image that will fill the Canvas provided by the system.
      shadow = new ColorDrawable(Color.LTGRAY);

      RectShape outlineShape = new RectShape();
      outline = new ShapeDrawable(outlineShape);
      outline.getPaint().setColor(Color.LTGRAY);
      outline.getPaint().setStyle(Style.STROKE);
      outline.getPaint().setStrokeWidth(4 * c.getResources().getDisplayMetrics().density);

      mHeight = pxHeight;
    }

    // Defines a callback that sends the drag shadow dimensions and touch point back to the system.
    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
      int width = getView().getWidth();
      int height = getView().getHeight();

      // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
      // Canvas that the system will provide. As a result, the drag shadow will fill the Canvas.
      shadow.setBounds(0, height, width, height + mHeight);
      outline.setBounds(0, 0, width, height);

      // Sets the size parameter's width and height values. These get back to the system
      // through the size parameter.
      size.set(width, height + mHeight);

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
