package com.kuxhausen.huemore;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.MoodExecuterService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.Group;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;

public class ColorWheelFragment extends Fragment implements OnCheckedChangeListener,
    com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener {

  public interface OnColorChangedListener {
    void colorChanged(int color, int hue);

    float getSaturation();
  }

  ColorPicker picker;
  SaturationBar saturationBar;
  private BulbState hs = new BulbState();
  {
    hs.on = true;
    hs.effect = "none";
    Float[] lol = {.5f, .5f};
    hs.xy = lol;// TODO change

  }
  Gson gson = new Gson();

  CompoundButton colorLoop;

  LinearLayout colorLoopLayout, transitionLayout;
  boolean colorLoopLayoutVisible = true;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.colorwheel_mood_fragment, null);

    picker = (ColorPicker) groupDialogView.findViewById(R.id.picker);
    saturationBar = (SaturationBar) groupDialogView.findViewById(R.id.saturationbar);
    picker.addSaturationBar(saturationBar);
    picker.setShowOldCenterColor(false);

    Bundle args = getArguments();
    if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
      BulbState bs =
          gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE), BulbState.class);
      loadPrevious(bs);
    }
    loadPrevious(hs);

    if (colorLoopLayoutVisible) {
      colorLoop = (CompoundButton) groupDialogView.findViewById(R.id.colorLoopCompoundButton);
      colorLoopLayout = (LinearLayout) groupDialogView.findViewById(R.id.colorLoopLayout);
    } else {
      groupDialogView.findViewById(R.id.colorLoopLayout).setVisibility(View.GONE);
    }

    return groupDialogView;
  }

  public void loadPrevious(BulbState bs) {
    if (bs.xy != null) {
      hs.xy = bs.xy;

      Float[] hueSat = Utils.xyTOhs(hs.xy);
      float[] hsv = {hueSat[0] * 360, hueSat[1], 1f};

      int rgb = Color.HSVToColor(hsv);

      picker.setColor(rgb);

      saturationBar.setSaturation(hsv[1]);
    }
  }

  public void onStart() {
    super.onStart();
    picker.setOnColorChangedListener(this);
    if (colorLoopLayoutVisible)
      colorLoop.setOnCheckedChangeListener(this);
  }

  public void hideColorLoop() {
    colorLoopLayoutVisible = false;
    colorLoop = null;
    if (colorLoopLayout != null)
      colorLoopLayout.setVisibility(View.GONE);
  }

  public void preview() {
    if (isAdded()) {
      MoodExecuterService service = ((NetworkManagedActivity) this.getActivity()).getService();
      if (service != null) {
        DeviceManager dm = service.getDeviceManager();
        Group g = dm.getSelectedGroup();
        for (Long bulbId : g.getNetworkBulbDatabaseIds()) {
          dm.getNetworkBulb(bulbId).setState(hs);
        }
      }
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    if (isChecked)
      hs.effect = "colorloop";
    else
      hs.effect = "none";
    preview();
  }

  @Override
  public void onColorChanged(int rgb) {
    float[] hsv = new float[3];
    int red = ((rgb >>> 16) & 0xFF);
    int green = ((rgb >>> 8) & 0xFF);
    int blue = ((rgb) & 0xFF);
    Color.RGBToHSV(red, green, blue, hsv);

    Float[] input = {hsv[0] / 360f, hsv[1]};
    hs.xy = Utils.hsTOxy(input);
    preview();
  }
}
