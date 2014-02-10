package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.R;

public class MoodRowAdapter extends SimpleCursorAdapter{

	private Cursor cursor;
	private Context context;
	private ArrayList<MoodRow> list = new ArrayList<MoodRow>();
	Gson gson = new Gson();
	MoodListFragment moodListFrag;
	
	public final static int TEXTVIEW_INDEX_IN_LAYOUT = 1;

	private ArrayList<MoodRow> getList() {
		return list;
	}

	public MoodRow getRow(int position) {
		return getList().get(position);
	}

	public MoodRowAdapter(MoodListFragment frag, Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.moodListFrag = frag;
		changeCursor(c);
	}

	@Override
	/***
	 * Cursor expected to have at each row {mood name, mood id}
	 */
	public void changeCursor(Cursor c) {
		super.changeCursor(c);
		//Log.e("changeCursor", ""+c);
		this.cursor = c;
		list = new ArrayList<MoodRow>();
		if (cursor != null) {
			cursor.moveToPosition(-1);// not the same as move to first!
			while (cursor.moveToNext()) {
				//Log.e("changeCursor", "row asdf");
				//try {
					list.add(new MoodRow(null, /*HueUrlEncoder.decode(cursor.getString(0)).second.first,*/cursor.getString(0), cursor.getInt(1)));
				/*} catch (InvalidEncodingException e) {
				} catch (FutureEncodingException e) {
				}*/
			}
		}
	}
	
	@Override
	public int getCount() {
		return (getList() != null) ? getList().size() : 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout moodRowView = (LinearLayout) convertView;
		ViewHolder viewHolder;

		if (moodRowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			moodRowView = (LinearLayout) inflater.inflate(R.layout.mood_row, null);

			// Hold the view objects in an object, that way the don't need to be "re-  finded"
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) moodRowView.findViewById(R.id.text1);
			moodRowView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) moodRowView.getTag();
		}

		/** Set data to your Views. */
		MoodRow item = getList().get(position);
		viewHolder.name.setText(item.name);
		
		moodRowView.setOnClickListener(new OnClickForwardingListener(moodListFrag, position));
		return moodRowView;
	}

	protected static class ViewHolder {
		protected TextView name;
	}
	
	public class OnClickForwardingListener implements OnClickListener{

		MoodListFragment mlf;
		int position;
		
		public OnClickForwardingListener(MoodListFragment frag, int pos){
			mlf = frag;
			position = pos;
		}
		
		@Override
		public void onClick(View v) {
			View textView = ((LinearLayout)v).getChildAt(TEXTVIEW_INDEX_IN_LAYOUT);
			mlf.onListItemClick(mlf.getListView(), textView, position, textView.getId());
		}
		
	}
	
}
