package com.kuxhausen.huemore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

public class HelpActivity extends ActionBarActivity implements OnNavigationListener  {

	private TextView mSelected;
    private String[] mPages, mTitles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help_fragment);
        mSelected = (TextView)findViewById(R.id.helpText);

        mTitles = getResources().getStringArray(R.array.help_page_titles);
        mPages = getResources().getStringArray(R.array.help_page_content);

        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.help_page_titles, android.R.layout.simple_spinner_item);
        list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        getSupportActionBar().setTitle(R.string.action_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Bundle args = this.getIntent().getExtras();
		if (args != null && args.containsKey(InternalArguments.HELP_PAGE)) {
			String desiredPageTitle = args.getString(InternalArguments.HELP_PAGE);
			for(int position = 0; position<mTitles.length; position++){
				if(desiredPageTitle.equals(mTitles[position]))
					getSupportActionBar().setSelectedNavigationItem(position);
			}
		}
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        mSelected.setText(mPages[itemPosition]);
        return true;
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this,MainActivity.class));
				return true;
		}
		return false;
    }

}
