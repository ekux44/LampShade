package com.kuxhausen.huemore.editmood;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class EditColorWheelFragment extends Fragment implements OnCreateColorListener,
    com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener {

  ColorPicker picker;
  SaturationBar saturationBar;
  ValueBar valueBar;
  EditStatePagerDialogFragment statePager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.colorwheel_state_fragment, null);

    picker = (ColorPicker) myView.findViewById(R.id.picker);
    saturationBar = (SaturationBar) myView.findViewById(R.id.saturationbar);
    picker.addSaturationBar(saturationBar);
    valueBar = (ValueBar) myView.findViewById(R.id.valuebar);
    picker.addValueBar(valueBar);
    picker.setShowOldCenterColor(false);
    picker.setOnColorChangedListener(this);

    return myView;
  }

  @Override
  public void onColorChanged(int rgb) {
    float[] newHSV = new float[3];
    int red = ((rgb >>> 16) & 0xFF);
    int green = ((rgb >>> 8) & 0xFF);
    int blue = ((rgb) & 0xFF);
    Color.RGBToHSV(red, green, blue, newHSV);
    Float[] newHueSat = {newHSV[0] / 360f, newHSV[1]};
    Float[] newXY = Utils.hsTOxy(newHueSat);

    BulbState state = statePager.getState();
    // relative brightness
    if (newHSV[2] != 1f)
      state.bri = (int) (newHSV[2] * 255f);
    state.on = true;
    state.xy = newXY;
    state.ct = null;
    if (EditStatePagerDialogFragment.currentPage == (EditStatePagerDialogFragment.WHEEL_PAGE - EditStatePagerDialogFragment.hasNoRecentStates))
      statePager.setState(state, this, "wheel");
  }

  @Override
  public boolean stateChanged() {
    BulbState state = statePager.getState();
    if (state.xy != null) {
      Float[] hueSat = Utils.xyTOhs(state.xy);
      // don't forget relative brightness if set
      float[] hsv = {hueSat[0] * 360, hueSat[1], (state.bri != null) ? state.bri / 255f : 1f};
      state.on = true;

      int rgb = Color.HSVToColor(hsv);

      if (picker != null && saturationBar != null && valueBar != null) {
        picker.setOnColorChangedListener(null);
        picker.setColor(rgb);
        saturationBar.setSaturation(hsv[1]);
        picker.setOnColorChangedListener(this);
        picker.invalidate();
      }
      return true;
    }
    return false;
  }

  @Override
  public void setStatePager(EditStatePagerDialogFragment statePage) {
    statePager = statePage;
  }
}
