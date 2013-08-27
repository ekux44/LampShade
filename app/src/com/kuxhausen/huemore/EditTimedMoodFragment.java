package com.kuxhausen.huemore;

import com.kuxhausen.huemore.EditMoodPagerDialogFragment.OnCreateMoodListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditTimedMoodFragment extends Fragment implements OnCreateMoodListener{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View myView = inflater.inflate(R.layout.edit_timed_mood, null);
		
		return myView;
	}

	@Override
	public void onCreateMood(String groupname) {
		// TODO Auto-generated method stub
		
	}

}
