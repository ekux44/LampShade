package com.kuxhausen.huemore.wear;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.voice.SpeechParser;

public class WearService extends WearableListenerService {

  private GoogleApiClient mGoogleApiClient;

  @Override
  public void onCreate() {
    super.onCreate();
    //  Needed for communication between watch and device.
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
          @Override
          public void onConnected(Bundle connectionHint) {
            Log.d("wear", "onConnected: " + connectionHint);
          }

          @Override
          public void onConnectionSuspended(int cause) {
            Log.d("wear", "onConnectionSuspended: " + cause);
          }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult result) {
            Log.d("wear", "onConnectionFailed: " + result);
          }
        })
        .addApi(Wearable.API)
        .build();

    mGoogleApiClient.connect();
  }

  @Override
  public void onPeerConnected(Node peer) {
    super.onPeerConnected(peer);

    String id = peer.getId();
    String name = peer.getDisplayName();

    Log.d("wear", "Connected peer name & ID: " + name + "|" + id);
  }

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {

    Log.v("wear", "msg rcvd");
    Log.v("wear", messageEvent.getPath());

    GroupMoodBrightness gmb = SpeechParser.parse(this, messageEvent.getPath(), null, null);
    Intent trasmitter = new Intent(this, ConnectivityService.class);
    trasmitter.putExtra(DatabaseDefinitions.InternalArguments.MOOD_NAME, gmb.mood);
    trasmitter.putExtra(DatabaseDefinitions.InternalArguments.GROUP_NAME, gmb.group);
    trasmitter.putExtra(DatabaseDefinitions.InternalArguments.MAX_BRIGHTNESS, gmb.brightness);
    startService(trasmitter);
  }
}
