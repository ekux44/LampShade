package com.kuxhausen.huemore.state;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.Definitions;

import java.util.ArrayList;
import java.util.List;

public class DatabaseGroup extends Group {

  private String mName;
  private List<Long> mNetworkBulbDatabaseIds;
  private int mPriority;

  public DatabaseGroup(String name, Context c) {
    String[] groupColumns = {Definitions.DeprecatedGroupColumns.BULB_DATABASE_ID};
    String[] gWhereClause = {name};
    Cursor cursor =
        c.getContentResolver()
            .query(Definitions.DeprecatedGroupColumns.GROUPBULBS_URI, groupColumns,
                   Definitions.DeprecatedGroupColumns.GROUP + "=?", gWhereClause, null);

    ArrayList<Long> netBulbDbIds = new ArrayList<Long>();
    while (cursor.moveToNext()) {
      netBulbDbIds.add(cursor.getLong(0));
    }
    cursor.close();

    mNetworkBulbDatabaseIds = netBulbDbIds;
    mName = name;
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public List<Long> getNetworkBulbDatabaseIds() {
    return mNetworkBulbDatabaseIds;
  }


}
