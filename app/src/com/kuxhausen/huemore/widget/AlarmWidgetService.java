/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kuxhausen.huemore.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.timing.AlarmRow;
import com.kuxhausen.huemore.timing.AlarmState;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
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
    		AlarmRow aRow = new AlarmRow(mContext, gson.fromJson(json, AlarmState.class), mCursor.getInt(1));
    		timeText = aRow.getTime();
        	subText = aRow.getSecondaryDescription();
        	alarmOn = aRow.isScheduled();
        	rowID = aRow.getID();
        }

        // Return a proper item with the proper day and temperature
        final String formatStr = mContext.getResources().getString(R.string.item_format_string);
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
        //mCursor = mContext.getContentResolver().query(WeatherDataProvider.CONTENT_URI, null, null,
        //        null, null);
        
        
        String[] columns = { AlarmColumns.STATE, BaseColumns._ID };
		mCursor = mContext.getContentResolver()
				.query(AlarmColumns.ALARMS_URI,
						columns, null, null, null);

    }
}
