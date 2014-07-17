package com.kuxhausen.huemore.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.state.GroupMoodBrightness;
import com.kuxhausen.huemore.voice.SpeechParser;

import java.util.List;

public class VoiceReadRouterActivity extends Activity {

  private static final int VOICE_RECOGNITION_REQUEST_CODE = 123;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.placeholder);
    startVoiceRecognitionActivity();
  }

  /**
   * Fire an intent to start the speech recognition activity.
   */
  private void startVoiceRecognitionActivity() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    // Specify the calling package to identify your application
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

    // Display an hint to the user about what he should say.
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "bedroom to relax");

    // Given an hint to the recognizer about what the user is going to say
    intent
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

    // Specify how many results you want to receive. The results will be sorted
    // where the first result is the one with higher confidence.
    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

    // Specify the recognition language. This parameter has to be specified only if the
    // recognition has to be done in a specific language and not the default one (i.e., the
    // system locale). Most of the applications do not have to set this parameter.
    //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
      List<String> candidateSpeeches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      float[] confidences = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
      GroupMoodBrightness gmb = SpeechParser.parse(this, null, candidateSpeeches, confidences);

      if (gmb != null) {
        Intent transmitter = new Intent(this, ConnectivityService.class);
        transmitter.putExtra(DatabaseDefinitions.InternalArguments.MOOD_NAME, gmb.mood);
        transmitter.putExtra(DatabaseDefinitions.InternalArguments.GROUP_NAME, gmb.group);
        transmitter
            .putExtra(DatabaseDefinitions.InternalArguments.MAX_BRIGHTNESS, gmb.brightness);
        startService(transmitter);
      }
    }

    this.finish();
  }
}
