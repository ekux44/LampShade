package com.kuxhausen.huemore.state;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.DatabaseDefinitions.GroupColumns;

public class Group {
	
	private String mName;
	private ArrayList<Long> mNetworkBulbDatabaseIds;
	
	public String getName(){
		return mName;
	}
	public ArrayList<Long> getNetworkBulbDatabaseIds(){
		return mNetworkBulbDatabaseIds;
	}
	
	public Group(ArrayList<Long> netBulbBaseIds, String name){
		mNetworkBulbDatabaseIds = netBulbBaseIds;
		mName = name;
	}
	
	public static Group loadFromDatabase(String name, Context c){
		
		String[] groupColumns = { GroupColumns.BULB_DATABASE_ID };
		String[] gWhereClause = { name };
		Cursor cursor = c.getContentResolver().query(GroupColumns.GROUPBULBS_URI, groupColumns, GroupColumns.GROUP + "=?", gWhereClause, null);

		ArrayList<Long> netBulbDbIds = new ArrayList<Long>();
		while (cursor.moveToNext()) {
			netBulbDbIds.add(cursor.getLong(0));
		}
		
		Group result = new Group(netBulbDbIds, name);
		return result;
	}
	
	public static Group loadFromLegacyData(Integer[] bulbs, String groupName, Context c) {
		// TODO Auto-generated method stub
		
		Group result = new Group(new ArrayList<Long>(), groupName);
		
		return result;
	}
	
	public boolean conflictsWith(Group other){
		for(Long mBulbId : mNetworkBulbDatabaseIds){
			for(Long oBulbId : other.mNetworkBulbDatabaseIds){
				if(mBulbId.equals(oBulbId))
					return true;
			}
		}
		return false;
	}
}
