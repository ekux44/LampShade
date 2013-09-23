package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StateCellAdapter extends BaseAdapter {
    public StateCellAdapter(OnClickListener l, ArrayList<StateCell> list, SherlockFragment frag) {
      	this.l = l;
      	this.list = list;
      	this.frag = frag;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {        	
    	View v = list.get(position).getView(position, parent, l, frag);
    	
    	return v;
    }

    private OnClickListener l;
    private ArrayList<StateCell> list;
    private SherlockFragment frag;
}

