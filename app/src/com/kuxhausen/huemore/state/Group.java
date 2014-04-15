package com.kuxhausen.huemore.state;

import android.content.Context;

public class Group {
	
	private String mName;
	private long[] mNetworkBulbDatabaseIds;
	
	public String getName(){
		return mName;
	}
	public long[] getNetworkBulbDatabaseIds(){
		return mNetworkBulbDatabaseIds;
	}
	
	public Group(long[] netBulbBaseIds, String name){
		mNetworkBulbDatabaseIds = netBulbBaseIds;
		mName = name;
	}
	
	public static Group loadFromDatabase(String name, Context c){
		// TODO Auto-generated method stub
		
		Group result = new Group(new long[0], name);
		
		
		/*// Look up bulbs for that mood from database
		String[] groupColumns = { GroupColumns.BULB };
		String[] gWhereClause = { selected.getText().toString() };
		Cursor cursor = getActivity().getContentResolver().query(
				DatabaseDefinitions.GroupColumns.GROUPBULBS_URI, // Use the
																	// default
																	// content
																	// URI
																	// for the
																	// provider.
				groupColumns, // Return the note ID and title for each note.
				GroupColumns.GROUP + "=?", // selection clause
				gWhereClause, // selection clause args
				null // Use the default sort order.
				);

		ArrayList<Integer> groupStates = new ArrayList<Integer>();
		while (cursor.moveToNext()) {
			groupStates.add(cursor.getInt(0));
		}
		int[] bulbS = new int[groupStates.size()];
		for(int i = 0; i< bulbS.length; i++)
			bulbS[i] = groupStates.get(i);
		 */

		return result;
	}
	
	public static Group loadFromLegacyData(int[] bulbs, String groupName, Context c) {
		// TODO Auto-generated method stub
		
		Group result = new Group(new long[0], groupName);
		
		return result;
	}
}
