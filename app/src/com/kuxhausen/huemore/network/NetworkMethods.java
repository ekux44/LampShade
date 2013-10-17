package com.kuxhausen.huemore.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.Bridge;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbList;
import com.kuxhausen.huemore.state.api.BulbState;
import com.kuxhausen.huemore.state.api.LightsPutResponse;
import com.kuxhausen.huemore.state.api.RegistrationRequest;
import com.kuxhausen.huemore.state.api.RegistrationResponse;

public class NetworkMethods {
	public static void PreformTransmitGroupMood(MoodExecuterService service, Integer bulb, BulbState bs){
		if (service == null || bulb == null || bs == null)
			return;
		
		Integer[] bulbs = {bulb};
		//TODO reimplement with support for Moods
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(service);
		String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferenceKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return;
		
		Gson gson = new Gson();
		for (int i = 0; i < bulbs.length; i++) {
			String url = "http://" + bridge + "/api/" + hash
					+ "/lights/" + bulbs[i] + "/state";
			
			GsonRequest<LightsPutResponse[]> req = new GsonRequest<LightsPutResponse[]>(Method.PUT, url,gson.toJson(bs), LightsPutResponse[].class, null,
					new BasicSuccessListener<LightsPutResponse[]>(service), new BasicErrorListener(service));
			req.setTag(InternalArguments.TRANSIENT_NETWORK_REQUEST);
			service.getRequestQueue().add(req);
		}
	}
	
	public static void PreformSetBulbAttributes(MoodExecuterService service, int bulbNum, BulbAttributes bulbAtt){
		if (service == null || bulbAtt == null)
			return;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(service);
		String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferenceKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return;
		
		Gson gson = new Gson();
		String url = "http://" + bridge + "/api/" + hash + "/lights/" + bulbNum;
		
		GsonRequest<LightsPutResponse[]> req = new GsonRequest<LightsPutResponse[]>(Method.PUT, url,gson.toJson(bulbAtt), LightsPutResponse[].class, null,
				new BasicSuccessListener<LightsPutResponse[]>(service), new BasicErrorListener(service));
		req.setTag(InternalArguments.PERMANENT_NETWORK_REQUEST);
		service.getRequestQueue().add(req);
	}
	
	public static void PreformGetBulbList(MoodExecuterService service, OnBulbListReturnedListener listener){
		if (service == null)
			return;

		// Get username and IP from preferences cache
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(service);
		String bridge = settings.getString(PreferenceKeys.BRIDGE_IP_ADDRESS,
				null);
		String hash = settings.getString(PreferenceKeys.HASHED_USERNAME, "");

		if (bridge == null)
			return;
		
		String url = "http://" + bridge + "/api/" + hash+ "/lights";
		
		GsonRequest<BulbList> req = new GsonRequest<BulbList>(Method.GET, url, null, BulbList.class, null,
				new BulbListSuccessListener(service, listener, service), new BasicErrorListener(service));
		req.setTag(InternalArguments.PERMANENT_NETWORK_REQUEST);
		service.getRequestQueue().add(req);
	}
	
	public static void PreformRegister(MoodExecuterService service, Listener<RegistrationResponse[]>[] listeners, Bridge[] bridges, String username, String deviceType){
		if (service == null || bridges == null)
			return;
		Gson gson = new Gson();
		RegistrationRequest request = new RegistrationRequest();
		request.username = username;
		request.devicetype = deviceType;
		String registrationRequest = gson.toJson(request);
		
		for (int i = 0; i< bridges.length; i++) {
		
			String url = "http://" + bridges[i].internalipaddress+ "/api/";
			
			GsonRequest<RegistrationResponse[]> req = new GsonRequest<RegistrationResponse[]>(Method.POST, url, registrationRequest, RegistrationResponse[].class, null,
					listeners[i], null);
			req.setTag(InternalArguments.TRANSIENT_NETWORK_REQUEST);
			service.getRequestQueue().add(req);
		}
	}	
}
