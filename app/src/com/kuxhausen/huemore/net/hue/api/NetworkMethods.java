package com.kuxhausen.huemore.net.hue.api;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.google.gson.Gson;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.HubData;
import com.kuxhausen.huemore.net.hue.PendingStateChange;
import com.kuxhausen.huemore.net.hue.api.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.net.hue.api.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetBulbColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.NetConnectionColumns;
import com.kuxhausen.huemore.state.BulbState;

public class NetworkMethods {


  public static HubData getBridgeAndHash(Context c) {
    Gson gson = new Gson();

    String[] columns =
        {BaseColumns._ID, NetConnectionColumns.TYPE_COLUMN, NetConnectionColumns.NAME_COLUMN,
            NetConnectionColumns.DEVICE_ID_COLUMN, NetConnectionColumns.JSON_COLUMN};
    String[] selectionArgs = {"" + NetBulbColumns.NetBulbType.PHILIPS_HUE};
    Cursor cursor =
        c.getContentResolver().query(NetConnectionColumns.URI, columns,
            NetConnectionColumns.TYPE_COLUMN + " = ?", selectionArgs, null);
    cursor.moveToPosition(-1);// not the same as move to first!
    while (cursor.moveToNext()) {
      return gson.fromJson(cursor.getString(4), HubData.class);
    }

    return null;
  }

  public static void preformTransmitPendingState(Context context, RequestQueue queue,
      HubConnection connection, PendingStateChange pState) {
    if (queue == null)
      return;

    HubData hub = getBridgeAndHash(context);
    String bridge = hub.localHubAddress;
    String hash = hub.hashedUsername;

    if (bridge == null)
      return;

    Gson gson = new Gson();

    String url =
        "http://" + bridge + "/api/" + hash + "/lights/" + pState.hubBulb.getHubBulbNumber()
            + "/state";

    GsonRequest<LightsPutResponse[]> req =
        new GsonRequest<LightsPutResponse[]>(Method.PUT, url, gson.toJson(pState.sentState),
            LightsPutResponse[].class, null, new StateSuccessListener(connection, pState),
            new StateErrorListener(connection, pState));
    req.setTag("");
    queue.add(req);
  }

  public static void PreformGetBulbAttributes(Context context, RequestQueue queue,
      ConnectionMonitor monitor, OnBulbAttributesReturnedListener listener, int bulb) {
    if (queue == null)
      return;

    HubData hub = getBridgeAndHash(context);
    String bridge = hub.localHubAddress;
    String hash = hub.hashedUsername;

    if (bridge == null)
      return;

    String url = "http://" + bridge + "/api/" + hash + "/lights/" + bulb;

    GsonRequest<BulbAttributes> req =
        new GsonRequest<BulbAttributes>(Method.GET, url, null, BulbAttributes.class, null,
            new BulbAttributesSuccessListener(monitor, listener, bulb), new BasicErrorListener(
                monitor));
    req.setTag("");
    queue.add(req);
  }

  public static void PreformSetBulbAttributes(Context context, RequestQueue queue,
      ConnectionMonitor monitor, int bulbNum, BulbAttributes bulbAtt) {
    if (queue == null || bulbAtt == null)
      return;

    HubData hub = getBridgeAndHash(context);
    String bridge = hub.localHubAddress;
    String hash = hub.hashedUsername;

    if (bridge == null)
      return;

    Gson gson = new Gson();
    String url = "http://" + bridge + "/api/" + hash + "/lights/" + bulbNum;

    GsonRequest<LightsPutResponse[]> req =
        new GsonRequest<LightsPutResponse[]>(Method.PUT, url, gson.toJson(bulbAtt),
            LightsPutResponse[].class, null,
            new BasicSuccessListener<LightsPutResponse[]>(monitor), new BasicErrorListener(monitor));
    req.setTag("");
    queue.add(req);
  }

  public static void PreformGetBulbList(Context context, RequestQueue queue,
      ConnectionMonitor monitor, OnBulbListReturnedListener listener) {
    if (queue == null)
      return;

    HubData hub = getBridgeAndHash(context);
    String bridge = hub.localHubAddress;
    String hash = hub.hashedUsername;

    if (bridge == null)
      return;

    String url = "http://" + bridge + "/api/" + hash + "/lights";

    GsonRequest<BulbList> req =
        new GsonRequest<BulbList>(Method.GET, url, null, BulbList.class, null,
            new BulbListSuccessListener(monitor, listener, context),
            new BasicErrorListener(monitor));
    req.setTag("");
    queue.add(req);
  }

  public static void PreformRegister(RequestQueue queue,
      Listener<RegistrationResponse[]>[] listeners, Bridge[] bridges, String username,
      String deviceType) {
    if (queue == null || bridges == null)
      return;
    Gson gson = new Gson();
    RegistrationRequest request = new RegistrationRequest();
    request.username = username;
    request.devicetype = deviceType;
    String registrationRequest = gson.toJson(request);

    for (int i = 0; i < bridges.length; i++) {

      String url = "http://" + bridges[i].internalipaddress + "/api/";

      GsonRequest<RegistrationResponse[]> req =
          new GsonRequest<RegistrationResponse[]>(Method.POST, url, registrationRequest,
              RegistrationResponse[].class, null, listeners[i], null);
      req.setTag("");
      queue.add(req);
    }
  }
}
