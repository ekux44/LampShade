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
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Effect;

public class EditColorTempFragment extends Fragment implements OnSeekBarChangeListener,
                                                               EditStatePagerDialogFragment.OnStateChangedListener {

  private BulbState hs = new BulbState();

  {
    hs.setOn(true);
    hs.setEffect(Effect.NONE);

    hs.setKelvinCT(4000);
  }

  SeekBar seekBar;
  EditText tempEditText;
  final int seekBarOffset = 2000;
  EditStatePagerDialogFragment statePager;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.colortemp_state_fragment, null);

    seekBar = (SeekBar) groupDialogView.findViewById(R.id.temperatureBar);

    seekBar.setProgress(hs.getKelvinCT() - seekBarOffset);

    seekBar.setOnSeekBarChangeListener(this);

    tempEditText = (EditText) groupDialogView.findViewById(R.id.temperatureText);
    tempEditText.setVisibility(View.VISIBLE);

    tempEditText.setText(hs.getKelvinCT() + "");

    tempEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          int temp;
          try {
            temp = Integer.parseInt((tempEditText.getText().toString()));
          } catch (NumberFormatException e) {
            temp = Integer.MAX_VALUE;
          }
          temp = Math.max(temp, seekBarOffset);
          temp = Math.min(temp, seekBarOffset + seekBar.getMax());
          tempEditText.setText(temp + "");
          seekBar.setProgress(temp - seekBarOffset);
          hs.setKelvinCT(temp);
          statePager.setStateIfVisible(hs, EditColorTempFragment.this, EditStatePager.TEMP_PAGE);
        }
        return false;
      }

    });

    return groupDialogView;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      hs.setKelvinCT(seekBarOffset + seekBar.getProgress());
      tempEditText.setText("" + (seekBarOffset + seekBar.getProgress()));
      statePager.setStateIfVisible(hs, this, EditStatePager.TEMP_PAGE);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public boolean stateChanged() {
    if (statePager.getState().getMiredCT() != null) {
      hs.setMiredCT(statePager.getState().getMiredCT());
      return true;
    }
    return false;
  }

  @Override
  public void setStatePager(EditStatePagerDialogFragment statePage) {
    statePager = statePage;
  }

}
