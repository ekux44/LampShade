package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kuxhausen.huemore.persistence.FutureEncodingException;
import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.persistence.InvalidEncodingException;

public class MoodRowAdapter extends SimpleCursorAdapter{

	private Cursor cursor;
	private Context context;
	private ArrayList<MoodRow> list = new ArrayList<MoodRow>();
	Gson gson = new Gson();
	MoodListFragment moodListFrag;
	

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
				try {
					list.add(new MoodRow(cursor.getString(0), cursor.getInt(1), HueUrlEncoder.decode(cursor.getString(2)).second.first));
				} catch (InvalidEncodingException e) {
				} catch (FutureEncodingException e) {
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		return (getList() != null) ? getList().size() : 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView moodRowView = (TextView) convertView;
		ViewHolder viewHolder;

		if (moodRowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			
			// create ContextThemeWrapper from the original Activity Context with the custom theme
		    final Context contextThemeWrapper = new ContextThemeWrapper(context, R.style.red);
		    // clone the inflater using the ContextThemeWrapper
		    LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
			
			
			int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
			moodRowView = (TextView) localInflater.inflate(layout, null);
			moodRowView.setLongClickable(true);
			
			// Hold the view objects in an object, that way the don't need to be "re-  finded"
			viewHolder = new ViewHolder();
			viewHolder.ctv = moodRowView;
			moodRowView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) moodRowView.getTag();
		}

		/** Set data to your Views. */
		MoodRow item = getList().get(position);
		if(!viewHolder.ctv.getText().equals(item.name)){
			viewHolder.ctv.setText(item.name);
			MoodPreviewDrawable mDraw = new MoodPreviewDrawable(context.getResources().getDisplayMetrics());
			mDraw.setMood(item.m);
			viewHolder.ctv.setCompoundDrawablesWithIntrinsicBounds(mDraw, null, null, null);
		}
		moodRowView.setOnClickListener(new OnClickForwardingListener(moodListFrag, position));
		return moodRowView;
	}

	protected static class ViewHolder {
		protected TextView ctv;
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
			View textView = v;
			mlf.onListItemClick(mlf.getListView(), textView, position, textView.getId());
		}
		
	}
	
}
