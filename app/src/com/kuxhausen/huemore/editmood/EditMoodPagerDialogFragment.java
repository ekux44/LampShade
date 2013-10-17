package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.BulbListFragment;
import com.kuxhausen.huemore.ColorWheelFragment;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.GroupListFragment;
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

public class EditMoodPagerDialogFragment extends GodObject implements
		OnClickListener, OnCheckedChangeListener {

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
	
	public final static int WHEEL_PAGE = 0, MULTI_PAGE=1, TIMED_PAGE=2;

	public interface OnCreateMoodListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onCreateMood(String groupname);

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mood_dialog_pager);
		this.restoreSerialized(this.getIntent().getStringExtra(InternalArguments.SERIALIZED_GOD_OBJECT));
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		nameEditText = (EditText) this.findViewById(R.id.moodNameEditText);
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
		this.getSupportActionBar().setTitle(
				this.getString(R.string.actionmenu_new_mood));

		Button cancelButton = (Button) this.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) this.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		newMoodFragments = new OnCreateMoodListener[mEditMoodPagerAdapter.getCount()];

		Bundle args = this.getIntent().getExtras();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			String moodName = args.getString(InternalArguments.MOOD_NAME);
			priorName = moodName;
			nameEditText.setText(moodName);
			
			priorMood = Utils.getMoodFromDatabase(moodName, this);
			
			routeMood(priorMood);
				
		} else {
			priorMood = null;
		}
		loop.setOnCheckedChangeListener(this);
	}
	
	private static int calculateRoute(Mood m){
		if(m==null)
			return -1;
		if(!m.usesTiming){
			if (m.events.length == 1 && m.events[0].state.ct == null) {
				// show simple mood page
				return WHEEL_PAGE;
			} else
			{
				// show multi mood page
				return MULTI_PAGE;
			}
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(currentPage==TIMED_PAGE)
			((EditAdvancedMoodFragment) mEditMoodPagerAdapter.getItem(currentPage)).preview();
	}
	
	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class EditMoodPagerAdapter extends FragmentPagerAdapter {

		EditMoodPagerDialogFragment frag;

		public EditMoodPagerAdapter(EditMoodPagerDialogFragment editMoodPagerDialogFragment) {
			super(editMoodPagerDialogFragment.getSupportFragmentManager());
			frag = editMoodPagerDialogFragment;
		}

		@Override
		public Fragment getItem(int i) {
			if (newMoodFragments[i] != null)
				return (Fragment) newMoodFragments[i];
			switch (i) {
			case WHEEL_PAGE:
				ColorWheelFragment nchf = new ColorWheelFragment();
				nchf.hideColorLoop();
				Bundle args = new Bundle();
				args.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (calculateRoute(priorMood)==WHEEL_PAGE) {
					args.putString(InternalArguments.PREVIOUS_STATE,
							gson.toJson(priorMood.events[0].state));
				}
				nchf.setArguments(args);
				newMoodFragments[i] = nchf;
				return (Fragment) newMoodFragments[i];
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
			case MULTI_PAGE:
				EditAdvancedMoodFragment eamf2 = new EditAdvancedMoodFragment();
				eamf2.setMultiMode(frag);
				Bundle args2 = new Bundle();
				if (calculateRoute(priorMood)==MULTI_PAGE) {
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
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case WHEEL_PAGE:
				return frag.getString(R.string.cap_simple_mood);
			case TIMED_PAGE:
				return frag.getString(R.string.cap_timed_mood);
			case MULTI_PAGE:
				return frag.getString(R.string.cap_multi_mood);
			}
			return "";
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.okay:
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
			this.onBackPressed();
			break;
		case R.id.cancel:
			this.onBackPressed();
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
		}
		return false;
	}

	@Override
	public void onListReturned(BulbAttributes[] bulbsAttributes) {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void onGroupBulbSelected(Integer[] bulb, String name) {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void setBulbListenerFragment(OnBulbListReturnedListener frag) {
		throw new RuntimeException("Not implemented here");
		
	}

	@Override
	public OnBulbListReturnedListener getBulbListenerFragment() {
		throw new RuntimeException("Not implemented here");
	}

	@Override
	public void onSelected(Integer[] bulbNum, String name,
			GroupListFragment groups, BulbListFragment bulbs) {
		throw new RuntimeException("Not implemented here");
	}
}
