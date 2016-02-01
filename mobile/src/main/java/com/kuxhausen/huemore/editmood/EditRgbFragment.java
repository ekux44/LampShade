package com.kuxhausen.huemore.editmood;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;

public class EditRgbFragment extends Fragment implements EditStateDialogFragment.StateSelector,
                                                         View.OnClickListener {

  private BulbState mBulbState;
  private EditText mRedEditText, mGreenEditText, mBlueEditText;
  private ImageButton mPlayButton;
  private EditStateDialogFragment mParent;

  @Override
  public void initialize(EditStateDialogFragment statePage, BulbState initialState) {
    mParent = statePage;

    mBulbState = new BulbState();
    mBulbState.setOn(true);
    mBulbState.setEffect(BulbState.Effect.NONE);

    if (initialState.get255Bri() != null) {
      mBulbState.set255Bri(initialState.get255Bri());
    } else {
      mBulbState.set255Bri(255);
    }
    if (initialState.hasXY()) {
      mBulbState.setXY(initialState.getXY());
    } else {
      float[] reading = {0.4571f, 0.4123f};
      mBulbState.setXY(reading);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.edit_rgb_fragment, null);

    mRedEditText = (EditText) groupDialogView.findViewById(R.id.edit_rgb_red);
    mGreenEditText = (EditText) groupDialogView.findViewById(R.id.edit_rgb_green);
    mBlueEditText = (EditText) groupDialogView.findViewById(R.id.edit_rgb_blue);

    mPlayButton = (ImageButton) groupDialogView.findViewById(R.id.action_play);

    mPlayButton.setOnClickListener(this);

    mRedEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          String corrected = ensureInBounds(v.getText().toString());
          mRedEditText.setText(corrected);
        }
        return false;
      }
    });
    mGreenEditText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              String corrected = ensureInBounds(v.getText().toString());
              mGreenEditText.setText(corrected);
            }
            return false;
          }
        });
    mBlueEditText.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              String corrected = ensureInBounds(v.getText().toString());
              mBlueEditText.setText(corrected);
            }
            return false;
          }
        });

    stateChanged(mBulbState);
    return groupDialogView;
  }

  @Override
  public void stateChanged(BulbState newState) {
    if (newState.getMiredCT() != null || newState.hasXY()) {
      float[] xy = newState.getXY();
      if (xy == null) {
        xy = Utils.ctTOxy(newState.getMiredCT());
      }

      float[] hueSat = Utils.xyTOhs(xy);
      // don't forget relative brightness if set
      float[]
          hsv =
          {hueSat[0] * 360, hueSat[1],
           (newState.get255Bri() != null) ? newState.get255Bri() / 255f : 1f};

      int rgb = Color.HSVToColor(hsv);

      mRedEditText.setText("" + Color.red(rgb));
      mGreenEditText.setText("" + Color.green(rgb));
      mBlueEditText.setText("" + Color.blue(rgb));
    }
  }

  /**
   * @param red   0-255
   * @param green 0-255
   * @param blue  0-255
   */
  private float[] rgbToXy(int red, int green, int blue) {
    float[] newHSV = new float[3];
    Color.RGBToHSV(red, green, blue, newHSV);
    float[] newHueSat = {newHSV[0] / 360f, newHSV[1]};
    float[] newXY = Utils.hsTOxy(newHueSat);

    return newXY;
  }

  @Override
  public BulbState getState() {
    mRedEditText.setText(ensureInBounds(mRedEditText.getText().toString()));
    mGreenEditText.setText(ensureInBounds(mGreenEditText.getText().toString()));
    mBlueEditText.setText(ensureInBounds(mBlueEditText.getText().toString()));

    int red = Integer.parseInt(mRedEditText.getText().toString());
    int green = Integer.parseInt(mGreenEditText.getText().toString());
    int blue = Integer.parseInt(mBlueEditText.getText().toString());
    mBulbState.setXY(rgbToXy(red, green, blue));
    return mBulbState;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.action_play:
        mParent.setStateIfVisible(getState(), this, EditStatePager.RGB_PAGE);
    }
  }

  private String ensureInBounds(String editTextInput) {
    Integer temp;
    try {
      temp = Integer.parseInt(editTextInput.toString());
    } catch (NumberFormatException e) {
      temp = Integer.MAX_VALUE;
    }
    temp = Math.max(temp, 0);
    temp = Math.min(temp, 255);
    return temp.toString();
  }
}
