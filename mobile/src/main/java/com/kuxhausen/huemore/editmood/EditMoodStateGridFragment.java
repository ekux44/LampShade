package com.kuxhausen.huemore.editmood;

import com.google.gson.Gson;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayout;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.editmood.StateGridSelections.StateGridDisplay;
import com.kuxhausen.huemore.net.ConnectivityService;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.BulbState;
import com.kuxhausen.huemore.state.BulbState.Alert;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;

import java.util.ArrayList;

public class EditMoodStateGridFragment extends Fragment implements OnClickListener,
                                                                   OnCreateMoodListener,
                                                                   StateGridDisplay {

  EditMoodFragment parentFrag;

  Gson gson = new Gson();
  private GridLayout grid;
  public ArrayList<StateRow> moodRows = new ArrayList<StateRow>();
  private RelativeStartTimeslot loopTimeslot;
  private ImageButton addChannel, addTimeslot;
  private ArrayList<ImageButton> channelButtons = new ArrayList<ImageButton>();
  private String priorName;
  private PageType pageType = PageType.SIMPLE_PAGE;
  private CellOnLongClickListener mCellLongListener = new CellOnLongClickListener(this,
                                                                                  ViewType.StateCell);
  CellOnDragListener mCellDragListener;
  private CellOnLongClickListener mChannelLongListener = new CellOnLongClickListener(this,
                                                                                     ViewType.Channel);
  CellOnDragListener mChannelDragListener;
  private CellOnLongClickListener mTimeslotLongListener = new CellOnLongClickListener(this,
                                                                                      ViewType.Timeslot);
  CellOnDragListener mTimeslotDragListener;


  ActionMode mActionMode;
  StateGridSelections mStateGrid;

  public enum PageType {
    SIMPLE_PAGE, RELATIVE_PAGE, DAILY_PAGE
  }

  public void setMoodMode(int spinnerPos) {
    if (pageType.ordinal() != spinnerPos) {
      if (spinnerPos == PageType.SIMPLE_PAGE.ordinal()) {
        setGridRows(1);
      }

      pageType = PageType.values()[spinnerPos];
      redrawGrid();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View myView = inflater.inflate(R.layout.edit_mood_state_grid_fragment, null);

    mCellDragListener = new CellOnDragListener(this, ViewType.StateCell);
    mChannelDragListener = new CellOnDragListener(this, ViewType.Channel);
    mTimeslotDragListener = new CellOnDragListener(this, ViewType.Timeslot);

    mStateGrid = new StateGridSelections(this);

    addTimeslot = (ImageButton) inflater.inflate(R.layout.edit_mood_down_arrow, null);
    addTimeslot.setOnClickListener(this);

    addChannel = (ImageButton) inflater.inflate(R.layout.edit_mood_right_arrow, null);
    addChannel.setOnClickListener(this);

    grid = (GridLayout) myView.findViewById(R.id.advancedGridLayout);
    grid.removeAllViews();
    moodRows.clear();
    channelButtons.clear();
    grid.setColumnCount(initialCols + 1 + endingCols);
    grid.setRowCount(initialRows + endingRows);

    addRow();

    loopTimeslot = new RelativeStartTimeslot(this, 0);

    Bundle args = getArguments();
    if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {

      // load prior mood
      priorName = args.getString(InternalArguments.MOOD_NAME);
      loadMood(Utils.getMoodFromDatabase(priorName, this.getActivity()));
    }

    redrawGrid();

    return myView;
  }

  public void setParentFragment(EditMoodFragment frag) {
    parentFrag = frag;
  }

  public int getGridWidth() {
    if (grid != null) {
      return grid.getWidth();
    }
    return 0;
  }

  public int getGridHeight() {
    if (grid != null) {
      return grid.getHeight();
    }
    return 0;
  }

  public void validate() {
    for (StateRow s : moodRows) {
      s.dailyTimeslot.validate();
      s.relativeTimeslot.validate();
    }
    loopTimeslot.validate();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    getCell(new Pair<Integer, Integer>(data.getIntExtra(InternalArguments.ROW, -1),
                                       data.getIntExtra(InternalArguments.COLUMN, -1))).hs =
        gson.fromJson(data.getStringExtra(InternalArguments.HUE_STATE), BulbState.class);

    redrawGrid();
  }

  private void stopPreview() {
    ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();
    if (service.getDeviceManager().getSelectedGroup() != null) {
      service.getMoodPlayer().cancelMood(service.getDeviceManager().getSelectedGroup());
    }
    //else there is nothing relevant to stop
  }

  @Override
  public void preview() {
    String name = parentFrag.getName();
    if (name == null || name.length() < 1) {
      name = parentFrag.getString(R.string.hint_mood_name);
    }

    ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();
    if (service.getDeviceManager().getSelectedGroup() != null) {
      service.getMoodPlayer().playMood(service.getDeviceManager().getSelectedGroup(), getMood(),
                                       name, null);
    } else {
      //TODO maybe toast to warn no group selected
    }
  }

  public static PageType calculateMoodType(Mood m) {
    if (!m.getUsesTiming()) {
      return PageType.SIMPLE_PAGE;
    } else if (m.getTimingPolicy() == Mood.TimingPolicy.DAILY) {
      return PageType.DAILY_PAGE;
    } else {
      return PageType.RELATIVE_PAGE;
    }
  }


  private void loadMood(Mood mFromDB) {
    // calculate & set the mood type
    pageType = EditMoodStateGridFragment.calculateMoodType(mFromDB);

    // calculate & set the number of grid rows
    this.setGridCols(mFromDB.getNumChannels());

    // calculate & set number of rows, and fill with times
    int rows = 0;
    int time = -1;
    for (Event e : mFromDB.getEvents()) {
      if (e.getLegacyTime() != time) {
        rows++;
        time = e.getLegacyTime();
      }
    }
    setGridRows(Math.max(rows, 1));

    int row = -1;
    time = -1;
    for (Event e : mFromDB.getEvents()) {
      if (e.getLegacyTime() != time) {
        row++;
        time = e.getLegacyTime();
        if (pageType == PageType.DAILY_PAGE || pageType == PageType.RELATIVE_PAGE) {
          moodRows.get(row).dailyTimeslot.setStartTime(e.getLegacyTime());
          moodRows.get(row).relativeTimeslot.setStartTime(e.getLegacyTime());
        }
      }
      moodRows.get(row).cellRay.get(e.getChannel()).hs = e.getBulbState();
    }

    // set loop button
    parentFrag.setChecked(mFromDB.getTimingPolicy() == Mood.TimingPolicy.LOOPING);

    loopTimeslot.setStartTime(Utils.toDeciSeconds(mFromDB.getLoopMilliTime()));

    redrawGrid();
  }

  protected Mood getMood() {
    Mood.Builder moodBuilder = new Mood.Builder();
    if (pageType == PageType.DAILY_PAGE || pageType == PageType.RELATIVE_PAGE) {
      moodBuilder.setUsesTiming(true);
    } else {
      moodBuilder.setUsesTiming(false);
    }
    moodBuilder.setNumChannels(gridCols());
    if (pageType == PageType.DAILY_PAGE) {
      moodBuilder.setTimingPolicy(Mood.TimingPolicy.DAILY);
    } else if (pageType == PageType.RELATIVE_PAGE && parentFrag.isChecked()) {
      moodBuilder.setTimingPolicy(Mood.TimingPolicy.LOOPING);
    } else {
      moodBuilder.setTimingPolicy(Mood.TimingPolicy.BASIC);
    }

    ArrayList<Event> events = new ArrayList<Event>();
    for (int r = 0; r < moodRows.size(); r++) {
      for (int c = 0; c < moodRows.get(r).cellRay.size(); c++) {
        StateCell mr = moodRows.get(r).cellRay.get(c);
        if (mr.hs != null && !mr.hs.isEmpty()) {
          Event e = new Event(mr.hs, c, getTime(r));
          events.add(e);
        }
      }
    }
    Event[] eRay = new Event[events.size()];
    for (int i = 0; i < eRay.length; i++) {
      eRay[i] = events.get(i);
    }

    moodBuilder.setEvents(eRay);
    moodBuilder.setLoopMilliTime(Utils.fromDeciSeconds(loopTimeslot.getStartTime()));
    return moodBuilder.build();
  }

  /**
   * @return time in milliseconds
   */
  private long getTime(int row) {
    if (row > -1 && row < moodRows.size()) {
      if (pageType == PageType.DAILY_PAGE) {
        return Utils.fromDeciSeconds(moodRows.get(row).dailyTimeslot.getStartTime());
      } else if (pageType == PageType.RELATIVE_PAGE) {
        return Utils.fromDeciSeconds(moodRows.get(row).relativeTimeslot.getStartTime());
      }
    }
    return 0;
  }

  /**
   * compute Minimum Value at my position *
   */
  public int computeMinimumValue(int position) {
    position = Math.min(position, moodRows.size());
    if (position <= 0) {
      return 0;
    } else {
      if (pageType == PageType.DAILY_PAGE) {
        return moodRows.get(position - 1).dailyTimeslot.getStartTime() + 600;
      } else if (pageType == PageType.RELATIVE_PAGE) {
        return moodRows.get(position - 1).relativeTimeslot.getStartTime() + 10;
      }
    }
    return 0;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.clickable_layout:
        stopPreview();
        mStateGrid.setStateSelectionByTag(v);
        EditStateDialogFragment cpdf = new EditStateDialogFragment();
        cpdf.setEditMoodFrag(this);
        Bundle args = new Bundle();
        args.putString(InternalArguments.PREVIOUS_STATE,
                       gson.toJson(this.getCell(mStateGrid.getSelectedCellRowCol()).hs));
        args.putInt(InternalArguments.ROW, mStateGrid.getSelectedCellRow());
        args.putInt(InternalArguments.COLUMN, mStateGrid.getSelectedCellCol());
        cpdf.setArguments(args);
        cpdf.setTargetFragment(this, -1);
        cpdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        break;
      case R.id.rightButton:
        addCol();
        redrawGrid();
        break;
      case R.id.downButton:
        addRow();
        redrawGrid();
        break;
      case R.id.infoImageButton:
        Mood.Builder moodBuilder = new Mood.Builder();
        moodBuilder.setUsesTiming(false);
        moodBuilder.setNumChannels(gridCols());

        int channelToFlash = (Integer) v.getTag();
        BulbState bs = new BulbState();
        bs.setAlert(Alert.FLASH_ONCE);
        bs.setOn(true);

        Event e = new Event(bs, channelToFlash, 0l);
        Event[] eRay = {e};
        moodBuilder.setEvents(eRay);

        Mood showChanM = moodBuilder.build();

        ConnectivityService service = ((NetworkManagedActivity) this.getActivity()).getService();
        if (service.getDeviceManager().getSelectedGroup() != null) {
          service.getMoodPlayer()
              .playMood(service.getDeviceManager().getSelectedGroup(), showChanM, null, null);
        } else {
          //TODO maybe toast to warn no group selected
        }
        break;
    }

  }

  public void switchCells(Pair<Integer, Integer> first, Pair<Integer, Integer> second) {
    StateCell temp = moodRows.get(first.first).cellRay.get(first.second);
    moodRows.get(first.first).cellRay.set(first.second,
                                          moodRows.get(second.first).cellRay.get(second.second));
    moodRows.get(second.first).cellRay.set(second.second, temp);
    redrawGrid();
  }

  public void deleteTimeslot(int position) {
    if (moodRows.size() > 1) {
      moodRows.remove(position);
      grid.setRowCount(initialRows + endingRows + gridRows() - 1);
      redrawGrid();
    }
  }

  public void insertionMoveTimeslot(int oldPos, int newPos) {
    StateRow temp = moodRows.get(oldPos);
    // always act on highest position first for corectness
    if (newPos > oldPos) {
      moodRows.add(newPos, temp);
      moodRows.remove(oldPos);
    } else if (newPos < oldPos) {
      moodRows.remove(oldPos);
      moodRows.add(newPos, temp);
    }
    redrawGrid();
  }

  public void deleteChannel(int position) {
    if (moodRows.get(0).cellRay.size() > 1) {
      for (StateRow sr : moodRows) {
        sr.cellRay.remove(position);
      }
      grid.setColumnCount(initialCols + endingCols + gridCols() - 1);
      redrawGrid();
    }
  }

  public void insertionMoveChannel(int oldPos, int newPos) {
    for (StateRow sr : moodRows) {
      StateCell temp = sr.cellRay.get(oldPos);
      // always act on highest position first for corectness
      if (newPos > oldPos) {
        sr.cellRay.add(newPos, temp);
        sr.cellRay.remove(oldPos);
      } else if (newPos < oldPos) {
        sr.cellRay.remove(oldPos);
        sr.cellRay.add(newPos, temp);
      }
    }
    redrawGrid();
  }

  public void redrawGrid() {
    if (grid == null) {
      return;
    }
    grid.removeAllViews();
    LayoutInflater inflater = this.getActivity().getLayoutInflater();
    for (int r = 0; r < moodRows.size(); r++) {
      for (int c = 0; c < moodRows.get(r).cellRay.size(); c++) {
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(c + initialCols);
        vg.rowSpec = GridLayout.spec(r + initialRows);
        View v =
            moodRows.get(r).cellRay.get(c)
                .getView(
                    grid,
                    this,
                    this,
                    mCellLongListener
                );
        v.setOnDragListener(mCellDragListener);
        mStateGrid.tagStateCell(v, r, c);
        grid.addView(v, vg);
      }
    }
    int gridStateRows = this.gridRows();
    int gridStateCols = this.gridCols();

    // add timeslot label
    if (pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE) {
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(0);
      vg.rowSpec = GridLayout.spec(initialRows + gridStateRows + endingRows - 1);
      vg.setGravity(Gravity.CENTER);
      grid.addView(addTimeslot, vg);
    }
    // add channel label
    {
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(initialCols + gridStateCols + endingCols - 1);
      vg.rowSpec = GridLayout.spec(1);
      vg.setGravity(Gravity.CENTER);
      grid.addView(addChannel, vg);
    }
    // add channel buttons
    {
      for (int c = 0; c < moodRows.get(0).cellRay.size(); c++) {
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(initialCols + c);
        vg.rowSpec = GridLayout.spec(1);
        vg.setGravity(Gravity.CENTER);
        if (channelButtons.size() <= c) {
          channelButtons.add((ImageButton) inflater.inflate(R.layout.info_image_button, null));
        }
        View v = channelButtons.get(c);
        if (v.getParent() != null) {
          ((ViewGroup) v.getParent()).removeView(v);
        }
        v.setOnClickListener(this);
        v.setOnLongClickListener(mChannelLongListener);
        v.setOnDragListener(mChannelDragListener);
        mStateGrid.tagChannel(v, c);
        grid.addView(v, vg);
      }
    }

    // timedTimeslotDuration views
    if (pageType == PageType.RELATIVE_PAGE) {
      for (int r = 0; r < moodRows.size(); r++) {
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(0);
        vg.rowSpec = GridLayout.spec(r + initialRows);
        vg.setGravity(Gravity.CENTER);

        View v = moodRows.get(r).relativeTimeslot.getView(r);
        if (v.getParent() != null) {
          ((ViewGroup) v.getParent()).removeView(v);
        }
        v.setOnLongClickListener(mTimeslotLongListener);
        v.setOnDragListener(mTimeslotDragListener);
        mStateGrid.tagTimeslot(v, r);
        grid.addView(v, vg);
      }
    }
    // dailytimeslotDuration views
    if (pageType == PageType.DAILY_PAGE) {
      for (int r = 0; r < moodRows.size(); r++) {
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(0);
        vg.rowSpec = GridLayout.spec(r + initialRows);
        vg.setGravity(Gravity.CENTER);

        View v = moodRows.get(r).dailyTimeslot.getView(r);
        if (v.getParent() != null) {
          ((ViewGroup) v.getParent()).removeView(v);
        }
        v.setOnLongClickListener(mTimeslotLongListener);
        v.setOnDragListener(mTimeslotDragListener);
        mStateGrid.tagTimeslot(v, r);
        grid.addView(v, vg);
      }
    }

    // channel label
    {
      View v = inflater.inflate(R.layout.grid_col_channels_label, null);
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
      vg.rowSpec = GridLayout.spec(0);
      vg.setGravity(Gravity.CENTER);
      grid.addView(v, vg);
    }
    // timeslot label
    if (pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE) {
      View v = null;
      if (pageType == PageType.RELATIVE_PAGE) {
        v = inflater.inflate(R.layout.grid_col_timed_timeslot_label, null);
      } else if (pageType == PageType.DAILY_PAGE) {
        v = inflater.inflate(R.layout.grid_col_daily_timeslot_label, null);
      }
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(0);
      vg.rowSpec = GridLayout.spec(0);
      vg.setGravity(Gravity.CENTER);
      grid.addView(v, vg);
    }
    // vertical separator
    if (pageType == PageType.RELATIVE_PAGE || pageType == PageType.DAILY_PAGE) {
      ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_vertical_seperator, null);

      ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
      rowView.setImageDrawable(cd);
      rowView.setMinimumWidth(1);
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(1);
      vg.rowSpec = GridLayout.spec(0, initialRows + gridStateRows);
      vg.setGravity(Gravity.FILL_VERTICAL);

      grid.addView(rowView, vg);
    }
    // horizontal separator
    {
      ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_horizontal_seperator, null);

      ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
      rowView.setImageDrawable(cd);
      rowView.setMinimumHeight(1);
      GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
      vg.columnSpec = GridLayout.spec(0, initialCols + gridStateCols);
      vg.rowSpec = GridLayout.spec(2);
      vg.setGravity(Gravity.FILL_HORIZONTAL);

      grid.addView(rowView, vg);
    }
    // loop related stuff
    if (pageType == PageType.RELATIVE_PAGE && parentFrag.isChecked()) {
      // loop banner that sits beside loop timeslot

      {
        View v = inflater.inflate(R.layout.grid_timeslot_loop_label, null);
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(initialCols, gridStateCols);
        vg.rowSpec = GridLayout.spec(initialRows + gridStateRows + endingRows);
        vg.setGravity(Gravity.CENTER);
        grid.addView(v, vg);
      }

      // loop timeslot view
      {
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(0);
        vg.rowSpec = GridLayout.spec(initialRows + gridStateRows + endingRows);
        vg.setGravity(Gravity.CENTER);

        View v = loopTimeslot.getView(moodRows.size());
        if (v.getParent() != null) {
          ((ViewGroup) v.getParent()).removeView(v);
        }

        grid.addView(v, vg);
      }
      // vertical separator extended down to loop
      {
        ImageView rowView = (ImageView) inflater.inflate(R.layout.grid_vertical_seperator, null);

        ColorDrawable cd = new ColorDrawable(0xFFB5B5E5);
        rowView.setImageDrawable(cd);
        rowView.setMinimumWidth(1);
        GridLayout.LayoutParams vg = new GridLayout.LayoutParams();
        vg.columnSpec = GridLayout.spec(1);
        vg.rowSpec = GridLayout.spec(initialRows + gridStateRows + endingRows);
        vg.setGravity(Gravity.FILL_VERTICAL);

        grid.addView(rowView, vg);
      }
    }

    grid.invalidate();
  }


  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    mStateGrid.setStateSelectionByTag(v);

    android.view.MenuInflater inflater = this.getActivity().getMenuInflater();
    inflater.inflate(R.menu.context_state, menu);

    android.view.MenuItem deleteTimeslot = menu.findItem(R.id.contextstatemenu_delete_timeslot);
    if (pageType == PageType.SIMPLE_PAGE) {
      deleteTimeslot.setEnabled(false);
      deleteTimeslot.setVisible(false);
    }
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case R.id.contextstatemenu_edit:
        stopPreview();
        EditStateDialogFragment cpdf = new EditStateDialogFragment();
        cpdf.setEditMoodFrag(this);
        Bundle args = new Bundle();
        args.putString(InternalArguments.PREVIOUS_STATE,
                       gson.toJson(getCell(mStateGrid.getSelectedCellRowCol()).hs));
        args.putInt(InternalArguments.ROW, mStateGrid.getSelectedCellRow());
        args.putInt(InternalArguments.COLUMN, mStateGrid.getSelectedCellCol());
        cpdf.setArguments(args);
        cpdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
        return true;
      case R.id.contextstatemenu_delete:
        deleteCell(mStateGrid.getSelectedCellRowCol());
        return true;
      case R.id.contextstatemenu_delete_timeslot:
        deleteRow(mStateGrid.getSelectedCellRow());
        redrawGrid();
        return true;
      case R.id.contextstatemenu_delete_channel:
        deleteCol(mStateGrid.getSelectedCellCol());
        redrawGrid();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  public StateCell getCell(Pair<Integer, Integer> tag) {
    int r = tag.first;
    int c = tag.second;
    return moodRows.get(r).cellRay.get(c);
  }

  public void deleteCell(Pair<Integer, Integer> tag) {
    getCell(tag).hs = new BulbState();
    redrawGrid();
  }

  private void deleteRow(int row) {
    if (row > -1 && row < moodRows.size() && moodRows.size() > 1) {
      moodRows.remove(row);
      grid.setRowCount(initialRows + endingRows + gridRows() - 1);
      redrawGrid();
    }
  }

  private void deleteCol(int col) {
    if (col > -1 && !moodRows.isEmpty() && col < moodRows.get(0).cellRay.size()
        && moodRows.get(0).cellRay.size() > 1) {
      for (StateRow sr : moodRows) {
        sr.cellRay.remove(col);
      }
      grid.setColumnCount(endingCols + initialCols + gridCols() - 1);
    }
    redrawGrid();
  }

  private void addRow() {
    if (gridRows() <= 64) {
      grid.setRowCount(initialRows + endingRows + gridRows() + 1);

      StateRow newRow = new StateRow();
      for (int i = gridCols(); i > 0; i--) {
        newRow.cellRay.add(new StateCell(this.getActivity()));
      }
      newRow.dailyTimeslot = new TimeOfDayTimeslot(this, gridRows() - 1);
      newRow.relativeTimeslot = new RelativeStartTimeslot(this, gridRows() - 1);

      moodRows.add(newRow);
    } else {
      Toast t = Toast.makeText(getActivity(), R.string.advanced_timeslot_limit, Toast.LENGTH_LONG);
      t.show();
    }
  }

  private void addCol() {
    if (gridCols() < 64) {
      int width = gridCols();
      grid.setColumnCount(1 + width + initialCols + endingCols);
      for (StateRow sr : moodRows) {
        sr.cellRay.add(new StateCell(this.getActivity()));
      }
    } else {
      Toast t = Toast.makeText(getActivity(), R.string.advanced_channel_limit, Toast.LENGTH_LONG);
      t.show();
    }
  }

  private final int initialRows = 3;
  private final int initialCols = 2;
  private final int endingRows = 2;
  private final int endingCols = 1;

  private final int gridRows() {
    return grid.getRowCount() - initialRows - endingRows;
  }

  private final int gridCols() {
    return grid.getColumnCount() - initialCols - endingCols;
  }

  private final void setGridRows(int num) {
    while (gridRows() != num) {
      if (gridRows() < num) {
        addRow();
      } else if (gridRows() > num) {
        deleteRow(gridRows() - 1);
      }
    }
  }

  private final void setGridCols(int num) {
    while (gridCols() != num) {
      if (gridCols() < num) {
        addCol();
      } else if (gridCols() > num) {
        deleteCol(gridCols() - 1);
      }
    }
  }

  @Override
  public PageType getPageType() {
    return pageType;
  }
}
