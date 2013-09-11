package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class MoodRowAdapter extends ArrayAdapter<MoodRow> {

	public MoodRowAdapter(Activity context, ArrayList<MoodRow> objects, OnClickListener l, SherlockFragment frag) {
		super(context, R.layout.edit_mood_row);
		this.activity = context;
		this.list = objects;
		this.listener = l;
		this.frag = frag;
	}

	private final OnClickListener listener;
	private final SherlockFragment frag;
	private final Activity activity;
	private final ArrayList<MoodRow> list;

	@Override
	public void add(MoodRow object){
		super.add(object);
		list.add(object);
	}
	
	@Override
	public void insert(MoodRow object, int position){
		super.insert(object, position);
		list.add(position, object);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder view;

		//if (rowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.edit_mood_row, null);

			// Hold the view objects in an object, that way the don't need to be
			// "re-  finded"
			view = new ViewHolder();

			view.state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			
			rowView.setOnClickListener(listener);
			if(frag!=null)
				frag.registerForContextMenu(rowView);
			//rowView.setTag(view);
			rowView.setTag(position);
		//} else {
		//	view = (ViewHolder) rowView.getTag();
		//}

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
