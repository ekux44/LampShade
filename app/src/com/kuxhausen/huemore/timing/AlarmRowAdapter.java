package com.kuxhausen.huemore.timing;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;

public class AlarmRowAdapter extends SimpleCursorAdapter implements
		OnCheckedChangeListener {

	private Cursor cursor;
	private Context context;
	private ArrayList<DatabaseAlarm> list = new ArrayList<DatabaseAlarm>();
	Gson gson = new Gson();

	private ArrayList<DatabaseAlarm> getList() {
		return list;
	}

	public DatabaseAlarm getRow(int position) {
		return getList().get(position);
	}

	public AlarmRowAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;

		changeCursor(c);
	}

	@Override
	public void changeCursor(Cursor c) {
		super.changeCursor(c);
		this.cursor = c;
		list = new ArrayList<DatabaseAlarm>();
		if (cursor != null) {
			cursor.moveToPosition(-1);// not the same as move to first!
			while (cursor.moveToNext()) {
				// Log.e("changeCursor _row",
				// gson.fromJson(cursor.getString(0),AlarmState.class).mood);
				list.add(new DatabaseAlarm(context, gson.fromJson(
						cursor.getString(0), AlarmState.class), cursor
						.getInt(1)));
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder view;

		if (rowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.alarm_row, null);

			// Hold the view objects in an object, that way the don't need to be
			// "re-  finded"
			view = new ViewHolder();

			view.scheduledButton = (CompoundButton) rowView
					.findViewById(R.id.alarmOnOffCompoundButton);
			view.time = (TextView) rowView.findViewById(R.id.timeTextView);
			view.taggedView = rowView
					.findViewById(R.id.rowExcludingCompoundButton);
			view.secondaryDescription = (TextView) rowView
					.findViewById(R.id.subTextView);

			rowView.setTag(view);
		} else {
			view = (ViewHolder) rowView.getTag();
		}

		/** Set data to your Views. */

		DatabaseAlarm item = getList().get(position);
		view.taggedView.setTag(item);
		view.scheduledButton.setTag(item);
		view.scheduledButton.setOnCheckedChangeListener(null);
		view.scheduledButton.setChecked(item.getAlarmState().isScheduled());
		view.scheduledButton.setOnCheckedChangeListener(this);
		view.time.setText(item.getTime());
		view.secondaryDescription.setText(item.getSecondaryDescription());
		return rowView;
	}

	protected static class ViewHolder {
		protected TextView time;
		protected TextView secondaryDescription;
		protected CompoundButton scheduledButton;
		protected View taggedView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		DatabaseAlarm ar = (DatabaseAlarm) buttonView.getTag();
		if (ar.getAlarmState().isScheduled() != isChecked) {
			ar.toggle();
		}
	}

	@Override
	public int getCount() {
		// Log.e("getCount", ""+((getList() != null) ? getList().size() : 0));
		return (getList() != null) ? getList().size() : 0;
	}
}
