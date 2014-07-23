package com.kuxhausen.huemore.editmood;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class StateCellAdapter extends BaseAdapter {

  public StateCellAdapter(OnClickListener l, ArrayList<StateCell> list, Fragment frag) {
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
    View v = list.get(position).getView(parent, l, frag, null);
    v.setTag(position);
    return v;
  }

  private OnClickListener l;
  private ArrayList<StateCell> list;
  private Fragment frag;
}
