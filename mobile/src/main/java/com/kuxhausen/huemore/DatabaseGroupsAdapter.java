package com.kuxhausen.huemore;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kuxhausen.huemore.state.DatabaseGroup;

import java.util.ArrayList;

public class DatabaseGroupsAdapter extends SimpleCursorAdapter {

  private Activity mActivity;
  private ArrayList<DatabaseGroup> mList = new ArrayList<DatabaseGroup>();

  private ArrayList<DatabaseGroup> getList() {
    return mList;
  }

  public DatabaseGroup getRow(int position) {
    return getList().get(position);
  }

  public DatabaseGroupsAdapter(Activity activity, int layout, Cursor c, String[] from, int[] to,
                               int flags) {
    super(activity, layout, c, from, to, flags);
    this.mActivity = activity;
    changeCursor(c);
  }

  @Override
  /**
   * Cursor expected to match DatabaseGroup.GROUP_QUERY_COLUMNS
   */
  public void changeCursor(Cursor cursor) {
    super.changeCursor(cursor);

    mList.clear();
    if (cursor != null) {
      cursor.moveToPosition(-1); // not the same as move to first!
      while (cursor.moveToNext()) {
        mList.add(new DatabaseGroup(cursor, mActivity));
      }
    }
  }

  @Override
  public int getCount() {
    return (getList() != null) ? getList().size() : 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View moodRowView = convertView;
    ViewHolder viewHolder;

    if (moodRowView == null) {
      // Get a new instance of the row layout view
      LayoutInflater inflater = (mActivity).getLayoutInflater();

      moodRowView = inflater.inflate(R.layout.mood_row, null);
      TextView textView = (TextView) moodRowView.findViewById(android.R.id.text1);
      textView.setLongClickable(true);
      View starView = moodRowView.findViewById(android.R.id.text2);

      // Hold the view objects in an object, that way the don't need to be "re-found"
      viewHolder = new ViewHolder();
      moodRowView.setTag(viewHolder);
      viewHolder.mText = textView;
      viewHolder.mStar = starView;
    } else {
      viewHolder = (ViewHolder) moodRowView.getTag();
    }

    /** Set data to your Views. */
    DatabaseGroup item = getList().get(position);
    viewHolder.mData = item;
    if (!viewHolder.mText.getText().equals(item.getName())) {
      viewHolder.mText.setText(item.getName());
    }

    if (item.isStared()) {
      viewHolder.mStar.setVisibility(View.VISIBLE);
    } else {
      viewHolder.mStar.setVisibility(View.INVISIBLE);
    }

    return moodRowView;
  }


  public String getTextFromRowView(View row) {
    return ((TextView) row.findViewById(android.R.id.text1)).getText().toString();
  }

  public DatabaseGroup getRowFromView(View view) {
    return ((ViewHolder) view.getTag()).mData;
  }

  protected static class ViewHolder {

    protected TextView mText;
    protected View mStar;
    protected DatabaseGroup mData;
  }
}
