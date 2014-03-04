package com.kuxhausen.huemore.editmood;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
		hs = new BulbState();
	}
	
	public StateCell clone(){
		StateCell copy = new StateCell(context);
		if(hs!=null)
			copy.hs = hs.clone();
		if(name!=null)
			copy.name = new String(name);
		copy.selected = Boolean.valueOf(selected);
		return copy;
	}
	
	public View getView(ViewGroup parent, OnClickListener l, SherlockFragment frag, OnLongClickListener longL) {
		View rowView;
		LayoutInflater inflater = frag.getActivity().getLayoutInflater();
		if(hs.ct!=null && hs.ct!=0){
			rowView = inflater.inflate(R.layout.edit_mood_colortemp_row, parent, false);
			TextView stateText = (TextView) rowView.findViewById(R.id.ctTextView);
			stateText.setText(hs.getCT());
		} else if(hs.xy!=null){
			rowView = inflater.inflate(R.layout.edit_mood_row, parent, false);

			ImageView state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			int color = getStateColor(hs, true);
			ColorDrawable cd = new ColorDrawable(color);
			cd.setAlpha(255);
			if((color%0xff000000)!=0)
				state_color.setImageDrawable(cd);
		}
		else if(hs.hue!=null && hs.sat!=null){
			rowView = inflater.inflate(R.layout.edit_mood_row, parent, false);

			ImageView state_color = (ImageView) rowView
					.findViewById(R.id.stateColorView);
			int color = getStateColor(hs, true);
			ColorDrawable cd = new ColorDrawable(color);
			cd.setAlpha(255);
			if((color%0xff000000)!=0)
				state_color.setImageDrawable(cd);		
		}else if (hs.on!=null){
			rowView = inflater.inflate(R.layout.edit_mood_on_row, parent, false);
			TextView stateText = (TextView) rowView.findViewById(R.id.onTextView);
			if(hs.on!=null && hs.on)
				stateText.setText(context.getResources().getString(R.string.cap_on));
			else
				stateText.setText(context.getResources().getString(R.string.cap_off));
		} else{
			rowView = inflater.inflate(R.layout.edit_mood_row, parent, false);
		}
		if(selected)
			rowView.setBackgroundColor(context.getResources().getColor(R.color.blue_color));
		else
			rowView.setBackgroundColor(0);
		
		rowView.setOnClickListener(l);
		
		if(longL!=null)
			rowView.setOnLongClickListener(longL);
		if(frag!=null)
			frag.registerForContextMenu(rowView);
		return rowView;
	}
	
	//TODO add color generation support for color temp, on, off
	public static int getStateColor(BulbState hs, boolean sRGB){
		if(hs==null)
			return 0;
		if(hs.ct!=null && hs.ct!=0){
			Float[] hueSat = Utils.xyTOhs(Utils.ctTOxy(hs.ct));
			float[] hsv = new float[3];
	    	hsv[0] = (float) (hueSat[0] *360) ;
	    	hsv[1] = (float) (hueSat[1]);
	    	hsv[2] = (hs.bri!=null)?hs.bri/255f:1f; //remember relative brightness
	    	return Color.HSVToColor(hsv);
		}
		else if(hs.xy!=null){
			Float[] hueSat = (sRGB) ? Utils.xyTOsRGBhs(hs.xy) : Utils.xyTOhs(hs.xy);
			float[] hsv = new float[3];
	    	hsv[0] = (float) (hueSat[0] *360) ;
	    	hsv[1] = (float) (hueSat[1]);
	    	hsv[2] = (hs.bri!=null)?hs.bri/255f:1f; //remember relative brightness
	    	return Color.HSVToColor(hsv);
		}
		else if(hs.hue!=null && hs.sat!=null){
			float[] hsv = new float[3];
	    	hsv[0] = (float) ((hs.hue *360)/ 65535.0) ;
	    	hsv[1] = (float) (hs.sat / 255.0);
	    	hsv[2] = 1f;
	    	return Color.HSVToColor(hsv);		
		} else {
			return 0;
		}
	}
}
