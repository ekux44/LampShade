package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;
import java.util.HashSet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;

public class RecentStatesFragment extends Fragment implements OnCreateColorListener,
    OnClickListener {

  private GridView g;
  private StateCellAdapter adapter;
  private int lastSelectedPosition = -1;
  private ArrayList<StateCell> list;
  private EditStatePagerDialogFragment statePager;

  public static ArrayList<StateCell> extractUniques(ArrayList<StateRow> rows) {
    ArrayList<StateCell> list = new ArrayList<StateCell>();
    HashSet<String> bulbStateHash = new HashSet<String>();
    for (StateRow row : rows) {
      for (StateCell cell : row.cellRay) {
        StateCell localCopy = cell.clone();
        if (!bulbStateHash.contains(localCopy.hs.toString())
            && (localCopy.hs.toString().length() > 0)) {
          bulbStateHash.add(localCopy.hs.toString());
          list.add(localCopy);
        }
      }
    }
    return list;
  }

  private void loadPrevious(ArrayList<StateRow> rows) {
    list = extractUniques(rows);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
  }

  @Override
  public void onClick(View v) {
    if (lastSelectedPosition > -1)
      list.get(lastSelectedPosition).selected = false;
    lastSelectedPosition = (Integer) v.getTag();
    list.get(lastSelectedPosition).selected = true;
    adapter.notifyDataSetChanged();
    statePager.setState(list.get(lastSelectedPosition).hs, this, "recent");
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
      if (adapter != null)
        adapter.notifyDataSetChanged();
    }
    if (newSelectedPosition != -1) {
      return true;
    }
    return false;
  }

  @Override
  public void setStatePager(EditStatePagerDialogFragment statePage) {
    statePager = statePage;
    loadPrevious(statePager.parrentMood.moodRows);
  }
}
