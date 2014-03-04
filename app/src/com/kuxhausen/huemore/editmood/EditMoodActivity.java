package com.kuxhausen.huemore.editmood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.kuxhausen.huemore.HelpActivity;
import com.kuxhausen.huemore.MainActivity;
import com.kuxhausen.huemore.NetworkManagedSherlockFragmentActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.Mood;

public class EditMoodActivity extends NetworkManagedSherlockFragmentActivity implements OnItemSelectedListener, OnCheckedChangeListener {

	EditMoodStateGridFragment stateGridFragment;

	private EditText nameEditText;
	private Spinner moodTypeSpinner;
	
	String priorName;
	Mood priorMood;
	Gson gson = new Gson();
	private CheckBox loop;
	
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
		loop.setOnCheckedChangeListener(this);
		moodTypeSpinner = (Spinner)this.findViewById(R.id.moodTypeSpinner);
		moodTypeSpinner.setOnItemSelectedListener(this);
		
		
		// If we're being restored from a previous state,
		// then we don't need to do anything and should return or else
		// we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			stateGridFragment = (EditMoodStateGridFragment)	getSupportFragmentManager().findFragmentById(R.id.edit_mood_fragment_container);
		} else {
			stateGridFragment = new EditMoodStateGridFragment();
			
			// In case this activity was started with special instructions from an Intent, pass the Intent's extras to the fragment as arguments
			stateGridFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.edit_mood_fragment_container, stateGridFragment, EditMoodStateGridFragment.class.getName()).commit();
		}
		
		Bundle args = this.getIntent().getExtras();
		if (args != null && args.containsKey(InternalArguments.MOOD_NAME)) {
			String moodName = args.getString(InternalArguments.MOOD_NAME);
			priorName = moodName;
			nameEditText.setText(moodName);
			
			priorMood = Utils.getMoodFromDatabase(moodName, this);
			
			moodTypeSpinner.setSelection(EditMoodStateGridFragment.calculateMoodType(priorMood).ordinal());
			if(moodTypeSpinner.getSelectedItemPosition() == EditMoodStateGridFragment.PageType.RELATIVE_PAGE.ordinal())
				setChecked(true);
			else
				setChecked(false);
				
		} else {
			priorName = null;
			priorMood = null;
		}
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
		if(loop!=null){
			loop.setChecked(check);
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
				stateGridFragment.preview();
				return true;
			case R.id.action_help:
				Intent i = new Intent(this, HelpActivity.class);
				i.putExtra(InternalArguments.HELP_PAGE, this.getResources().getString(R.string.help_title_editingmoods));
				this.startActivity(i);
				return true;
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
				stateGridFragment.onCreateMood(moodName);
				return true;
		}
		return false;
	}



	@Override
	public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(stateGridFragment!=null){
			stateGridFragment.setMoodMode(position);
		}
		if(position == EditMoodStateGridFragment.PageType.RELATIVE_PAGE.ordinal()){
			loop.setVisibility(View.VISIBLE);
		} else{
			loop.setVisibility(View.INVISIBLE);
		}
	}



	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(stateGridFragment!=null){
			stateGridFragment.redrawGrid();
		}
	}
}
