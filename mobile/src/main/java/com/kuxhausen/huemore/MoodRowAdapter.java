package com.kuxhausen.huemore;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;

import java.util.ArrayList;

public class MoodRowAdapter extends ResourceCursorAdapter {

  private ArrayList<MoodRow> mList = new ArrayList<MoodRow>();

  public MoodRowAdapter(Context context, int layout, Cursor c, int flags) {
    super(context, layout, c, flags);
  }

  public MoodRow getRow(int position) {
    return mList.get(position);
  }

  @Override
  public int getCount() {
    return mList.size();
  }

  @Override
  /***
   * Cursor expected to have at each row {mood name, mood id}
   */
  public void changeCursor(Cursor cursor) {
    super.changeCursor(cursor);
    mList.clear();
    if (cursor != null) {
      cursor.moveToPosition(-1);// not the same as move to first!
      while (cursor.moveToNext()) {
        try {
          mList.add(new MoodRow(cursor.getString(0), cursor.getLong(1),
                                HueUrlEncoder.decode(cursor.getString(
                                    2)).second.first, cursor.getString(3), cursor.getInt(4)));
        } catch (InvalidEncodingException e) {
        } catch (FutureEncodingException e) {
        }
      }
      cursor.moveToFirst();
      notifyDataSetChanged();
    }
  }

  @Override
  public void bindView(View rowView, Context context, Cursor cursor) {
    ViewHolder viewHolder;

    if (rowView.getTag() == null) {
      viewHolder = new ViewHolder();
      viewHolder.moodName = (TextView) rowView.findViewById(android.R.id.text1);
      viewHolder.star = rowView.findViewById(android.R.id.text2);

      // Hold the view objects in an object, that way the don't need to be "re-found"
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) rowView.getTag();
    }

    /** Set data to your Views. */
    MoodRow item = mList.get(cursor.getPosition());
    if (!viewHolder.moodName.getText().equals(item.getName())) {
      viewHolder.moodName.setText(item.getName());
      MoodPreviewDrawable mDraw =
          new MoodPreviewDrawable(mContext.getResources().getDisplayMetrics());
      mDraw.setMood(item.getMood());
      viewHolder.moodName.setCompoundDrawablesWithIntrinsicBounds(mDraw, null, null, null);
    }
    if (item.isStared()) {
      viewHolder.star.setVisibility(View.VISIBLE);
    } else {
      viewHolder.star.setVisibility(View.INVISIBLE);
    }
  }

  protected static class ViewHolder {

    protected TextView moodName;
    protected View star;
  }
}
