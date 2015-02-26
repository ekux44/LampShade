package com.kuxhausen.huemore;

import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;

import java.util.ArrayList;

public class MoodRowAdapter extends SimpleCursorAdapter {

  private Cursor cursor;
  private Activity mActivity;
  private ArrayList<MoodRow> list = new ArrayList<MoodRow>();
  private MoodListFragment moodListFrag;

  private ArrayList<MoodRow> getList() {
    return list;
  }

  public MoodRow getRow(int position) {
    return getList().get(position);
  }

  public MoodRowAdapter(MoodListFragment frag, Activity activity, int layout, Cursor c,
                        String[] from, int[] to, int flags) {
    super(activity, layout, c, from, to, flags);
    this.cursor = c;
    this.mActivity = activity;
    this.moodListFrag = frag;
    changeCursor(c);
  }

  @Override
  /***
   * Cursor expected to have at each row {mood name, mood id}
   */
  public void changeCursor(Cursor c) {
    super.changeCursor(c);
    // Log.e("changeCursor", ""+c);
    this.cursor = c;
    list = new ArrayList<MoodRow>();
    if (cursor != null) {
      cursor.moveToPosition(-1);// not the same as move to first!
      while (cursor.moveToNext()) {
        // Log.e("changeCursor", "row asdf");
        try {
          list.add(new MoodRow(cursor.getString(0), cursor.getLong(1), HueUrlEncoder.decode(cursor
                                                                                                .getString(
                                                                                                    2)).second.first,
                               cursor.getString(3), cursor.getInt(4)
          ));
        } catch (InvalidEncodingException e) {
        } catch (FutureEncodingException e) {
        }
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
    MoodRow item = getList().get(position);
    viewHolder.mData = item;
    if (!viewHolder.mText.getText().equals(item.getName())) {
      viewHolder.mText.setText(item.getName());
      MoodPreviewDrawable mDraw =
          new MoodPreviewDrawable(mActivity.getResources().getDisplayMetrics());
      mDraw.setMood(item.getMood());
      viewHolder.mText.setCompoundDrawablesWithIntrinsicBounds(mDraw, null, null, null);
    }
    viewHolder.mText.setOnClickListener(new OnClickForwardingListener(moodListFrag, position));

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

  public MoodRow getRowFromView(View view) {
    return ((ViewHolder) view.getTag()).mData;
  }

  protected static class ViewHolder {

    protected TextView mText;
    protected View mStar;
    protected MoodRow mData;
  }

  public class OnClickForwardingListener implements OnClickListener {

    MoodListFragment mlf;
    int position;

    public OnClickForwardingListener(MoodListFragment frag, int pos) {
      mlf = frag;
      position = pos;
    }

    @Override
    public void onClick(View v) {
      View textView = v;
      mlf.onListItemClick(mlf.getListView(), textView, position, textView.getId());
    }
  }

}
