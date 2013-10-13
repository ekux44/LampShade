package com.kuxhausen.huemore.editmood;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.api.BulbState;

public class StateCell {

	public String name;
	public BulbState hs;
	public Context context;
	public boolean selected;
	
	public StateCell(Context ctx){
		context = ctx;
	}
	
	public View getView(int position, ViewGroup parent, OnClickListener l, SherlockFragment frag) {
		View rowView;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		if(hs.ct!=null && hs.ct!=0){
			rowView = inflater.inflate(R.layout.edit_mood_colortemp_row, null);
			TextView stateText = (TextView) rowView.findViewById(R.id.ctTextView);
			stateText.setText(hs.getCT());
		} else if(hs.xy!=null){
			rowView = inflater.inflate(R.layout.edit_mood_row, null);

			ImageView state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			Float[] hueSat = Utils.xyTOhs(hs.xy[0], hs.xy[1]);
			float[] hsv = new float[3];
	    	hsv[0] = (float) (hueSat[0] *360) ;
	    	hsv[1] = (float) (hueSat[1]);
	    	hsv[2] = 1f;
	    	int color = Color.HSVToColor(hsv);
			ColorDrawable cd = new ColorDrawable(color);
			cd.setAlpha(255);
			if((color%0xff000000)!=0)
				state_color.setImageDrawable(cd);
		}
		else if(hs.hue!=null && hs.sat!=null){
			rowView = inflater.inflate(R.layout.edit_mood_row, null);

			ImageView state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			float[] hsv = new float[3];
	    	hsv[0] = (float) ((hs.hue *360)/ 65535.0) ;
	    	hsv[1] = (float) (hs.sat / 255.0);
	    	hsv[2] = 1f;
	    	int color = Color.HSVToColor(hsv);
			ColorDrawable cd = new ColorDrawable(color);
			cd.setAlpha(255);
			if((color%0xff000000)!=0)
				state_color.setImageDrawable(cd);		
		}else if (hs.on!=null){
			rowView = inflater.inflate(R.layout.edit_mood_on_row, null);
			TextView stateText = (TextView) rowView.findViewById(R.id.onTextView);
			if(hs.on!=null && hs.on)
				stateText.setText(context.getResources().getString(R.string.cap_on));
			else
				stateText.setText(context.getResources().getString(R.string.cap_off));
		} else{
			rowView = inflater.inflate(R.layout.edit_mood_row, null);
		}
		if(selected)
			rowView.setBackgroundColor(0xFFFFBB33);
		else
			rowView.setBackgroundColor(0);
		
		rowView.setOnClickListener(l);
		if(frag!=null)
			frag.registerForContextMenu(rowView);
		rowView.setTag(position);
		return rowView;
	}
}
