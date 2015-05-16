package com.kuxhausen.huemore;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class VoiceInputActivity extends Activity {

  private TextView mTextView;
  private GoogleApiClient mGoogleApiClient;
  private static String latestSpeeech;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main_wear);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView) stub.findViewById(R.id.text);
      }
    });

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

    displaySpeechRecognizer();
  }

  private static final int SPEECH_REQUEST_CODE = 0;

  // Create an intent that can start the Speech Recognizer activity
  private void displaySpeechRecognizer() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    // Start the activity, the intent will be populated with the speech text
    startActivityForResult(intent, SPEECH_REQUEST_CODE);
  }

  // This callback is invoked when the Speech Recognizer returns.
  // This is where you process the intent and extract the speech text from the intent.
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
      List<String> results = data.getStringArrayListExtra(
          RecognizerIntent.EXTRA_RESULTS);
      String spokenText = results.get(0);
      // Do something with spokenText
      if (mTextView != null) {
        mTextView.setText(spokenText);
      }

      latestSpeeech = spokenText;
      sendSpeech();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private List<Node> getNodes() {
    List<Node> nodes = new ArrayList<Node>();
    NodeApi.GetConnectedNodesResult rawNodes =
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
    for (Node node : rawNodes.getNodes()) {
      nodes.add(node);
    }
    return nodes;
  }

  private void sendSpeech() {
    //  This, or at least getNodes() has to be done in the background. Explanation there.
    new AsyncTask<Void, Void, List<Node>>() {

      @Override
      protected List<Node> doInBackground(Void... params) {
        return getNodes();
      }

      @Override
      protected void onPostExecute(List<Node> nodeList) {

        List<Node> nodes = nodeList;
        for (Node node : nodes) {
          PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
              mGoogleApiClient,
              node.getId(),
              VoiceInputActivity.latestSpeeech,
              null
          );

          result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
              Log.v("wear", "we got something back");
            }
          });
        }
      }
    }.execute();


  }
}
