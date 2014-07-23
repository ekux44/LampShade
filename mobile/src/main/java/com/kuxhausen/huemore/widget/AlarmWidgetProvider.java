package com.kuxhausen.huemore.widget;

import com.google.gson.Gson;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.RemoteViews;

import com.kuxhausen.huemore.NavigationDrawerActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.AlarmColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.timing.AlarmState;
import com.kuxhausen.huemore.timing.DatabaseAlarm;

/**
 * Our data observer just notifies an update for all weather widgets when it detects a change.
 */
class AlarmDataProviderObserver extends ContentObserver {

  private AppWidgetManager mAppWidgetManager;
  private ComponentName mComponentName;

  AlarmDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
    super(h);
    mAppWidgetManager = mgr;
    mComponentName = cn;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void onChange(boolean selfChange) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      // The data has changed, so notify the widget that the collection view needs to be updated.
      // In response, the factory's onDataSetChanged() will be called which will requery the
      // cursor for the new data.
      mAppWidgetManager.notifyAppWidgetViewDataChanged(
          mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.alarm_list);
    }
  }
}


/**
 * The weather widget's AppWidgetProvider.
 */
public class AlarmWidgetProvider extends AppWidgetProvider {

  public static String CLICK_ACTION = "com.example.android.weatherlistwidget.CLICK";
  public static String REFRESH_ACTION = "com.example.android.weatherlistwidget.REFRESH";

  private static HandlerThread sWorkerThread;
  private static Handler sWorkerQueue;
  private static AlarmDataProviderObserver sDataObserver;

  Gson gson = new Gson();

  public AlarmWidgetProvider() {
    // Start the worker thread
    sWorkerThread = new HandlerThread("AlarmWidgetProvider-worker");
    sWorkerThread.start();
    sWorkerQueue = new Handler(sWorkerThread.getLooper());
  }

  @Override
  public void onEnabled(Context context) {
    // Register for external updates to the data to trigger an update of the widget. When using
    // content providers, the data is often updated via a background service, or in response to
    // user interaction in the main app. To ensure that the widget always reflects the current
    // state of the data, we must listen for changes and update ourselves accordingly.
    final ContentResolver r = context.getContentResolver();
    if (sDataObserver == null) {
      final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
      final ComponentName cn = new ComponentName(context, AlarmWidgetProvider.class);
      sDataObserver = new AlarmDataProviderObserver(mgr, cn, sWorkerQueue);
      r.registerContentObserver(AlarmColumns.ALARMS_URI, true, sDataObserver);
    }
  }

  @Override
  public void onReceive(Context ctx, Intent intent) {
    final String action = intent.getAction();
    if (action.equals(CLICK_ACTION)) {

      String json = intent.getStringExtra(InternalArguments.ALARM_JSON);
      int id = intent.getIntExtra(InternalArguments.ALARM_ID, -1);
      DatabaseAlarm aRow = new DatabaseAlarm(ctx, gson.fromJson(json, AlarmState.class), id);
      aRow.toggle();
    }

    super.onReceive(ctx, intent);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private RemoteViews buildLayout(Context context, int appWidgetId) {
    RemoteViews rv;
    // Specify the service to provide data for the collection widget. Note that we need to
    // embed the appWidgetId via the data otherwise it will be ignored.
    rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      final Intent intent = new Intent(context, AlarmWidgetService.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
      intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

      rv.setRemoteAdapter(appWidgetId, R.id.alarm_list, intent);

      // Set the empty view to be displayed if the collection is empty. It must be a sibling
      // view of the collection view.
      rv.setEmptyView(R.id.alarm_list, R.id.empty_view);

      final Intent onClickIntent = new Intent(context, AlarmWidgetProvider.class);
      onClickIntent.setAction(AlarmWidgetProvider.CLICK_ACTION);
      onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
      onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
      final PendingIntent onClickPendingIntent =
          PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setPendingIntentTemplate(R.id.alarm_list, onClickPendingIntent);

      Intent openAlarmsIntent = new Intent(context, NavigationDrawerActivity.class);
      openAlarmsIntent.putExtra(InternalArguments.NAV_DRAWER_PAGE,
                                NavigationDrawerActivity.ALARM_FRAG);
      openAlarmsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      final PendingIntent openAlarmsPendingIntent =
          PendingIntent.getActivity(context, NavigationDrawerActivity.ALARM_FRAG, openAlarmsIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setOnClickPendingIntent(R.id.alarms_icon, openAlarmsPendingIntent);

      final Intent openHueMoreIntent = new Intent(context, NavigationDrawerActivity.class);
      openHueMoreIntent.putExtra(InternalArguments.NAV_DRAWER_PAGE,
                                 NavigationDrawerActivity.GROUP_FRAG);
      openHueMoreIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      final PendingIntent openHueMorePendingIntent =
          PendingIntent.getActivity(context, -1, openHueMoreIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setOnClickPendingIntent(R.id.huemore_icon, openHueMorePendingIntent);
    }
    return rv;
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Update each of the widgets with the remote adapter
    for (int i = 0; i < appWidgetIds.length; ++i) {
      RemoteViews layout = buildLayout(context, appWidgetIds[i]);
      appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds);

    // wtf another google bug?
    this.onEnabled(context);
  }

  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId, Bundle newOptions) {
    RemoteViews layout;
    layout = buildLayout(context, appWidgetId);
    appWidgetManager.updateAppWidget(appWidgetId, layout);
  }
}
