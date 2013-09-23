package com.kuxhausen.huemore.editmood;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.api.BulbState;

public class StateCell {

	public String name;
	public int color;
	public BulbState hs;
	
	
	public View getView(int position, ViewGroup parent, OnClickListener l, SherlockFragment frag) {
		View rowView;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		if(hs.ct!=null && hs.ct!=0){
			rowView = inflater.inflate(R.layout.edit_mood_colortemp_row, null);
			TextView stateText = (TextView) rowView.findViewById(R.id.ctTextView);
			stateText.setText(hs.getCT());
		}else{
			rowView = inflater.inflate(R.layout.edit_mood_row, null);

			ImageView state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			ColorDrawable cd = new ColorDrawable(color);
			cd.setAlpha(255);
			if((color%0xff000000)!=0)
				state_color.setImageDrawable(cd);		
		}
		rowView.setOnClickListener(l);
		if(frag!=null)
			frag.registerForContextMenu(rowView);
		rowView.setTag(position);
		return rowView;
	}
}
