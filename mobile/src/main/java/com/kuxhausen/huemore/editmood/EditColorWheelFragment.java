package com.kuxhausen.huemore.editmood;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class EditColorWheelFragment extends Fragment implements
                                                     EditStateDialogFragment.StateSelector,
                                                     ColorPicker.OnColorChangedListener {

  private ColorPicker mPicker;
  private SaturationBar mSaturationBar;
  private ValueBar mValueBar;
  private EditStateDialogFragment mParent;
  private BulbState mBulbState;

  @Override
  public void initialize(EditStateDialogFragment statePage, BulbState initialState) {
    mParent = statePage;
    mBulbState = new BulbState();

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

    mBulbState.setOn(true);
    mBulbState.setEffect(BulbState.Effect.NONE);

    View myView = inflater.inflate(R.layout.colorwheel_state_fragment, null);

    mPicker = (ColorPicker) myView.findViewById(R.id.picker);
    mSaturationBar = (SaturationBar) myView.findViewById(R.id.saturationbar);
    mPicker.addSaturationBar(mSaturationBar);
    mValueBar = (ValueBar) myView.findViewById(R.id.valuebar);
    mPicker.addValueBar(mValueBar);
    mPicker.setShowOldCenterColor(false);
    mPicker.setOnColorChangedListener(this);

    stateChanged(mBulbState);
    return myView;
  }

  @Override
  public void onColorChanged(int rgb) {
    float[] newHSV = new float[3];
    int red = ((rgb >>> 16) & 0xFF);
    int green = ((rgb >>> 8) & 0xFF);
    int blue = ((rgb) & 0xFF);
    Color.RGBToHSV(red, green, blue, newHSV);
    float[] newHueSat = {newHSV[0] / 360f, newHSV[1]};
    float[] newXY = Utils.hsTOxy(newHueSat);

    // relative brightness
    if (newHSV[2] != 1f) {
      mBulbState.set255Bri((int) (newHSV[2] * 255f));
    }

    mBulbState.setXY(newXY);

    mParent.setStateIfVisible(mBulbState, this, EditStatePager.WHEEL_PAGE);
  }

  @Override
  public BulbState getState() {
    return mBulbState;
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

      if (mPicker != null && mSaturationBar != null && mValueBar != null) {
        mPicker.setOnColorChangedListener(null);
        mPicker.setColor(rgb);
        mSaturationBar.setSaturation(hsv[1]);
        mPicker.setOnColorChangedListener(this);
        mPicker.invalidate();
      }
    }
  }
}
