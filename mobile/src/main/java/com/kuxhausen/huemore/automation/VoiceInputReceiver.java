package com.kuxhausen.huemore.automation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.utils.DeferredLog;

import java.util.ArrayList;

/**
 * As part of the public automation package, this class's behavior must not be changed.
 */
public class VoiceInputReceiver extends WakefulBroadcastReceiver {

  /**
   * The intent action sent when starting VoiceInputReceiver with voice input extras.
   */
  public final static String ACTION_VOICE_INPUT = "automation.action.voice_input";

  /**
   * A String representing the best transcription of the users voice input. Note: any trigger phrase
   * such as "control my home" should not be included in the String.
   */
  public final static String EXTRA_VOICE_INPUT_STRING = "automation.extra.voice_input_string";

  /**
   * Similar to the definition in Android's RecognizerIntent class. Note: any trigger phrase such as
   * "control my home" should not be included in the Strings.
   */
  public final static String EXTRA_RESULTS = RecognizerIntent.EXTRA_RESULTS;

  /**
   * See the definition in Android's RecognizerIntent class
   */
  public final static String EXTRA_CONFIDENCE_SCORES = RecognizerIntent.EXTRA_CONFIDENCE_SCORES;


  private final static String TAG = "automation";

  @Override
  public void onReceive(Context context, Intent intent) {
    DeferredLog.v(TAG, "onReceive");

    if (ACTION_VOICE_INPUT.equals(intent.getAction())) {
      Intent transmitter = new Intent(context, ConnectivityService.class);

      Bundle b = intent.getExtras();
      if (b.containsKey(EXTRA_RESULTS)) {
        ArrayList<String> voiceList = b.getStringArrayList(EXTRA_RESULTS);
        if (voiceList != null) {
          transmitter.putStringArrayListExtra(InternalArguments.VOICE_INPUT_LIST, voiceList);

          if (b.containsKey(EXTRA_CONFIDENCE_SCORES)) {
            float[] confidences = b.getFloatArray(EXTRA_CONFIDENCE_SCORES);
            if (confidences != null) {
              transmitter.putExtra(InternalArguments.VOICE_INPUT_CONFIDENCE_ARRAY, confidences);
            }
          }

          DeferredLog.v(TAG, "valid voice extras");
          startWakefulService(context, transmitter);
        }
      } else if (b.containsKey(EXTRA_VOICE_INPUT_STRING)) {
        ArrayList<String> voiceList = b.getStringArrayList(EXTRA_VOICE_INPUT_STRING);
        if (voiceList != null) {
          transmitter.putStringArrayListExtra(InternalArguments.VOICE_INPUT, voiceList);

          DeferredLog.v(TAG, "valid voice extras");
          startWakefulService(context, transmitter);
        }
      }
    }
  }
}
