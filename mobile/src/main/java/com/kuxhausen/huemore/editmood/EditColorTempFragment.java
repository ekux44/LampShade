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
                                                               EditStateDialogFragment.StateSelector {

  private final static int SEEK_BAR_OFFSET = 2000;

  private BulbState mBulbState;
  private SeekBar mSeekBar;
  private EditText mTempEditText;
  private EditStateDialogFragment mParent;

  @Override
  public void initialize(EditStateDialogFragment statePage, BulbState initialState) {
    mParent = statePage;

    mBulbState = new BulbState();
    mBulbState.setOn(true);
    mBulbState.setEffect(Effect.NONE);
    if (initialState.getMiredCT() != null) {
      mBulbState.setMiredCT(initialState.getMiredCT());
    } else {
      mBulbState.setKelvinCT(4000);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View groupDialogView = inflater.inflate(R.layout.colortemp_state_fragment, null);

    mSeekBar = (SeekBar) groupDialogView.findViewById(R.id.temperatureBar);

    mSeekBar.setOnSeekBarChangeListener(this);

    mTempEditText = (EditText) groupDialogView.findViewById(R.id.temperatureText);

    mTempEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          int temp;
          try {
            temp = Integer.parseInt((mTempEditText.getText().toString()));
          } catch (NumberFormatException e) {
            temp = Integer.MAX_VALUE;
          }
          temp = Math.max(temp, SEEK_BAR_OFFSET);
          temp = Math.min(temp, SEEK_BAR_OFFSET + mSeekBar.getMax());
          mTempEditText.setText(temp + "");
          mSeekBar.setProgress(temp - SEEK_BAR_OFFSET);
          mBulbState.setKelvinCT(temp);
          mParent
              .setStateIfVisible(mBulbState, EditColorTempFragment.this, EditStatePager.TEMP_PAGE);
        }
        return false;
      }

    });

    stateChanged(mBulbState);
    return groupDialogView;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      mBulbState.setKelvinCT(SEEK_BAR_OFFSET + seekBar.getProgress());
      mTempEditText.setText("" + (SEEK_BAR_OFFSET + seekBar.getProgress()));
      mParent.setStateIfVisible(mBulbState, this, EditStatePager.TEMP_PAGE);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void stateChanged(BulbState newState) {
    if (newState.getMiredCT() != null) {
      mBulbState.setMiredCT(newState.getMiredCT());
      mSeekBar.setProgress(mBulbState.getKelvinCT() - SEEK_BAR_OFFSET);
      mTempEditText.setText(mBulbState.getKelvinCT() + "");
    }
  }

  @Override
  public BulbState getState() {
    return mBulbState;
  }
}
