package com.kuxhausen.huemore.alarm;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.kuxhausen.huemore.R;

import java.util.ArrayList;

public class AlarmRowAdapter extends ResourceCursorAdapter implements OnCheckedChangeListener {

  private ArrayList<AlarmData> mList = new ArrayList<AlarmData>();

  public AlarmRowAdapter(Context context, int layout, Cursor c, int flags) {
    super(context, layout, c, flags);
  }

  public AlarmData getRow(int position) {
    return mList.get(position);
  }

  @Override
  public int getCount() {
    return mList.size();
  }

  @Override
  public void changeCursor(Cursor cursor) {
    super.changeCursor(cursor);
    mList.clear();
    if (cursor != null) {
      cursor.moveToPosition(-1);// not the same as move to first!
      while (cursor.moveToNext()) {
        mList.add(new AlarmData(cursor));
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
      viewHolder.scheduledButton =
          (CompoundButton) rowView.findViewById(R.id.alarmOnOffCompoundButton);
      viewHolder.time = (TextView) rowView.findViewById(R.id.timeTextView);
      viewHolder.secondaryDescription = (TextView) rowView.findViewById(R.id.subTextView);

      // Hold the view objects in an object, that way the don't need to be "re-found"
      rowView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) rowView.getTag();
    }

    /** Set data to your Views. */
    AlarmData item = mList.get(cursor.getPosition());
    AlarmLogic.logAlarm("BindView", item);

    viewHolder.scheduledButton.setTag(item);
    viewHolder.scheduledButton.setOnCheckedChangeListener(null);
    viewHolder.scheduledButton.setChecked(item.isEnabled());
    viewHolder.scheduledButton.setOnCheckedChangeListener(this);
    viewHolder.time.setText(item.getUserTimeString(context));
    viewHolder.secondaryDescription.setText(item.getSecondaryDescription(context));
  }

  protected static class ViewHolder {

    protected TextView time;
    protected TextView secondaryDescription;
    protected CompoundButton scheduledButton;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    AlarmData ar = (AlarmData) buttonView.getTag();
    if (ar.isEnabled() != isChecked) {
      AlarmLogic.toggleAlarm(mContext, ar);
    }
  }
}
