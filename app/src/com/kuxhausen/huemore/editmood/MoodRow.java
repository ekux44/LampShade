package com.kuxhausen.huemore.editmood;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.MoodRowAdapter.ViewHolder;
import com.kuxhausen.huemore.state.api.BulbState;

public class MoodRow {

	public String name;
	public int color;
	public BulbState hs;
	
	private View me;
	
	public View getView(int position, ViewGroup parent, OnClickListener l, SherlockFragment frag) {
		
		View rowView = me;
		ViewHolder view;

		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		rowView = inflater.inflate(R.layout.edit_mood_row, null);

		view = new ViewHolder();

		view.state_color = (ImageView) rowView
				.findViewById(R.id.stateColorView);
		
		rowView.setOnClickListener(l);
		rowView.setTag(position);
		if(frag!=null)
			frag.registerForContextMenu(rowView);

		ColorDrawable cd = new ColorDrawable(color);
		cd.setAlpha(255);
		if((color%0xff000000)!=0)
			view.state_color.setImageDrawable(cd);
		rowView.setMinimumHeight(96);
		return rowView;
	}
}
