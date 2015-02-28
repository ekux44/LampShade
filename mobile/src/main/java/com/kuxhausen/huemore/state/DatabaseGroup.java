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
      GroupColumns.COL_GROUP_PRIORITY,
      GroupColumns.COL_GROUP_FLAGS
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
  private int mFlags;
  private List<Long> mGroupBulbNetId = new ArrayList<Long>();

  public static DatabaseGroup loadAllGroup(Context context) {
    String where = GroupColumns.COL_GROUP_FLAGS + "=?";
    String[] whereArgs = {"" + GroupColumns.FLAG_ALL};
    Cursor
        c =
        context.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, where, whereArgs, null);
    return new DatabaseGroup(c, context);
  }

  public DatabaseGroup(String name, Context c) {
    this(getGroupCursor(name, c), c);
  }

  public DatabaseGroup(long id, Context c) {
    this(getGroupCursor(id, c), c);
  }

  public DatabaseGroup(Cursor groupCursor, Context c) {
    mId = groupCursor.getLong(0);
    mName = groupCursor.getString(1);
    mLowercaseName = groupCursor.getString(2);
    mPriority = groupCursor.getInt(3);
    mFlags = groupCursor.getInt(4);

    groupCursor.close();

    String[] bulbWhere = {"" + mId};
    Cursor
        bulbCursor =
        c.getContentResolver().query(GroupBulbColumns.URI, GROUPBULB_QUERY_COLUMNS,
                                     GroupBulbColumns.COL_GROUP_ID + "=?", bulbWhere,
                                     GroupBulbColumns.COL_BULB_PRECEDENCE + " ASC");

    bulbCursor.moveToPosition(-1);
    while (bulbCursor.moveToNext()) {
      mGroupBulbNetId.add(bulbCursor.getLong(3));
    }

    bulbCursor.close();

  }

  private static Cursor getGroupCursor(String name, Context c) {
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

  private static Cursor getGroupCursor(long id, Context c) {
    String[] groupWhere = {"" + id};
    Cursor
        groupCursor =
        c.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, GroupColumns._ID + "=?", groupWhere,
                   null);

    if (groupCursor.moveToFirst()) {
      return groupCursor;
    } else {
      throw new IllegalStateException("Group id " + id + " not in database");
    }
  }

  public long getId(){
    return mId;
  }

  @Override
  public String getName() {
    return mName;
  }

  public void setName(String name, Context c) {
    if (!mName.equals(name)) {
      mName = name;
      mLowercaseName = name.toLowerCase().trim();
      saveGroupUpdate(c);
    }
  }

  @Override
  public List<Long> getNetworkBulbDatabaseIds() {
    return mGroupBulbNetId;
  }

  public void setNetBulbDatabaseIds(List<Long> bulbIdList, Context c) {
    if (!mGroupBulbNetId.equals(bulbIdList)) {

      String where = GroupBulbColumns.COL_GROUP_ID + "=?";
      String[] whereArgs = {"" + mId};
      c.getContentResolver().delete(GroupBulbColumns.URI, where, whereArgs);

      for (int i = 0; i < bulbIdList.size(); i++) {
        ContentValues values = new ContentValues();
        values.put(GroupBulbColumns.COL_GROUP_ID, mId);
        values.put(GroupBulbColumns.COL_BULB_PRECEDENCE, i);
        values.put(GroupBulbColumns.COL_NET_BULB_ID, bulbIdList.get(i));

        c.getContentResolver().insert(GroupBulbColumns.URI, values);
      }

      mGroupBulbNetId = bulbIdList;
    }
  }

  public boolean isStared() {
    return (mPriority == GroupColumns.PRIORITY_STARRED);
  }

  public void starChanged(Context c, boolean isStared) {
    if (isStared) {
      mPriority = GroupColumns.PRIORITY_STARRED;
    } else {
      mPriority = GroupColumns.PRIORITY_UNSTARRED;
    }

    saveGroupUpdate(c);
  }

  public boolean isALL() {
    return (mFlags == GroupColumns.FLAG_ALL);
  }

  private void saveGroupUpdate(Context c) {
    ContentValues values = new ContentValues();
    values.put(GroupColumns.COL_GROUP_NAME, mName);
    values.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, mLowercaseName);
    values.put(GroupColumns.COL_GROUP_PRIORITY, mPriority);
    values.put(GroupColumns.COL_GROUP_PRIORITY, mFlags);

    String where = GroupColumns._ID + "=?";
    String[] whereArg = {"" + mId};

    c.getContentResolver().update(GroupColumns.URI, values, where, whereArg);
  }

  public static DatabaseGroup createGroup(String name, Context c) {
    ContentValues values = new ContentValues();
    values.put(GroupColumns.COL_GROUP_NAME, name);
    values.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, name.toLowerCase());
    values.put(GroupColumns.COL_GROUP_PRIORITY, GroupColumns.PRIORITY_UNSTARRED);
    values.put(GroupColumns.COL_GROUP_FLAGS, GroupColumns.FLAG_NORMAL);

    long
        id =
        Long.parseLong(
            c.getContentResolver().insert(GroupColumns.URI, values).getLastPathSegment());
    return new DatabaseGroup(id, c);
  }

  /*
   * no further methods should be called on this object after this
   */
  public void deleteSelf(Context c) {
    String groupWhere = GroupColumns._ID + "=?";
    String[] whereArgs = {"" + mId};
    c.getContentResolver().delete(GroupColumns.URI, groupWhere, whereArgs);

    mId = mPriority = -1;
    mName = mLowercaseName = "";
  }

}
