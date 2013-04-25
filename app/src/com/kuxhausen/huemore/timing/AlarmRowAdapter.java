package com.kuxhausen.huemore.timing;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AlarmRowAdapter extends SimpleCursorAdapter {

	private Cursor cursor;
	private Context context;
	private ArrayList<AlarmRow> list = new ArrayList<AlarmRow>();
	Gson gson = new Gson();
	
	public AlarmRowAdapter(Context context, int layout, Cursor c, String[] from,
	        int[] to, int flags) {
	    super(context, layout, c, from, to, flags);
	    this.cursor = c;
	    this.context = context;
	    
	    list = new ArrayList<AlarmRow>();
	    changeCursor(c);
	}
	@Override
	public void changeCursor(Cursor c){
		super.changeCursor(c);
		this.cursor=c;
		if(c!=null){
			while (cursor.moveToNext()) {
				list.add(new AlarmRow(context, gson.fromJson(cursor.getString(0),AlarmState.class),cursor.getInt(1)));
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
			view.time = (TextView) rowView
					.findViewById(R.id.timeTextView);
			view.secondaryDescription = (TextView) rowView
					.findViewById(R.id.subTextView);

			rowView.setTag(view);
		} else {
			view = (ViewHolder) rowView.getTag();
		}

		/** Set data to your Views. */
		
		AlarmRow item = list.get(position);
		view.scheduledButton.setChecked(item.isScheduled());
		view.time.setText(item.getTime());
		view.secondaryDescription.setText(item.getSecondaryDescription());
		return rowView;
	}

	protected static class ViewHolder {
		protected TextView time;
		protected TextView secondaryDescription;
		protected CompoundButton scheduledButton;
	}
}
