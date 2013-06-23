package com.kuxhausen.huemore;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

public class AddMoodGroupSelectorDialogFragment extends DialogFragment implements OnClickListener {

	MainActivity ma;
	Button newGroup, newMood;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.add_new_selector, container, false);
		ma = (MainActivity) this.getActivity();
		//this.getDialog().setTitle(R.string.);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		Button newGroup = (Button)myView.findViewById(R.id.newGroupButton);
		newGroup.setOnClickListener(this);
		
		Button newMood = (Button)myView.findViewById(R.id.newMoodButton);
		newMood.setOnClickListener(this);
		
		
		return myView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newGroupButton: 
			EditGroupDialogFragment ngdf = new EditGroupDialogFragment();
			ngdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			dismiss();
			break;
		case R.id.newMoodButton: 
			EditMoodPagerDialogFragment nmdf = new EditMoodPagerDialogFragment();
			nmdf.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
			dismiss();
			break;
		}
	}
}
