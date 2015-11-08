package com.kuxhausen.huemore.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.alarm.AlarmData;
import com.kuxhausen.huemore.persistence.Definitions;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AlarmWidgetService extends RemoteViewsService {

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
  }
}


/**
 * This is the factory that will provide data to the collection widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

  private Context mContext;
  private Cursor mCursor;
  private int mAppWidgetId;

  public StackRemoteViewsFactory(Context context, Intent intent) {
    mContext = context;
    mAppWidgetId =
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                           AppWidgetManager.INVALID_APPWIDGET_ID);
  }

  public void onCreate() {
    // Since we reload the cursor in onDataSetChanged() which gets called immediately after
    // onCreate(), we do nothing here.
  }

  public void onDestroy() {
    if (mCursor != null) {
      mCursor.close();
    }
  }

  public int getCount() {
    return mCursor.getCount();
  }

  public RemoteViews getViewAt(int position) {
    // Get the data for this position from the content provider
    String timeText = "0:00 AM";
    String subText = "Error";
    long rowID = -1;
    boolean alarmOn = false;
    if (mCursor.moveToPosition(position)) {
      AlarmData data = new AlarmData(mCursor);

      timeText = data.getUserTimeString(mContext);
      subText = data.getSecondaryDescription(mContext);
      alarmOn = data.isEnabled();
      rowID = data.getId();
    }

    final int itemId = R.layout.widget_alarm_row;
    RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
    rv.setTextViewText(R.id.timeTextView, timeText);
    rv.setTextViewText(R.id.subTextView, subText);
    if (alarmOn) {
      rv.setImageViewResource(R.id.alarmOnOffImageButton, R.drawable.on);
    } else {
      rv.setImageViewResource(R.id.alarmOnOffImageButton, R.drawable.off);
    }

    // Set the click intent so that we can handle it and show a toast message
    final Intent fillInIntent = new Intent();
    final Bundle extras = new Bundle();
    extras.putLong(InternalArguments.ALARM_ID, rowID);
    fillInIntent.putExtras(extras);
    rv.setOnClickFillInIntent(R.id.alarmOnOffImageButton, fillInIntent);

    return rv;
  }

  public RemoteViews getLoadingView() {
    return null;
  }

  public int getViewTypeCount() {
    return 1;
  }

  public long getItemId(int position) {
    return position;
  }

  public boolean hasStableIds() {
    return true;
  }

  public void onDataSetChanged() {
    // Refresh the cursor
    if (mCursor != null) {
      mCursor.close();
    }

    ContentResolver r = mContext.getContentResolver();
    mCursor =
        r.query(Definitions.AlarmColumns.ALARMS_URI, AlarmData.QUERY_COLUMNS, null, null, null);

  }
}
