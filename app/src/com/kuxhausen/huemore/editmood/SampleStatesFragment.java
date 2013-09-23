package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.api.BulbState;

public class SampleStatesFragment extends SherlockFragment implements OnCreateColorListener, OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		
		ArrayList<MoodRow> list = new ArrayList<MoodRow>();
		
		String[] simpleNames = {"Reading","Relax","Concentrate","Energize", "Deep Sea", "Deep Sea", "Fruit", "Fruit", "Fruit"};
		int[] simpleSat = {144, 211 ,49, 232, 253, 230, 244, 254, 173};
		int[] simpleHue = {15331, 13122, 33863, 34495, 45489, 1111, 15483, 25593, 64684};
		    
		for(int i = 0; i<simpleSat.length; i++){
			BulbState hs = new BulbState();
	    	hs.sat=(short)simpleSat[i];
	    	hs.hue=simpleHue[i];
	    	hs.on=true;
	    	hs.effect="none";
	    	
	    	MoodRow mr = new MoodRow();
	    	mr.hs = hs;
	    	mr.name = simpleNames[i];
	    	float[] hsv = new float[3];
	    	hsv[0] = (float) ((hs.hue *360)/ 65535.0) ;
	    	hsv[1] = (float) (hs.sat / 255.0);
	    	hsv[2] = 1f;
	    	mr.color = Color.HSVToColor(hsv);
	    	list.add(mr);
    	
		}
		
		
		GridView g = (GridView) myView.findViewById(R.id.myGrid);
		g.setAdapter(new ImageAdapter(this, list));
		
		return myView;
	}
	
	@Override
	public Intent onCreateColor(Integer transitionTime) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	
	public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(OnClickListener l, ArrayList<MoodRow> list) {
          	this.l = l;
          	this.list = list;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	
        	View v = list.get(position).getView(position, parent, l, SampleStatesFragment.this);
        	v.setLayoutParams(new GridView.LayoutParams(100, 100));
        	v.setPadding(8, 8, 8, 8);
        	
        	return v;
        	/*
        	ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(72, 72));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
			
            return imageView;*/
        }

        private OnClickListener l;
        private ArrayList<MoodRow> list;
    }

}
