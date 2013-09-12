package com.kuxhausen.huemore.editmood;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.BulbListFragment;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.GroupListFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.R.id;
import com.kuxhausen.huemore.R.layout;
import com.kuxhausen.huemore.R.string;
import com.kuxhausen.huemore.network.GetBulbList.OnBulbListReturnedListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbAttributes;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditMoodPagerDialogFragment extends GodObject implements
		OnClickListener {

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

	EditText nameEditText;
	static String priorName;
	static Mood priorMood;
	static Gson gson = new Gson();

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
		
		
		nameEditText = (EditText) this.findViewById(R.id.editText1);

		mEditMoodPagerAdapter = new EditMoodPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) this.findViewById(R.id.pager);
		mViewPager.setAdapter(mEditMoodPagerAdapter);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;

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
				
		}
	}
	
	public void routeMood(Mood m){
		if(!m.usesTiming){
			if (m.events.length == 1 && m.events[0].state.ct == null) {
				// show simple mood page
				mViewPager.setCurrentItem(0);
			} else
			{
				// show multi mood page
				mViewPager.setCurrentItem(2);
			}
		}else{
			if(m.numChannels==1){
				mViewPager.setCurrentItem(1);
			}else{
				mViewPager.setCurrentItem(3);
			}	
		}
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
			case 0:
				ColorWheelFragment nchf = new ColorWheelFragment();
				nchf.hideColorLoop();
				Bundle args = new Bundle();
				args.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (priorMood != null && !priorMood.usesTiming && priorMood.events.length == 1
						&& priorMood.events[0].state.ct == null) {
					args.putString(InternalArguments.PREVIOUS_STATE,
							gson.toJson(priorMood.events[0].state));
				}
				nchf.setArguments(args);
				newMoodFragments[i] = nchf;
				return (Fragment) newMoodFragments[i];
			case 1:
				EditLoopMoodFragment etmf = new EditLoopMoodFragment();
				Bundle args1 = new Bundle();
				args1.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (priorMood != null && !priorMood.usesTiming) {
					args1.putString(InternalArguments.MOOD_NAME, priorName);
				}
				etmf.setArguments(args1);
				newMoodFragments[i] = etmf;
				return (Fragment) newMoodFragments[i];
			case 2:
				EditMultiMoodFragment nmmf = new EditMultiMoodFragment();
				Bundle args2 = new Bundle();
				args2.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (priorMood != null && !priorMood.usesTiming) {
					args2.putString(InternalArguments.MOOD_NAME, priorName);
				}
				nmmf.setArguments(args2);
				newMoodFragments[i] = nmmf;
				return (Fragment) newMoodFragments[i];
			case 3:
				EditComplexMoodFragment ecmf = new EditComplexMoodFragment();
				Bundle args3 = new Bundle();
				args3.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (priorMood != null && !priorMood.usesTiming) {
					args3.putString(InternalArguments.MOOD_NAME, priorName);
				}
				ecmf.setArguments(args3);
				newMoodFragments[i] = ecmf;
				return (Fragment) newMoodFragments[i];
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return frag.getString(R.string.cap_simple_mood);
			case 1:
				return frag.getString(R.string.cap_timed_mood);
			case 2:
				return frag.getString(R.string.cap_multi_mood);
			case 3:
				return frag.getString(R.string.cap_advanced_mood);
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
			newMoodFragments[currentPage].onCreateMood(nameEditText.getText()
					.toString());
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
