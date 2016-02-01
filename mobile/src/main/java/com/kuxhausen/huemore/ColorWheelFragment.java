package com.kuxhausen.huemore;

import com.google.gson.Gson;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.kuxhausen.huemore.net.BrightnessManager;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Effect;
import com.kuxhausen.huemore.state.Group;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;

public class ColorWheelFragment extends Fragment implements OnCheckedChangeListener,
                                                            com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener {

  private static Gson gson = new Gson();
  private CompoundButton mColorLoop;
  private ColorPicker mPicker;
  private SaturationBar mSaturationBar;
  private BulbState hs = new BulbState();

  {
    hs.setOn(true);
    hs.setEffect(Effect.NONE);
    hs.setXY(new float[]{.5f, .5f});// TODO change
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.colorwheel_mood_fragment, null);

    mPicker = (ColorPicker) groupDialogView.findViewById(R.id.picker);
    mSaturationBar = (SaturationBar) groupDialogView.findViewById(R.id.saturationbar);
    mPicker.addSaturationBar(mSaturationBar);
    mPicker.setShowOldCenterColor(false);

    Bundle args = getArguments();
    if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
      BulbState bs =
          gson.fromJson(args.getString(InternalArguments.PREVIOUS_STATE), BulbState.class);
      loadPrevious(bs);
    }
    loadPrevious(hs);

    mColorLoop = (CompoundButton) groupDialogView.findViewById(R.id.colorLoopCompoundButton);

    return groupDialogView;
  }

  public void loadPrevious(BulbState bs) {
    if (bs.hasXY()) {
      hs.setXY(bs.getXY());

      float[] hueSat = Utils.xyTOhs(hs.getXY());
      float[] hsv = {hueSat[0] * 360, hueSat[1], 1f};

      int rgb = Color.HSVToColor(hsv);

      mPicker.setColor(rgb);

      mSaturationBar.setSaturation(hsv[1]);
    }
  }

  public void onStart() {
    super.onStart();
    mPicker.setOnColorChangedListener(this);
    mColorLoop.setOnCheckedChangeListener(this);
  }

  public void preview() {
    if (isAdded()) {
      ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();
      if (service != null) {
        DeviceManager dm = service.getDeviceManager();
        Group g = dm.getSelectedGroup();
        if (g != null) {
          BrightnessManager briManager = dm.obtainBrightnessManager(g);
          for (Long bulbId : g.getNetworkBulbDatabaseIds()) {
            if (dm.getNetworkBulb(bulbId) != null) {
              briManager.setState(dm.getNetworkBulb(bulbId), hs);
            }
          }
        }
      }
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    if (isChecked) {
      hs.setEffect(Effect.COLORLOOP);
    } else {
      hs.setEffect(Effect.NONE);
    }
    preview();
  }

  @Override
  public void onColorChanged(int rgb) {
    float[] hsv = new float[3];
    int red = ((rgb >>> 16) & 0xFF);
    int green = ((rgb >>> 8) & 0xFF);
    int blue = ((rgb) & 0xFF);
    Color.RGBToHSV(red, green, blue, hsv);

    float[] input = {hsv[0] / 360f, hsv[1]};
    hs.setXY(Utils.hsTOxy(input));
    preview();
  }
}
