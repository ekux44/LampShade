package com.kuxhausen.huemore.widget;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.timing.DatabaseAlarm;
import com.kuxhausen.huemore.timing.AlarmState;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

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
    Gson gson = new Gson();

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
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
        String json = "";
        int rowID = -1;
        boolean alarmOn = false;
    	if (mCursor.moveToPosition(position)) {
    		json = mCursor.getString(0);
    		DatabaseAlarm aRow = new DatabaseAlarm(mContext, gson.fromJson(json, AlarmState.class), mCursor.getInt(1));
    		timeText = aRow.getTime();
        	subText = aRow.getSecondaryDescription();
        	alarmOn = aRow.getAlarmState().isScheduled();
        	rowID = aRow.getID();
        }

        final int itemId = R.layout.widget_alarm_row;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.timeTextView, timeText);
        rv.setTextViewText(R.id.subTextView, subText);
        if(alarmOn){
        	rv.setImageViewResource(R.id.alarmOnOffImageButton, R.drawable.on);
        }else{
        	rv.setImageViewResource(R.id.alarmOnOffImageButton, R.drawable.off);
        }
        
        
        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putInt(InternalArguments.ALARM_ID, rowID);
        extras.putString(InternalArguments.ALARM_JSON, json);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.alarmOnOffImageButton, fillInIntent);

        return rv;
    }
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
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
        String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
		mCursor = r.query(AlarmColumns.ALARMS_URI,
						columns, null, null, null);

    }
}
