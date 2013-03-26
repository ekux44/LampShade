package com.kuxhausen.huemore;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class MoodRowAdapter extends ArrayAdapter<MoodRow> {

	public MoodRowAdapter(Activity context, ArrayList<MoodRow> objects) {
		super(context, R.layout.edit_mood_row);
		this.activity = context;
		this.list = objects;
	}

	private final Activity activity;
	private final ArrayList<MoodRow> list;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder view;

		if (rowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.edit_mood_row, null);

			// Hold the view objects in an object, that way the don't need to be
			// "re-  finded"
			view = new ViewHolder();

			view.state_color = (ImageView) rowView
					.findViewById(R.id.stateColorButton);

			rowView.setTag(view);
		} else {
			view = (ViewHolder) rowView.getTag();
		}

		/** Set data to your Views. */
		MoodRow item = list.get(position);

		ColorDrawable cd = new ColorDrawable(item.color);
		cd.setAlpha(255);
		view.state_color.setImageDrawable(cd);
		rowView.setMinimumHeight(96);
		return rowView;
	}

	protected static class ViewHolder {
		protected ImageView state_color;
	}
}
