package com.kuxhausen.huemore;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;

import java.lang.Override;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kuxhausen.huemore.editmood.StateCell;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

/**
 * Custom view that shows a pie chart and, optionally, a label.
 */
public class MoodView extends View {
   
	public List<Item> mData = new ArrayList<Item>();
	private float maxCol;
	private float maxRow;
	private int xStart, yStart, xWidth, yWidth;
	private float colSpacing = 0, rowSpacing=0;
	private Mood mood;
	private Paint boarderPaint;
    private RectF boarderSize;
	
    /**
     * Class constructor taking only a context. Use this constructor to create
     * {@link MoodView} objects from your own code.
     *
     * @param context
     */
    public MoodView(Context context) {
        super(context);
        this.setBackgroundColor(Color.TRANSPARENT);
        init();
    }

    /**
     * Class constructor taking a context and an attribute set. This constructor
     * is used by the layout engine to construct a {@link MoodView} from a set of
     * XML attributes.
     *
     * @param context
     * @param attrs   An attribute set which can contain attributes from
     *                {@link com.example.android.customviews.R.styleable.MoodView} as well as attributes inherited
     *                from {@link android.view.View}.
     */
    public MoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(Color.TRANSPARENT);

        init();
    }

    
    public void setMood(Mood m){
    	mood = m;
    	init();
    }
    
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children. PieChart lays out its children in onSizeChanged().
    	xStart = l + this.getPaddingLeft();
    	yStart = t + this.getPaddingTop();
    	xWidth = r - xStart - this.getPaddingRight();
    	yWidth = b - xStart - this.getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for(Item i : mData){
        	canvas.drawRoundRect(i.mSize, getResources().getDisplayMetrics().density * 0, getResources().getDisplayMetrics().density * 0, i.mPaint);
        }
        //canvas.drawRoundRect(new RectF(xStart, yStart, xStart+xWidth, yStart+yWidth), 1, 1, boarderPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        xWidth = w - this.getPaddingLeft() - this.getPaddingRight();
        yWidth = h - this.getPaddingTop() - this.getPaddingBottom();         
        onDataChanged();
    }

    /**
     * Do all of the recalculations needed when the data array changes.
     */
    private void onDataChanged() {
        // When the data changes, we have to recalculate
    	
    	boarderSize.left = xStart;
    	boarderSize.top = yStart;
    	boarderSize.right = xStart + xWidth;
    	boarderSize.bottom = yStart + yWidth;
    	
    	for(Item i: mData){
    		i.mSize.left = (xStart-colSpacing) + (i.c1/maxCol)*(xWidth+colSpacing) + colSpacing;
    		i.mSize.top = (yStart-rowSpacing) + (i.r1/maxRow)*(yWidth+rowSpacing) + rowSpacing;
    		i.mSize.right = (xStart-colSpacing) + (i.c2/maxCol)*(xWidth+colSpacing) - colSpacing;
    		i.mSize.bottom = (yStart-rowSpacing) + (i.r2/maxRow)*(yWidth+rowSpacing) - rowSpacing;
    	}
    	this.invalidate();
    }

    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
    	boarderPaint = new Paint(0);
    	boarderPaint.setAntiAlias(true);
    	boarderPaint.setStyle(Paint.Style.STROKE);
    	boarderPaint.setStrokeWidth(1*getResources().getDisplayMetrics().density);
    	boarderPaint.setColor(0xffffffff);
        boarderSize = new RectF();
    	
    	
        if (this.isInEditMode()) {
            Resources res = getResources();
            mData.add(new Item(res.getColor(R.color.bluegrass), 0,0,1,2));
            mData.add(new Item(res.getColor(R.color.chartreuse), 1,0,2,1));
            mData.add(new Item(res.getColor(R.color.emerald), 1,1,2,2));
            mData.add(new Item(res.getColor(R.color.seafoam), 2,0,3,1));
            mData.add(new Item(res.getColor(R.color.bluegrass), 2,1,3,2));
            mData.add(new Item(res.getColor(R.color.emerald), 0,2,3,3));
            
            maxCol = 3;
            maxRow = 3;
            //colSpacing = getResources().getDisplayMetrics().density*6/maxCol;
            //rowSpacing = getResources().getDisplayMetrics().density*6/maxRow;
            
            onDataChanged();
        } else if(mood!=null && mood.events!=null && mood.events.length>0){
        	mData.clear();
        	BulbState[][] bsMat = mood.getEventStatesAsSparseMatrix();
        	maxRow=bsMat.length;
        	maxCol=bsMat[0].length;
        	
        	for(int r = 0; r < maxRow; r++){
        		for(int c = 0; c < maxCol; c++){
        			BulbState b = bsMat[r][c];
        			if(b==null)
        				continue;
        			int numRowsSpanned=1;
        			examine: for(int r2 = r+1; r2<maxRow; r2++){
        				if(bsMat[r2][c]==null)
        					numRowsSpanned++;
        				else
        					break examine;
        			}
        			mData.add(new Item(StateCell.getStateColor(b), r,c,r+numRowsSpanned,c+1));
        		}
        	}
        	
            //colSpacing = getResources().getDisplayMetrics().density*6/maxCol;
            //rowSpacing = getResources().getDisplayMetrics().density*6/maxRow;
        	onDataChanged();
        }
    }

    /**
     * Maintains the state for a data item.
     */
    public class Item {
        public Paint mPaint;
        public RectF mSize;
        
        //0 indexed...
        public int c1, r1, c2, r2;

        public Item(int color, int r1, int c1, int r2, int c2){
        	this.c1 = c1;
        	this.r1 = r1;
        	this.c2 = c2;
        	this.r2 = r2;
            // Set up the paint for the shadow
            mPaint = new Paint(0);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(color);
            //mPaint.setMaskFilter(new BlurMaskFilter(4 * getResources().getDisplayMetrics().density, BlurMaskFilter.Blur.INNER));
            mSize = new RectF();
        }
        
    }

}
