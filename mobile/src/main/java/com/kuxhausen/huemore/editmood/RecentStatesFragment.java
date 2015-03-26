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

import java.util.ArrayList;
import java.util.HashSet;

public class RecentStatesFragment extends Fragment implements
                                                   EditStateDialogFragment.StateSelector,
                                                   OnClickListener {

  private BulbState mInitialState;
  private GridView mGrid;
  private StateCellAdapter mCellAdapter;
  private int mLastSelectedPosition = -1;
  private ArrayList<StateCell> mList;
  private EditStateDialogFragment mParent;

  @Override
  public void initialize(EditStateDialogFragment statePage, BulbState initialState) {
    mParent = statePage;
    loadPrevious(mParent.getStateGridFragment().moodRows);
    mInitialState = initialState;
  }

  private void loadPrevious(ArrayList<StateRow> rows) {
    mList = extractUniques(rows);
  }

  public static ArrayList<StateCell> extractUniques(ArrayList<StateRow> rows) {
    ArrayList<StateCell> list = new ArrayList<StateCell>();
    HashSet<String> bulbStateHash = new HashSet<String>();
    for (StateRow row : rows) {
      for (StateCell cell : row.cellRay) {
        StateCell localCopy = cell.clone();
        if (!bulbStateHash.contains(localCopy.hs.toString())
            && (!localCopy.hs.isEmpty())) {
          bulbStateHash.add(localCopy.hs.toString());
          list.add(localCopy);
        }
      }
    }
    return list;
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
    mParent.setStateIfVisible(mList.get(mLastSelectedPosition).hs, this,
                              EditStatePager.RECENT_PAGE);
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
      if (mList.get(i).hs.toString().equals(newState.toString())) {
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
