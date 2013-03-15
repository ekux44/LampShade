package com.kuxhausen.huemore;

import java.util.ArrayList;

import com.kuxhausen.huemore.GroupSelectorDialogFragment.OnGroupSelectedListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class NewMoodDialogFragment extends DialogFragment implements OnClickListener , OnKeyListener, OnGroupSelectedListener{

	ListView bulbsListView;
	MoodRowAdapter rayAdapter;
	ArrayList<MoodRow> moodRowArray;
	EditText nameEditText;
	EditText stateName;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		moodRowArray = new ArrayList<MoodRow>();
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View groupDialogView = inflater.inflate(R.layout.edit_mood_dialog,
				null);
		bulbsListView = ((ListView) groupDialogView
				.findViewById(R.id.listView1));
		rayAdapter = new MoodRowAdapter(this.getActivity(),
				moodRowArray);
		bulbsListView.setAdapter(rayAdapter);
		builder.setView(groupDialogView);

		nameEditText = (EditText) groupDialogView.findViewById(R.id.editText1);
		
		Button enablePreview = (Button) groupDialogView.findViewById(R.id.previewButton);
		enablePreview.setOnClickListener(this);
		
		Button addColor = (Button) groupDialogView.findViewById(R.id.addColor);
		addColor.setOnClickListener(this);
		
		builder.setPositiveButton(R.string.accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//TODO
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	
	private void addState() {
		MoodRow mr = new MoodRow();
        mr.color = 0xff000000;
        moodRowArray.add(mr);
        rayAdapter.add(mr);
		ColorPickerDialogFragment cpdf = new ColorPickerDialogFragment();
		cpdf.setTargetFragment(this, rayAdapter.getPosition(mr));
		cpdf.show(getFragmentManager(), "dialog");
    }
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		rayAdapter.getItem(requestCode).color = resultCode;
		rayAdapter.notifyDataSetChanged();
	}

	@Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.FLAG_EDITOR_ACTION:
                    addState();
                    return true;
            }
        }
        return false;
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addColor:
			addState();
			break;
		case R.id.previewButton:
			GroupSelectorDialogFragment gsdf = new GroupSelectorDialogFragment();
			gsdf.setOnGroupSelectedListener(this);
			gsdf.show(getFragmentManager(), "dialog");
			break;
		}
	}


	@Override
	public void groupSelected(String group) {
		// TODO Auto-generated method stub
		Log.e("asdf", group);
	}
}
