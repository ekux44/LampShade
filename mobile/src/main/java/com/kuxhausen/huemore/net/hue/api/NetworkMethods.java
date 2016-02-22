package com.kuxhausen.huemore.net.hue.api;

import com.google.gson.Gson;

import android.content.Context;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.kuxhausen.huemore.net.hue.HubConnection;
import com.kuxhausen.huemore.net.hue.PendingStateChange;
import com.kuxhausen.huemore.net.hue.Route;
import com.kuxhausen.huemore.net.hue.api.BulbAttributesSuccessListener.OnBulbAttributesReturnedListener;
import com.kuxhausen.huemore.net.hue.api.BulbListSuccessListener.OnBulbListReturnedListener;

public class NetworkMethods {

  public static void preformTransmitPendingState(Route route, String hash, Context context,
                                                 RequestQueue queue, HubConnection connection,
                                                 PendingStateChange pState) {
    if (queue == null) {
      return;
    }

    Gson gson = new Gson();

    String url =
        route.address + "/api/" + hash + "/lights/" + pState.hubBulb.getHubBulbNumber()
        + "/state";

    GsonRequest<LightsPutResponse[]> req =
        new GsonRequest<LightsPutResponse[]>(Method.PUT, url, gson.toJson(pState.sentState),
                                             LightsPutResponse[].class, null,
                                             new StateSuccessListener(connection, pState, route),
                                             new StateErrorListener(connection, pState, route));
    req.setTag("");
    queue.add(req);
  }

  public static void PreformGetBulbAttributes(Route route, String hash, Context context,
                                              RequestQueue queue, ConnectionMonitor monitor,
                                              OnBulbAttributesReturnedListener listener,
                                              String bulb) {
    if (queue == null) {
      return;
    }

    String url = route.address + "/api/" + hash + "/lights/" + bulb;

    GsonRequest<BulbAttributes> req =
        new GsonRequest<BulbAttributes>(Method.GET, url, null, BulbAttributes.class, null,
                                        new BulbAttributesSuccessListener(monitor, listener, bulb,
                                                                          route),
                                        new BasicErrorListener(monitor, route)
        );
    req.setTag("");
    queue.add(req);
  }

  public static void setBulbAttributes(Route route, String hash, Context context,
                                       RequestQueue queue, ConnectionMonitor monitor,
                                       int bulbNum, BulbAttributes bulbAtt) {
    if (queue == null || bulbAtt == null) {
      return;
    }

    Gson gson = new Gson();
    String url = route.address + "/api/" + hash + "/lights/" + bulbNum;

    GsonRequest<LightsPutResponse[]> req =
        new GsonRequest<>(Method.PUT, url, gson.toJson(bulbAtt.getSettableAttributes()),
                                             LightsPutResponse[].class, null,
                                             new BasicSuccessListener<LightsPutResponse[]>(monitor,
                                                                                           route),
                                             new BasicErrorListener(monitor, route)
        );
    req.setTag("");
    queue.add(req);
  }

  public static void PreformGetBulbList(Route route, String hash, Context context,
                                        RequestQueue queue, ConnectionMonitor monitor,
                                        OnBulbListReturnedListener listener) {
    if (queue == null) {
      return;
    }

    String url = route.address + "/api/" + hash + "/lights";

    GsonRequest<BulbList> req =
        new GsonRequest<BulbList>(Method.GET, url, null, BulbList.class, null,
                                  new BulbListSuccessListener(monitor, listener, context, route),
                                  new BasicErrorListener(
                                      monitor, route)
        );
    req.setTag("");
    queue.add(req);
  }

  public static void PreformRegister(RequestQueue queue,
                                     Listener<RegistrationResponse[]>[] listeners, Bridge[] bridges,
                                     String username,
                                     String deviceType) {
    if (queue == null || bridges == null) {
      return;
    }
    Gson gson = new Gson();
    RegistrationRequest request = new RegistrationRequest();
    request.username = username;
    request.devicetype = deviceType;
    String registrationRequest = gson.toJson(request);

    for (int i = 0; i < bridges.length; i++) {

      String url = bridges[i].internalipaddress + "/api/";

      GsonRequest<RegistrationResponse[]> req =
          new GsonRequest<RegistrationResponse[]>(Method.POST, url, registrationRequest,
                                                  RegistrationResponse[].class, null, listeners[i],
                                                  null);
      req.setTag("");
      queue.add(req);
    }
  }
}
