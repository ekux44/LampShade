package com.kuxhausen.huemore;

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

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.state.api.BulbState;

public class EditMoodPagerDialogFragment extends DialogFragment implements
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
	String priorName;

	static BulbState[] priorMood;
	static Gson gson = new Gson();

	public interface OnCreateMoodListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onCreateMood(String groupname);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.mood_dialog_pager, container,
				false);
		// Bundle args = getArguments();

		nameEditText = (EditText) myView.findViewById(R.id.editText1);

		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use
		// getSupportFragmentManager.
		mEditMoodPagerAdapter = new EditMoodPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mEditMoodPagerAdapter);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;

			}

		});
		this.getDialog().setTitle(
				getActivity().getString(R.string.actionmenu_new_mood));

		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);
		newMoodFragments = new OnCreateMoodListener[2];

		Bundle args = this.getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			String moodName = args.getString(InternalArguments.MOOD_NAME);
			priorName = moodName;
			nameEditText.setText(moodName);

			// Look up states for that mood from database
			String[] moodColumns = { MoodColumns.STATE };
			String[] mWhereClause = { moodName };
			Cursor moodCursor = this.getActivity().getContentResolver()
					.query(DatabaseDefinitions.MoodColumns.MOODSTATES_URI, // Use
																			// the
																			// default
																			// content
																			// URI
																			// for
																			// the
																			// provider.
							moodColumns, // Return the note ID and title for
											// each note.
							MoodColumns.MOOD + "=?", // selection clause
							mWhereClause, // election clause args
							null // Use the default sort order.
					);

			ArrayList<String> moodStates = new ArrayList<String>();
			while (moodCursor.moveToNext()) {
				moodStates.add(moodCursor.getString(0));
			}
			String[] moodS = moodStates.toArray(new String[moodStates.size()]);
			priorMood = new BulbState[moodStates.size()];
			for (int i = 0; i < priorMood.length; i++)
				priorMood[i] = gson.fromJson(moodS[i], BulbState.class);

			if (priorMood.length == 1 && priorMood[0].ct == null) {
				// show simple mood page
				mViewPager.setCurrentItem(0);
			} else {
				// show multi mood page
				mViewPager.setCurrentItem(1);
			}
		}
		return myView;
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class EditMoodPagerAdapter extends FragmentPagerAdapter {

		android.support.v4.app.Fragment frag;

		public EditMoodPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());
			frag = fragment;
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
				if (priorMood != null && priorMood.length == 1
						&& priorMood[0].ct == null) {
					args.putString(InternalArguments.BULB_STATE,
							gson.toJson(priorMood[0]));
				}
				nchf.setArguments(args);
				newMoodFragments[i] = nchf;
				return (Fragment) newMoodFragments[i];
			case 1:
				NewMultiMoodFragment nmmf = new NewMultiMoodFragment();
				Bundle args2 = new Bundle();
				args2.putBoolean(InternalArguments.SHOW_EDIT_TEXT, true);
				if (priorMood != null
						&& (priorMood.length > 1 || priorMood[0].ct != null)) {
					args2.putString(InternalArguments.BULB_STATES,
							gson.toJson(priorMood));
				}
				nmmf.setArguments(args2);
				newMoodFragments[i] = nmmf;
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
			case 0:
				return frag.getActivity().getString(R.string.cap_simple_mood);
			case 1:
				return frag.getActivity().getString(R.string.cap_advanced_mood);
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
				getActivity().getContentResolver().delete(
						DatabaseDefinitions.MoodColumns.MOODSTATES_URI,
						moodSelect, moodArg);
			}
			newMoodFragments[currentPage].onCreateMood(nameEditText.getText()
					.toString());
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
