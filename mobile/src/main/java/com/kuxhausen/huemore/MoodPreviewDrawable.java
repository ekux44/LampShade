package com.kuxhausen.huemore;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.kuxhausen.huemore.editmood.StateCell;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view that shows a pie chart and, optionally, a label.
 */
public class MoodPreviewDrawable extends Drawable {

  public List<Item> mData = new ArrayList<Item>();
  private float maxCol;
  private float maxRow;
  private int xStart, yStart, xWidth, yWidth;
  private float colSpacing = 0, rowSpacing = 0;
  private Mood mood;
  private Paint boarderPaint;
  private RectF boarderSize;

  private Rect padding;

  DisplayMetrics metrics;

  public MoodPreviewDrawable(DisplayMetrics mets) {
    metrics = mets;
    padding = new Rect(0, 0, (int) (8 * metrics.density), 0);
  }

  public void setMood(Mood m) {
    mood = m;
    init();
  }

  @Override
  public boolean getPadding(Rect r) {
    r.left = padding.left;
    r.top = padding.top;
    r.right = padding.right;
    r.bottom = padding.bottom;
    return true;
  }

  @Override
  public void setBounds(int l, int t, int r, int b) {
    super.setBounds(l, t, r, b);
    this.getPadding(padding);
    xStart = l + padding.left;
    yStart = t + padding.top;
    xWidth = r - xStart - padding.right;
    yWidth = b - xStart - padding.bottom;
    onDataChanged();
  }

  @Override
  public void setBounds(Rect rect) {
    super.setBounds(rect);
    this.getPadding(padding);
    xStart = rect.left + padding.left;
    yStart = rect.top + padding.top;
    xWidth = rect.right - xStart - padding.right;
    yWidth = rect.bottom - xStart - padding.bottom;
    onDataChanged();
  }

  @Override
  public int getIntrinsicWidth() {
    return (int) (metrics.density * 32) + padding.left + padding.right;
  }

  @Override
  public int getIntrinsicHeight() {
    return (int) (metrics.density * 32) + padding.top + padding.bottom;
  }

  @Override
  public void draw(Canvas canvas) {

    for (Item i : mData) {
      canvas.drawRoundRect(i.mSize, metrics.density * 0, metrics.density * 0, i.mPaint);
    }
    // canvas.drawRoundRect(new RectF(xStart, yStart, xStart+xWidth, yStart+yWidth), 1, 1,
    // boarderPaint);
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

    for (Item i : mData) {
      i.mSize.left = (xStart - colSpacing) + (i.c1 / maxCol) * (xWidth + colSpacing) + colSpacing;
      i.mSize.top = (yStart - rowSpacing) + (i.r1 / maxRow) * (yWidth + rowSpacing) + rowSpacing;
      i.mSize.right = (xStart - colSpacing) + (i.c2 / maxCol) * (xWidth + colSpacing) - colSpacing;
      i.mSize.bottom = (yStart - rowSpacing) + (i.r2 / maxRow) * (yWidth + rowSpacing) - rowSpacing;
    }
    this.invalidateSelf();
  }

  /**
   * Initialize the control. This code is in a separate method so that it can be called from both
   * constructors.
   */
  private void init() {
    boarderPaint = new Paint(0);
    boarderPaint.setAntiAlias(true);
    boarderPaint.setStyle(Paint.Style.STROKE);
    boarderPaint.setStrokeWidth(1 * metrics.density);
    boarderPaint.setColor(0xffffffff);
    boarderSize = new RectF();

    if (mood != null && mood.events != null && mood.events.length > 0) {
      mData.clear();
      BulbState[][] bsMat = mood.getEventStatesAsSparseMatrix();
      maxRow = bsMat.length;
      maxCol = bsMat[0].length;

      for (int r = 0; r < maxRow; r++) {
        for (int c = 0; c < maxCol; c++) {
          BulbState b = bsMat[r][c];
          if (b == null) {
            continue;
          }
          int numRowsSpanned = 1;
          examine:
          for (int r2 = r + 1; r2 < maxRow; r2++) {
            if (bsMat[r2][c] == null) {
              numRowsSpanned++;
            } else {
              break examine;
            }
          }
          mData.add(new Item(StateCell.getStateColor(b, true), r, c, r + numRowsSpanned, c + 1));
        }
      }

      // colSpacing = metrics.density*6/maxCol;
      // rowSpacing = metrics*6/maxRow;
      onDataChanged();
    }
  }

  /**
   * Maintains the state for a data item.
   */
  public class Item {

    public Paint mPaint;
    public RectF mSize;

    // 0 indexed...
    public int c1, r1, c2, r2;

    public Item(int color, int r1, int c1, int r2, int c2) {
      this.c1 = c1;
      this.r1 = r1;
      this.c2 = c2;
      this.r2 = r2;
      // Set up the paint for the shadow
      mPaint = new Paint(0);
      mPaint.setAntiAlias(true);
      mPaint.setStyle(Paint.Style.FILL);
      mPaint.setColor(color);
      // mPaint.setMaskFilter(new BlurMaskFilter(4 * metrics.density, BlurMaskFilter.Blur.INNER));
      mSize = new RectF();
    }

  }

  @Override
  public int getOpacity() {
    // TODO Auto-generated method stub
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    // TODO Auto-generated method stub

  }

}
