package com.kuxhausen.huemore.editmood;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;

public class StateCell {

  public String name;
  public BulbState hs;
  public Context context;
  public boolean selected;

  public StateCell(Context ctx) {
    context = ctx;
    hs = new BulbState();
  }

  public StateCell clone() {
    StateCell copy = new StateCell(context);
    if (hs != null) {
      copy.hs = hs.clone();
    }
    if (name != null) {
      copy.name = new String(name);
    }
    copy.selected = Boolean.valueOf(selected);
    return copy;
  }

  public View getView(ViewGroup parent, OnClickListener l, Fragment frag,
                      OnLongClickListener longL) {
    View rowView;
    LayoutInflater inflater = frag.getActivity().getLayoutInflater();
    if (hs.getKelvinCT() != null) {
      rowView = inflater.inflate(R.layout.edit_mood_colortemp_row, parent, false);
      TextView stateText = (TextView) rowView.findViewById(R.id.ctTextView);
      stateText.setText(hs.getKelvinCT()+"K");
    } else if (hs.hasXY()) {
      rowView = inflater.inflate(R.layout.edit_mood_row, parent, false);

      ImageView state_color = (ImageView) rowView.findViewById(R.id.stateColorView);
      int color = getStateColor(hs, true);
      ColorDrawable cd = new ColorDrawable(color);
      cd.setAlpha(255);
      if ((color % 0xff000000) != 0) {
        state_color.setImageDrawable(cd);
      }
    } else if (hs.getOn() != null) {
      rowView = inflater.inflate(R.layout.edit_mood_on_row, parent, false);
      TextView stateText = (TextView) rowView.findViewById(R.id.onTextView);
      if (hs.getOn() != null && hs.getOn()) {
        stateText.setText(context.getResources().getString(R.string.cap_on));
      } else {
        stateText.setText(context.getResources().getString(R.string.cap_off));
      }
    } else {
      rowView = inflater.inflate(R.layout.edit_mood_row, parent, false);
    }
    if (selected) {
      rowView.setBackgroundColor(context.getResources().getColor(R.color.day_primary));
    } else {
      rowView.setBackgroundColor(0);
    }

    rowView.setOnClickListener(l);

    if (longL != null) {
      rowView.setOnLongClickListener(longL);
    }
    if (frag != null) {
      frag.registerForContextMenu(rowView);
    }
    return rowView;
  }

  // TODO add color generation support for color temp, on, off
  public static int getStateColor(BulbState hs, boolean sRGB) {
    if (hs == null) {
      return 0;
    }
    if (hs.getMiredCT() != null) {
      float[] hueSat = Utils.xyTOhs(Utils.ctTOxy(hs.getMiredCT()));
      float[] hsv = new float[3];
      hsv[0] = (hueSat[0] * 360);
      hsv[1] = (hueSat[1]);
      hsv[2] = (hs.get255Bri() != null) ? hs.get255Bri() / 255f : 1f; // remember relative brightness
      return Color.HSVToColor(hsv);
    } else if (hs.hasXY()) {
      float[] hueSat = (sRGB) ? Utils.xyTOsRGBhs(hs.getXY()) : Utils.xyTOhs(hs.getXY());
      float[] hsv = new float[3];
      hsv[0] = (hueSat[0] * 360);
      hsv[1] = (hueSat[1]);
      hsv[2] = (hs.get255Bri() != null) ? hs.get255Bri() / 255f : 1f; // remember relative brightness
      return Color.HSVToColor(hsv);
    } else {
      return 0;
    }
  }
}
