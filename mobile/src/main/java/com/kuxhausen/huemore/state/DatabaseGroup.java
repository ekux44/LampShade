package com.kuxhausen.huemore.state;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kuxhausen.huemore.persistence.Definitions.GroupBulbColumns;
import com.kuxhausen.huemore.persistence.Definitions.GroupColumns;
import com.kuxhausen.huemore.utils.DeferredLog;

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
  private final static String[] GROUPBULB_QUERY_COLUMNS = {
      GroupBulbColumns._ID,
      GroupBulbColumns.COL_GROUP_ID,
      GroupBulbColumns.COL_BULB_PRECEDENCE,
      GroupBulbColumns.COL_NET_BULB_ID
  };

  private long mId;
  private String mName, mLowercaseName;
  private int mPriority;
  private int mFlags;
  private List<Long> mGroupBulbNetId = new ArrayList<>();

  public static DatabaseGroup loadAllGroup(Context context) {
    String where = GroupColumns.COL_GROUP_FLAGS + "=?";
    String[] whereArgs = {"" + GroupColumns.FLAG_ALL};
    Cursor
        groupCursor =
        context.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, where, whereArgs, null);
    if (groupCursor.moveToFirst()) {
      DatabaseGroup result = new DatabaseGroup(groupCursor, context);
      groupCursor.close();
      return result;
    } else {
      throw new IllegalStateException("ALL Group not in database");
    }
  }

  public static @Nullable DatabaseGroup load(@NonNull String name, @NonNull Context context) {
    Cursor cursor = getGroupCursor(name, context);
    if (cursor != null) {
      return new DatabaseGroup(cursor, context);
    } else {
      return null;
    }
  }

  public static @Nullable DatabaseGroup load(long id, @NonNull Context context) {
    Cursor cursor = getGroupCursor(id, context);
    if (cursor != null) {
      return new DatabaseGroup(cursor, context);
    } else {
      return null;
    }
  }

  public DatabaseGroup(@NonNull Cursor groupCursor, @NonNull Context c) {
    mId = groupCursor.getLong(0);
    mName = groupCursor.getString(1);
    mLowercaseName = groupCursor.getString(2);
    mPriority = groupCursor.getInt(3);
    mFlags = groupCursor.getInt(4);

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

  private static @Nullable Cursor getGroupCursor(String name, @NonNull Context c) {
    String[] groupWhere = {name};
    Cursor
        groupCursor =
        c.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, GroupColumns.COL_GROUP_NAME + "=?",
                   groupWhere, null);

    if (groupCursor!=null && groupCursor.moveToFirst()) {
      return groupCursor;
    } else {

      if (groupCursor != null) {
        groupCursor.close();
      }

      String[] lowercaseGroupWhere = {name.toLowerCase().trim()};
      Cursor lowercaseGroupCursor = c.getContentResolver().query(GroupColumns.URI,
                                                                 GROUP_QUERY_COLUMNS,
                                                                 GroupColumns.COL_GROUP_LOWERCASE_NAME
                                                                 + "=?", lowercaseGroupWhere, null);
      if (lowercaseGroupCursor != null && lowercaseGroupCursor.moveToFirst()) {
        return lowercaseGroupCursor;
      } else {
        DeferredLog.w("Database", "Group %s not in database", name);

        if (lowercaseGroupCursor != null) {
          lowercaseGroupCursor.close();
        }
        return null;
      }
    }
  }

  private static @Nullable Cursor getGroupCursor(long id, @NonNull Context c) {
    String[] groupWhere = {"" + id};
    Cursor
        groupCursor =
        c.getContentResolver()
            .query(GroupColumns.URI, GROUP_QUERY_COLUMNS, GroupColumns._ID + "=?", groupWhere,
                   null);

    if (groupCursor != null && groupCursor.moveToFirst()) {
      return groupCursor;
    } else {
      DeferredLog.w("Database", "Group id %d not in database", id);
      if (groupCursor != null) {
        groupCursor.close();
      }
      return null;
    }
  }

  public long getId() {
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
    values.put(GroupColumns.COL_GROUP_FLAGS, mFlags);

    String where = GroupColumns._ID + "=?";
    String[] whereArg = {"" + mId};

    c.getContentResolver().update(GroupColumns.URI, values, where, whereArg);
  }

  public static DatabaseGroup createGroup(String name, Context c) {
    ContentValues values = new ContentValues();
    values.put(GroupColumns.COL_GROUP_NAME, name);
    values.put(GroupColumns.COL_GROUP_LOWERCASE_NAME, name.toLowerCase().trim());
    values.put(GroupColumns.COL_GROUP_PRIORITY, GroupColumns.PRIORITY_UNSTARRED);
    values.put(GroupColumns.COL_GROUP_FLAGS, GroupColumns.FLAG_NORMAL);

    long
        id =
        Long.parseLong(
            c.getContentResolver().insert(GroupColumns.URI, values).getLastPathSegment());
    return load(id, c);
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
