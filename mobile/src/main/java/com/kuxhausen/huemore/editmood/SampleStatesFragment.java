package com.kuxhausen.huemore.editmood;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Effect;

import java.util.ArrayList;

public class SampleStatesFragment extends Fragment implements OnCreateColorListener,
                                                              OnClickListener {

  private GridView g;
  private StateCellAdapter adapter;
  private int lastSelectedPosition = -1;
  private EditStatePagerDialogFragment statePager;
  private ArrayList<StateCell> list;

  private void loadPresets() {
    list = new ArrayList<StateCell>();

    String[] simpleNames =
        {"Reading", "Relax", "Concentrate", "Energize", "Deep Sea1", "Deep Sea2", "Fruit1",
         "Fruit2", "Fruit3"};
    float[] simpleX =
        {0.4571f, 0.5119f, 0.368f, 0.3151f, 0.1859f, 0.6367f, 0.5089f, 0.5651f, 0.4081f};
    float[] simpleY =
        {0.4123f, 0.4147f, 0.3686f, 0.3252f, 0.0771f, 0.3349f, 0.438f, 0.3306f, 0.518f};

    for (int i = 0; i < simpleX.length; i++) {
      BulbState hs = new BulbState();
      Float[] conversionXY = {simpleX[i], simpleY[i]};
      hs.xy = conversionXY;
      hs.setOn(true);
      hs.setEffect(Effect.NONE);
      hs.setTransitionTime(BulbState.TRANSITION_TIME_DEFAULT);

      StateCell mr = new StateCell(statePager.getActivity());
      mr.hs = hs;
      mr.name = simpleNames[i];
      list.add(mr);

    }
    {
      BulbState hs = new BulbState();
      hs.setOn(true);
      hs.setEffect(Effect.NONE);

      StateCell mr = new StateCell(statePager.getActivity());
      mr.hs = hs;
      mr.name = statePager.getActivity().getResources().getString(R.string.cap_on);
      list.add(mr);
    }
    {
      BulbState hs = new BulbState();
      hs.setOn(true);
      hs.setEffect(Effect.NONE);

      StateCell mr = new StateCell(statePager.getActivity());
      mr.hs = hs;
      mr.name = statePager.getActivity().getResources().getString(R.string.cap_off);
      list.add(mr);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View myView = inflater.inflate(R.layout.grid_view, null);
    g = (GridView) myView.findViewById(R.id.myGrid);
    adapter = new StateCellAdapter(this, list, this);
    g.setAdapter(adapter);

    return myView;
  }

  @Override
  public void onResume() {
    super.onResume();

    stateChanged();
    Log.e("lifecycle", "sample onResume");
  }

  @Override
  public void onClick(View v) {
    if (lastSelectedPosition > -1) {
      list.get(lastSelectedPosition).selected = false;
    }
    lastSelectedPosition = (Integer) v.getTag();
    list.get(lastSelectedPosition).selected = true;
    adapter.notifyDataSetChanged();
    statePager.setState(list.get(lastSelectedPosition).hs, this, "sample");
  }


  @Override
  public boolean stateChanged() {
    int newSelectedPosition = -1;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).hs.toString().equals(statePager.getState().toString())) {
        newSelectedPosition = i;
        list.get(i).selected = true;
      } else {
        list.get(i).selected = false;
      }
    }
    if (lastSelectedPosition != newSelectedPosition) {
      lastSelectedPosition = newSelectedPosition;
      if (adapter != null) {
        adapter.notifyDataSetChanged();
      }
    }
    if (newSelectedPosition != -1) {
      return true;
    }
    return false;
  }

  @Override
  public void setStatePager(EditStatePagerDialogFragment statePage) {
    statePager = statePage;
    loadPresets();
  }
}
