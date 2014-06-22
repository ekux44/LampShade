package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.BulbState;

public class EditColorTempFragment extends Fragment implements OnSeekBarChangeListener,
    OnCreateColorListener {

  private BulbState hs = new BulbState();
  {
    hs.on = true;
    hs.effect = "none";

    hs.ct = 1000000 / 4000;
  }
  SeekBar seekBar;
  EditText tempEditText;
  final int seekBarOffset = 2000;
  EditStatePagerDialogFragment statePager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.colortemp_state_fragment, null);

    seekBar = (SeekBar) groupDialogView.findViewById(R.id.temperatureBar);

    seekBar.setProgress((1000000 / hs.ct) - seekBarOffset);

    seekBar.setOnSeekBarChangeListener(this);

    tempEditText = (EditText) groupDialogView.findViewById(R.id.temperatureText);
    tempEditText.setVisibility(View.VISIBLE);

    tempEditText.setText(hs.getCT());

    tempEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          int temp = Integer.parseInt((tempEditText.getText().toString()));
          temp = Math.max(temp, 0);
          temp = Math.min(temp, seekBarOffset + seekBar.getMax());
          seekBar.setProgress(temp - seekBarOffset);
          hs.ct = ((1000000 / temp));
          statePager.setState(hs, EditColorTempFragment.this, "ct");
        }
        return false;
      }

    });

    return groupDialogView;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      hs.ct = ((1000000 / (seekBarOffset + seekBar.getProgress())));
      tempEditText.setText("" + (seekBarOffset + seekBar.getProgress()));
      statePager.setState(hs, this, "ct");
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    hs.ct = ((1000000 / (seekBarOffset + seekBar.getProgress())));
    tempEditText.setText("" + (seekBarOffset + seekBar.getProgress()));
    statePager.setState(hs, this, "ct");
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    hs.ct = ((1000000 / (seekBarOffset + seekBar.getProgress())));
    tempEditText.setText("" + (seekBarOffset + seekBar.getProgress()));
    statePager.setState(hs, this, "ct");
  }

  @Override
  public boolean stateChanged() {
    if (statePager.getState().ct != null) {
      hs.ct = statePager.getState().ct;
      return true;
    }
    return false;
  }

  @Override
  public void setStatePager(EditStatePagerDialogFragment statePage) {
    statePager = statePage;
  }

}
