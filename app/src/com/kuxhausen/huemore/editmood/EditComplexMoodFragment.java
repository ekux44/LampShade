package com.kuxhausen.huemore.editmood;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditMoodPagerDialogFragment.OnCreateMoodListener;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class EditComplexMoodFragment extends SherlockFragment implements OnCreateMoodListener, OnClickListener{
	
	Gson gson = new Gson();
	Button enterAdvanced;
	EditMoodPagerDialogFragment pager;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.edit_complex_mood, null);
		enterAdvanced = (Button) myView.findViewById(R.id.enterAdvancedEditor);
		enterAdvanced.setOnClickListener(this);
		
		
		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			Intent i = getAdvancedIntent();
			i.putExtra(InternalArguments.MOOD_NAME, args.getString(InternalArguments.MOOD_NAME));
			this.startActivity(i);
		}
		
		return myView;
	}

	@Override
	public void onCreateMood(String groupname) {
		// TODO Auto-generated method stub
		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.enterAdvancedEditor:
			this.startActivity(getAdvancedIntent());
			break;
		}
	}
	
	public Intent getAdvancedIntent(){
		Intent i = new Intent(this.getActivity(), EditAdvancedMoodActivity.class);
		if(pager!= null && pager.getName().length()>0)
			i.putExtra(InternalArguments.MOOD_NAME, pager.getName());
		i.putExtra(InternalArguments.SERIALIZED_GOD_OBJECT, ((GodObject)this.getActivity()).getSerialized());
		return i;
	}
}
