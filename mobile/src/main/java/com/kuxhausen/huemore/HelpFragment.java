package com.kuxhausen.huemore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class HelpFragment extends Fragment implements OnNavigationListener {

  private TextView mSelected;
  private String[] mPages, mTitles;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View myView = inflater.inflate(R.layout.help_fragment, container, false);

    mSelected = (TextView) myView.findViewById(R.id.helpText);

    mTitles = getResources().getStringArray(R.array.help_page_titles);
    mPages = getResources().getStringArray(R.array.help_page_content);

    ActionBar aBar = ((ActionBarActivity) this.getActivity()).getSupportActionBar();

    Context context = aBar.getThemedContext();
    ArrayAdapter<CharSequence> list =
        ArrayAdapter.createFromResource(context, R.array.help_page_titles,
            android.R.layout.simple_spinner_item);
    list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


    aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    aBar.setListNavigationCallbacks(list, this);
    aBar.setTitle(R.string.action_help);
    aBar.setDisplayHomeAsUpEnabled(true);

    Bundle args = this.getArguments();
    if (args != null && args.containsKey(InternalArguments.HELP_PAGE)) {
      String desiredPageTitle = args.getString(InternalArguments.HELP_PAGE);
      for (int position = 0; position < mTitles.length; position++) {
        if (desiredPageTitle.equals(mTitles[position]))
          aBar.setSelectedNavigationItem(position);
      }
    }
    return myView;
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    mSelected.setText(mPages[itemPosition]);
    return true;
  }
}
