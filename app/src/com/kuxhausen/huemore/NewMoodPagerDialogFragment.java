package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kuxhausen.huemore.database.DatabaseDefinitions;
import com.kuxhausen.huemore.database.DatabaseDefinitions.GroupColumns;
import com.kuxhausen.huemore.state.HueState;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class NewMoodPagerDialogFragment extends DialogFragment implements
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
	NewMoodPagerAdapter mNewMoodPagerAdapter;
	static OnCreateMoodListener[] newMoodFragments;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;

	static int currentPage;

	public interface OnCreateMoodListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onCreateMood();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.dialog_pager, container, false);
		Bundle args = getArguments();

		// Create an adapter that when requested, will return a fragment
		// representing an object in
		// the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use
		// getSupportFragmentManager.
		mNewMoodPagerAdapter = new NewMoodPagerAdapter(this);

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) myView.findViewById(R.id.pager);
		mViewPager.setAdapter(mNewMoodPagerAdapter);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				currentPage = position;

			}

		});
		this.getDialog().setTitle("New Mood");

		Button cancelButton = (Button) myView.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		Button okayButton = (Button) myView.findViewById(R.id.okay);
		okayButton.setOnClickListener(this);

		newMoodFragments = new OnCreateMoodListener[2];
		return myView;
	}

	/**
	 * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a
	 * fragment representing an object in the collection.
	 */
	public static class NewMoodPagerAdapter extends FragmentPagerAdapter {

		public NewMoodPagerAdapter(android.support.v4.app.Fragment fragment) {
			super(fragment.getChildFragmentManager());

			// write your code here
		}

		@Override
		public Fragment getItem(int i) {
			if (newMoodFragments[i] != null)
				return (Fragment) newMoodFragments[i];
			switch (i) {
			case 0:
				NewColorHueFragment nchf = new NewColorHueFragment();
				Bundle args = new Bundle();
				args.putBoolean("ShowEditText", true);
				nchf.setArguments(args);
				newMoodFragments[i] = nchf;
				return (Fragment) newMoodFragments[i];
			case 1:
				newMoodFragments[i] = new NewMultiMoodFragment();
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
				return "SIMPLE";// TODO figure out how to make static
								// references
								// to strings.xml
			case 1:
				return "ADVANCED";
			}
			return "";
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.okay:
			((OnCreateMoodListener) newMoodFragments[currentPage])
					.onCreateMood();
			this.dismiss();
			break;
		case R.id.cancel:
			this.dismiss();
			break;
		}
	}
}
