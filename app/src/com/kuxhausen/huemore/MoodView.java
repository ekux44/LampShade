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

import com.kuxhausen.huemore.state.Mood;

/**
 * Custom view that shows a pie chart and, optionally, a label.
 */
public class MoodView extends View {
   
	public List<Item> mData = new ArrayList<Item>();
	private float maxCol;
	private float maxRow;
	private int xStart, yStart, xWidth, yWidth;
	private float colSpacing, rowSpacing;
	private Mood mood;
	
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
    	xStart = l;
    	yStart = t;
    	xWidth = r-l;
    	yWidth = b-t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for(Item i : mData){
        	canvas.drawRoundRect(i.mSize, 20, 20, i.mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        xWidth = w;
        yWidth = h;         
        onDataChanged();
    }

    /**
     * Do all of the recalculations needed when the data array changes.
     */
    private void onDataChanged() {
        // When the data changes, we have to recalculate
    	for(Item i: mData){
    		i.mSize.left = xStart + (i.c1/maxCol)*xWidth + colSpacing;
    		i.mSize.top = yStart + (i.r1/maxCol)*yWidth + rowSpacing;
    		i.mSize.right = xStart + (i.c2/maxCol)*xWidth - colSpacing;
    		i.mSize.bottom = yStart + (i.r2/maxCol)*yWidth -rowSpacing;
    	}	
    }

    /**
     * Initialize the control. This code is in a separate method so that it can be
     * called from both constructors.
     */
    private void init() {
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
            colSpacing = 30/maxCol;
            rowSpacing = 30/maxRow;
            
            onDataChanged();
        } /*else if(mood!=null && mood.events!=null && mood.events.length>0){
        	int maxCol = mood.getNumChannels();
        	int maxRow = mood.getNumTimeslots();
        	
        	HashMap<Integer, Integer> timeslotMapping = new HashMap<Integer, Integer>();
        	int[][] colorGrid = new int[maxRow][maxCol]; 
        	
        	onDataChanged();
        }*/
    }

    /**
     * Maintains the state for a data item.
     */
    public class Item {
        public Paint mPaint;
        public RectF mSize;
        
        //0 indexed...
        public int c1, r1, c2, r2;

        public Item(int color, int c1, int r1, int c2, int r2){
        	this.c1 = c1;
        	this.r1 = r1;
        	this.c2 = c2;
        	this.r2 = r2;
            // Set up the paint for the shadow
            mPaint = new Paint(0);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(color);
            mPaint.setMaskFilter(new BlurMaskFilter(12, BlurMaskFilter.Blur.NORMAL));
            mSize = new RectF();
        }
        
    }

}
