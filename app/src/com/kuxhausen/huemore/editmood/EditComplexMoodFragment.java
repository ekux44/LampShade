package com.kuxhausen.huemore.editmood;

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

public class EditComplexMoodFragment extends Fragment implements OnCreateMoodListener, OnClickListener{
	
	Gson gson = new Gson();
	Button enterAdvanced;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		View myView = inflater.inflate(R.layout.edit_complex_mood, null);
		enterAdvanced = (Button) myView.findViewById(R.id.enterAdvancedEditor);
		enterAdvanced.setOnClickListener(this);
		
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
			Intent i = new Intent(this.getActivity(), EditAdvancedMoodActivity.class);
			i.putExtra(InternalArguments.SERIALIZED_GOD_OBJECT, ((GodObject)this.getActivity()).getSerialized());
			this.startActivity(i);
			break;
		}
	}
}
