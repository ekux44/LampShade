package com.kuxhausen.huemore;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import com.kuxhausen.huemore.state.DatabaseGroup;

import java.util.ArrayList;

public class DatabaseGroupsAdapter extends ResourceCursorAdapter {

  private ArrayList<DatabaseGroup> mList = new ArrayList<DatabaseGroup>();

  public DatabaseGroupsAdapter(Context context, int layout, Cursor c, int flags) {
    super(context, layout, c, flags);
  }

  public DatabaseGroup getRow(int position) {
    return mList.get(position);
  }

  @Override
  public int getCount() {
    return mList.size();
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
        mList.add(new DatabaseGroup(cursor, this.mContext));
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
      viewHolder.groupName = (TextView) rowView.findViewById(android.R.id.text1);
      viewHolder.star = rowView.findViewById(android.R.id.text2);

      // Hold the view objects in an object, that way the don't need to be "re-found"
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) rowView.getTag();
    }

    /** Set data to your Views. */
    DatabaseGroup item = mList.get(cursor.getPosition());
    if (!viewHolder.groupName.getText().equals(item.getName())) {
      viewHolder.groupName.setText(item.getName());
    }
    if (item.isStared()) {
      viewHolder.star.setVisibility(View.VISIBLE);
    } else {
      viewHolder.star.setVisibility(View.INVISIBLE);
    }
  }

  protected static class ViewHolder {

    protected TextView groupName;
    protected View star;
  }
}
