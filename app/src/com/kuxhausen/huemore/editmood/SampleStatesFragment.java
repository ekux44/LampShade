package com.kuxhausen.huemore.editmood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.api.BulbState;

public class SampleStatesFragment extends SherlockFragment implements OnCreateColorListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.grid_view, null);
		
		GridView g = (GridView) myView.findViewById(R.id.myGrid);
        g.setAdapter(new ImageAdapter(this.getActivity()));
		
		return myView;
	}
	
	@Override
	public Intent onCreateColor(Integer transitionTime) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	BulbState hs = new BulbState();
        	hs.sat=(short)144;
        	hs.hue=15331;
        	hs.on=true;
        	hs.effect="none";
        	
        	MoodRow mr = new MoodRow();
        	mr.hs = hs;
        	mr.name = "Reading";
        	float[] hsv = new float[3];
        	hsv[0] = (float) ((hs.hue *360)/ 65535.0) ;
        	hsv[1] = (float) (hs.sat / 255.0);
        	hsv[2] = 1f;
        	mr.color = Color.HSVToColor(hsv);
        	
        	View v = mr.getView(position, parent, null, SampleStatesFragment.this);
        	v.setLayoutParams(new GridView.LayoutParams(72, 72));
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

        private Context mContext;

        private Integer[] mThumbIds = {
                R.drawable.on, R.drawable.off,
        };
    }

}
