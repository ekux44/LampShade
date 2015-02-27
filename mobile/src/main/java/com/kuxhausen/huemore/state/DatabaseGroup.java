package com.kuxhausen.huemore.state;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.Definitions.GroupBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;

import java.util.ArrayList;
import java.util.List;

public class DatabaseGroup extends Group {

  // must be kept in sync with DatabaseGroup constructor
  public final static String[] GROUP_QUERY_COLUMNS = {
      GroupColumns._ID,
      GroupColumns.COL_GROUP_NAME,
      GroupColumns.COL_GROUP_LOWERCASE_NAME,
      GroupColumns.COL_GROUP_PRIORITY
  };

  // must be kept in sync with DatabaseGroup constructor
  public final static String[] GROUPBULB_QUERY_COLUMNS = {
      GroupBulbColumns._ID,
      GroupBulbColumns.COL_GROUP_ID,
      GroupBulbColumns.COL_BULB_PRECEDENCE,
      GroupBulbColumns.COL_NET_BULB_ID
  };

  private long mId;
  private String mName, mLowercaseName;
  private int mPriority;
  private List<Long> mGroupBulbId;
  private List<Integer> mGroupBulbPrecedence;
  private List<Long> mGroupBulbNetId;


  public DatabaseGroup(String name, Context c) {
    this(getGroupCursor(name, c), c);
  }

  public DatabaseGroup(Cursor groupCursor, Context c) {
    mId = groupCursor.getLong(0);
    mName = groupCursor.getString(1);
    mLowercaseName = groupCursor.getString(2);
    mPriority = groupCursor.getInt(3);

    groupCursor.close();

    String[] bulbWhere = {"" + mId};
    Cursor
        bulbCursor =
        c.getContentResolver().query(GroupBulbColumns.URI, GROUPBULB_QUERY_COLUMNS,
                                     GroupBulbColumns.COL_GROUP_ID + "=?", bulbWhere,
                                     GroupBulbColumns.COL_BULB_PRECEDENCE + " ASC");

    mGroupBulbId = new ArrayList<Long>();
    mGroupBulbPrecedence = new ArrayList<Integer>();
    mGroupBulbNetId = new ArrayList<Long>();

    bulbCursor.moveToPosition(-1);
    while (bulbCursor.moveToNext()) {
      mGroupBulbId.add(bulbCursor.getLong(0));
      mGroupBulbPrecedence.add(bulbCursor.getInt(2));
      mGroupBulbNetId.add(bulbCursor.getLong(3));
    }

    bulbCursor.close();

  }

  public static Cursor getGroupCursor(String name, Context c) {
    String[] groupWhere = {name};
    Cursor
        groupCursor =
        c.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, GroupColumns.COL_GROUP_NAME + "=?",
                   groupWhere, null);

    if (groupCursor.moveToFirst()) {
      return groupCursor;
    } else {
      throw new IllegalStateException("Group " + name + " not in database");
    }
  }

  @Override
  public String getName() {
    return mName;
  }

  public void setName(String name, Context c) {
    mName = name;
    mLowercaseName = name.toLowerCase();
    saveGroupUpdate(c);
  }

  @Override
  public List<Long> getNetworkBulbDatabaseIds() {
    return mGroupBulbNetId;
  }

  public boolean isStared() {
    return (mPriority == GroupColumns.STARRED_PRIORITY);
  }

  public void starChanged(Context c, boolean isStared) {
    if (isStared) {
      mPriority = GroupColumns.STARRED_PRIORITY;
    } else {
      mPriority = GroupColumns.UNSTARRED_PRIORITY;
    }

    saveGroupUpdate(c);
  }

  private void saveGroupUpdate(Context c) {
    ContentValues mValues = new ContentValues();
    mValues.put(GroupColumns.COL_GROUP_NAME, mName);
    mValues.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, mLowercaseName);
    mValues.put(GroupColumns.COL_GROUP_PRIORITY, mPriority);

    String where = GroupColumns._ID + "=?";
    String[] whereArg = {"" + mId};

    c.getContentResolver().update(GroupColumns.URI, mValues, where, whereArg);
  }

  /*
   * no further methods should be called on this object after this
   */
  public void deleteSelf(Context c) {
    String groupWhere = GroupColumns._ID + "=?";
    String[] whereArgs = {"" + mId};
    c.getContentResolver().delete(GroupColumns.URI, groupWhere, whereArgs);

    mId = -1;
    mName = "";
    mLowercaseName = "";
    mPriority = -1;
  }

}
