package com.kuxhausen.huemore.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferencesKeys;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.LightsPutResponse;

public class NetworkMethods {
	public static void PreformTransmitGroupMood(RequestQueue mRequestQueue, Context cont, Integer[] bulbs, String[] moods){
		if (cont == null || bulbs == null || moods == null)
			return;

		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(cont);
		String bridge = settings.getString(PreferencesKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferencesKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return;
		
		for (int i = 0; i < bulbs.length; i++) {
		String url = "http://" + bridge + "/api/" + hash
				+ "/lights/" + bulbs[i] + "/state";
		
		Listener<LightsPutResponse> requestListener = new Listener<LightsPutResponse>() {
			public void onResponse(LightsPutResponse response) {
				
			}};
		
		GsonRequest req = new GsonRequest<LightsPutResponse>(Method.PUT, url,moods[i % moods.length], LightsPutResponse.class, null,
				requestListener, null);
		req.setTag(InternalArguments.TRANSIENT_NETWORK_REQUEST);
		mRequestQueue.add(req);
		}
	}
	
	public static void PreformSetBulbAttributes(RequestQueue mRequestQueue, Context cont, int bulbNum, BulbAttributes bulbAtt){
		if (cont == null || bulbAtt == null)
			return;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(cont);
		String bridge = settings.getString(PreferencesKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferencesKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return;
		
		Gson gson = new Gson();
		String url = "http://" + bridge + "/api/" + hash + "/lights/" + bulbNum;
		
		Listener<LightsPutResponse> requestListener = new Listener<LightsPutResponse>() {
			public void onResponse(LightsPutResponse response) {
				
			}};
		
		GsonRequest req = new GsonRequest<LightsPutResponse>(Method.PUT, url,gson.toJson(bulbAtt), LightsPutResponse.class, null,
				requestListener, null);
		req.setTag(InternalArguments.PERMANENT_NETWORK_REQUEST);
		mRequestQueue.add(req);
	}
}
