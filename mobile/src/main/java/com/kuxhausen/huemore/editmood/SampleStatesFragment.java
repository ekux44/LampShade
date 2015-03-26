package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Effect;

import java.util.ArrayList;

public class SampleStatesFragment extends Fragment implements
                                                   EditStateDialogFragment.StateSelector,
                                                   OnClickListener {

  private BulbState mInitialState;
  private GridView mGrid;
  private StateCellAdapter mCellAdapter;
  private int mLastSelectedPosition = -1;
  private EditStateDialogFragment mStatePager;
  private ArrayList<StateCell> mList;

  @Override
  public void initialize(EditStateDialogFragment statePage, BulbState initialState) {
    mStatePager = statePage;
    loadPresets();
    mInitialState = initialState;
  }

  private void loadPresets() {
    mList = new ArrayList<StateCell>();

    String[] simpleNames =
        {"Reading", "Relax", "Concentrate", "Energize", "Deep Sea1", "Deep Sea2", "Fruit1",
         "Fruit2", "Fruit3"};
    float[] simpleX =
        {0.4571f, 0.5119f, 0.368f, 0.3151f, 0.1859f, 0.6367f, 0.5089f, 0.5651f, 0.4081f};
    float[] simpleY =
        {0.4123f, 0.4147f, 0.3686f, 0.3252f, 0.0771f, 0.3349f, 0.438f, 0.3306f, 0.518f};

    for (int i = 0; i < simpleX.length; i++) {
      BulbState hs = new BulbState();
      float[] conversionXY = {simpleX[i], simpleY[i]};
      hs.setXY(conversionXY);
      hs.setOn(true);
      hs.setEffect(Effect.NONE);
      hs.setTransitionTime(BulbState.TRANSITION_TIME_DEFAULT);

      StateCell mr = new StateCell(mStatePager.getActivity());
      mr.hs = hs;
      mr.name = simpleNames[i];
      mList.add(mr);

    }
    {
      BulbState hs = new BulbState();
      hs.setOn(true);
      hs.setEffect(Effect.NONE);

      StateCell mr = new StateCell(mStatePager.getActivity());
      mr.hs = hs;
      mr.name = mStatePager.getActivity().getResources().getString(R.string.cap_on);
      mList.add(mr);
    }
    {
      BulbState hs = new BulbState();
      hs.setOn(false);
      hs.setEffect(Effect.NONE);

      StateCell mr = new StateCell(mStatePager.getActivity());
      mr.hs = hs;
      mr.name = mStatePager.getActivity().getResources().getString(R.string.cap_off);
      mList.add(mr);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.grid_view, null);
    mGrid = (GridView) myView.findViewById(R.id.myGrid);
    mCellAdapter = new StateCellAdapter(this, mList, this);
    mGrid.setAdapter(mCellAdapter);

    stateChanged(mInitialState);
    return myView;
  }

  @Override
  public void onClick(View v) {
    if (mLastSelectedPosition > -1) {
      mList.get(mLastSelectedPosition).selected = false;
    }
    mLastSelectedPosition = (Integer) v.getTag();
    mList.get(mLastSelectedPosition).selected = true;
    mCellAdapter.notifyDataSetChanged();
    mStatePager
        .setStateIfVisible(mList.get(mLastSelectedPosition).hs, this, EditStatePager.SAMPLE_PAGE);
  }


  @Override
  public BulbState getState() {
    if (mLastSelectedPosition > -1) {
      return mList.get(mLastSelectedPosition).hs;
    } else {
      return new BulbState();
    }
  }

  @Override
  public void stateChanged(BulbState newState) {
    int newSelectedPosition = -1;
    for (int i = 0; i < mList.size(); i++) {
      if (mList.get(i).hs.equals(newState)) {
        newSelectedPosition = i;
        mList.get(i).selected = true;
      } else {
        mList.get(i).selected = false;
      }
    }
    if (mLastSelectedPosition != newSelectedPosition) {
      mLastSelectedPosition = newSelectedPosition;
      if (mCellAdapter != null) {
        mCellAdapter.notifyDataSetChanged();
      }
    }
  }
}
