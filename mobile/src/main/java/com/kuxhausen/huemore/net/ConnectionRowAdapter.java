package com.kuxhausen.huemore.net;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kuxhausen.huemore.R;

import java.util.ArrayList;

public class ConnectionRowAdapter extends ArrayAdapter<Connection> {

  private Activity context;
  private ArrayList<Connection> list;

  public void setList(ArrayList<Connection> l) {
    list = l;
  }

  public ConnectionRowAdapter(Activity con, int layout, ArrayList<Connection> l) {
    super(con, layout, l);
    context = con;
    list = l;
  }


  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View rowView = convertView;
    ViewHolder holder;

    if (rowView == null) {
      // Get a new instance of the row layout view
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      rowView = inflater.inflate(R.layout.connection_row, null);

      // Hold the view objects in an object, that way the don't need to be "re-finded"
      holder = new ViewHolder();

      holder.mainText = (TextView) rowView.findViewById(R.id.mainTextView);
      holder.secondaryText = (TextView) rowView.findViewById(R.id.subTextView);
      holder.connectivingSpinner =
          (ProgressBar) rowView.findViewById(R.id.connectivityStatusProgressBar);
      holder.connectedIcon =
          (ImageView) rowView.findViewById(R.id.connectivityStatusConnectedImageView);
      holder.unreachableIcon =
          (ImageView) rowView.findViewById(R.id.connectivityStatusUnreachableImageView);

      rowView.setTag(holder);
    } else {
      holder = (ViewHolder) rowView.getTag();
    }

    /** Set data to your Views. */
    Connection c = list.get(position);
    holder.mainText.setTag(c);

    holder.mainText.setText(c.mainDescription());
    holder.secondaryText.setText(c.subDescription());

    boolean anyConnected = false;
    boolean anyUnknown = false;
    for (NetworkBulb nb : c.getBulbs()) {
      switch (nb.getConnectivityState()) {
        case Connected:
          anyConnected = true;
          break;
        case Unknown:
          anyUnknown = true;
          break;
        case Unreachable:
          break;
      }
    }

    if (anyConnected) {
      holder.connectedIcon.setVisibility(View.VISIBLE);
      holder.connectivingSpinner.setVisibility(View.GONE);
      holder.unreachableIcon.setVisibility(View.GONE);
    } else if (anyUnknown) {
      holder.connectedIcon.setVisibility(View.GONE);
      holder.connectivingSpinner.setVisibility(View.VISIBLE);
      holder.unreachableIcon.setVisibility(View.GONE);
    } else {
      holder.connectedIcon.setVisibility(View.GONE);
      holder.connectivingSpinner.setVisibility(View.GONE);
      holder.unreachableIcon.setVisibility(View.VISIBLE);
    }

    return rowView;
  }

  protected static class ViewHolder {

    protected TextView mainText;
    protected TextView secondaryText;
    protected ProgressBar connectivingSpinner;
    protected ImageView connectedIcon;
    protected ImageView unreachableIcon;
  }
}
