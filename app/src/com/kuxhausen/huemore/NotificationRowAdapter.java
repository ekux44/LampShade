package com.kuxhausen.huemore;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.MoodPlayer;
import com.kuxhausen.huemore.net.PlayingMood;

public class NotificationRowAdapter extends ArrayAdapter<PlayingMood> implements
    OnActiveMoodsChangedListener, OnClickListener {

  private Context context;
  private MoodPlayer mPlayer;

  public NotificationRowAdapter(Context con, MoodPlayer mp) {
    super(con, android.R.id.text1, mp.getPlayingMoods());
    context = con;
    mPlayer = mp;
    mp.addOnActiveMoodsChangedListener(this);
  }


  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View rowView = convertView;
    ViewHolder holder;

    if (rowView == null) {
      // Get a new instance of the row layout view
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      rowView = inflater.inflate(R.layout.notification_row, null);

      // Hold the view objects in an object, that way the don't need to be "re-finded"
      holder = new ViewHolder();

      holder.mainText = (TextView) rowView.findViewById(R.id.notificationText);
      holder.cancelButton = (ImageButton) rowView.findViewById(R.id.moodStopButton);

      rowView.setTag(holder);
    } else {
      holder = (ViewHolder) rowView.getTag();
    }

    /** Set data to your Views. */
    PlayingMood pm = mPlayer.getPlayingMoods().get(position);
    holder.mainText.setTag(pm);
    holder.mainText.setText(pm.toString());
    holder.cancelButton.setOnClickListener(this);
    holder.cancelButton.setTag(pm);

    return rowView;
  }

  protected static class ViewHolder {
    protected TextView mainText;
    protected ImageButton cancelButton;
  }

  @Override
  public void onActiveMoodsChanged() {
    this.notifyDataSetChanged();
  }

  public void onDestroy() {
    mPlayer.removeOnActiveMoodsChangedListener(this);
  }

  @Override
  public int getCount() {
    return mPlayer.getPlayingMoods().size();
  }

  @Override
  public boolean isEmpty() {
    return mPlayer.getPlayingMoods().isEmpty();
  }

  @Override
  public PlayingMood getItem(int position) {
    return mPlayer.getPlayingMoods().get(position);
  }


  @Override
  public void onClick(View v) {
    PlayingMood pm = (PlayingMood) v.getTag();
    mPlayer.cancelMood(pm.getGroup());
  }

}
