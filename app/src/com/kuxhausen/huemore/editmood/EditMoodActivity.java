package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.BulbListFragment;
import com.kuxhausen.huemore.ColorWheelFragment;
import com.kuxhausen.huemore.GroupListFragment;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.string;
import com.kuxhausen.huemore.network.BulbListSuccessListener.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditMoodActivity extends NetworkManagedSherlockFragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	EditMoodPagerAdapter mEditMoodPagerAdapter;
	static OnCreateMoodListener[] newMoodFragments;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	static int currentPage;

	private EditText nameEditText;
	static String priorName;
	static Mood priorMood;
	static Gson gson = new Gson();
	private CheckBox loop;
	
	public final static int TIMED_PAGE=0, DAILY_PAGE = 1;

	public interface OnCreateMoodListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onCreateMood(String groupname);
		
		public void preview();

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_mood_activity);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		//Inflate the custom view
		nameEditText = (EditText) LayoutInflater.from(this).inflate(R.layout.mood_name_edit_text, null);
		getSupportActionBar().setCustomView(nameEditText);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        
		loop = (CheckBox)this.findViewById(R.id.loopCheckBox);
		loop.setVisibility(View.GONE);
		
		mEditMoodPagerAdapter = new EditMoodPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mEditMoodPagerAdapter);
		mViewPager.setOffscreenPageLimit(4);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;
				if(loop!=null){
					if(currentPage == TIMED_PAGE){
						loop.setVisibility(View.VISIBLE);
					} else {
						loop.setVisibility(View.GONE);
					}
						
				}
				
			}

		});
		
		newMoodFragments = new OnCreateMoodListener[mEditMoodPagerAdapter.getCount()];

		Bundle args = this.getIntent().getExtras();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			String moodName = args.getString(InternalArguments.MOOD_NAME);
			priorName = moodName;
			nameEditText.setText(moodName);
			
			priorMood = Utils.getMoodFromDatabase(moodName, this);
			
			routeMood(priorMood);
				
		} else {
			priorName = null;
			priorMood = null;
		}
	}
	
	private static int calculateRoute(Mood m){
		if(m==null)
			return -1;
		if (m.timeAddressingRepeatPolicy==true){
			//show daily page
			return DAILY_PAGE;
		}else
			return TIMED_PAGE;
	}
	
	public void routeMood(Mood m){
		if(m!=null)
			mViewPager.setCurrentItem(calculateRoute(m));
	}
	
	public String getName(){
		if(nameEditText!=null)
			return nameEditText.getText().toString();
		return "";
	}
	public boolean isChecked(){
		if(loop!=null)
			return loop.isChecked();
		return false;
	}
	public void setChecked(boolean check){
		if(loop!=null)
			loop.setChecked(check);
	}
	
	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class EditMoodPagerAdapter extends FragmentPagerAdapter {

		EditMoodActivity frag;

		public EditMoodPagerAdapter(EditMoodActivity editMoodPagerDialogFragment) {
			super(editMoodPagerDialogFragment.getSupportFragmentManager());
			frag = editMoodPagerDialogFragment;
		}

		@Override
		public Fragment getItem(int i) {
			if (newMoodFragments[i] != null)
				return (Fragment) newMoodFragments[i];
			switch (i) {
			case TIMED_PAGE:
				EditAdvancedMoodFragment eamf = new EditAdvancedMoodFragment();
				eamf.setTimedMode(frag);
				Bundle args1 = new Bundle();
				if (calculateRoute(priorMood)==TIMED_PAGE) {
					args1.putString(InternalArguments.MOOD_NAME, priorName);
				}
				eamf.setArguments(args1);
				newMoodFragments[i] = eamf;
				return (Fragment) newMoodFragments[i];
			case DAILY_PAGE:
				EditAdvancedMoodFragment eamf2 = new EditAdvancedMoodFragment();
				eamf2.setDailyMode(frag);
				Bundle args2 = new Bundle();
				if (calculateRoute(priorMood)==DAILY_PAGE) {
					args2.putString(InternalArguments.MOOD_NAME, priorName);
				}
				eamf2.setArguments(args2);
				newMoodFragments[i] = eamf2;
				return (Fragment) newMoodFragments[i];
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case TIMED_PAGE:
				return frag.getString(R.string.cap_timed_mood);
			case DAILY_PAGE:
				return frag.getString(R.string.cap_daily_mood);
			}
			return "";
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.action_edit_mood, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.startActivity(new Intent(this,MainActivity.class));
				return true;
			case R.id.action_play:
				((OnCreateMoodListener)mEditMoodPagerAdapter.getItem(currentPage)).preview();
				break;
			case R.id.action_help:
				//TODO
				break;
			case R.id.action_save:
				if (priorName != null) {
					// delete old mood
					String moodSelect = MoodColumns.MOOD + "=?";
					String[] moodArg = { priorName };
					this.getContentResolver().delete(
							DatabaseDefinitions.MoodColumns.MOODS_URI,
							moodSelect, moodArg);
				}
				String moodName=nameEditText.getText().toString();
				if(moodName==null || moodName.length()<1){
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
					int unnamedNumber = 1+settings.getInt(PreferenceKeys.UNNAMED_MOOD_NUMBER, 0);
					Editor edit = settings.edit();
					edit.putInt(PreferenceKeys.UNNAMED_MOOD_NUMBER, unnamedNumber);
					edit.commit();
					moodName = this.getResources().getString(R.string.unnamed_mood)+" "+unnamedNumber;
				}
				newMoodFragments[currentPage].onCreateMood(moodName);
				break;
		}
		return false;
	}
}
