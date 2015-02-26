package com.kuxhausen.huemore.state;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.persistence.Definitions;

import java.util.ArrayList;
import java.util.List;

public class NfcGroup extends Group {

  private String mName;
  private List<Long> mNetworkBulbDatabaseIds;

  public NfcGroup(Integer[] bulbs, String groupName, Context c) {
    ArrayList<Long> netBulbDbIds = new ArrayList<Long>();

    String[] projections = {Definitions.NetBulbColumns._ID};
    for (Integer deviceId : bulbs) {
      String[] selectionArgs =
          {"" + deviceId, "" + Definitions.NetBulbColumns.NetBulbType.PHILIPS_HUE};

      Cursor cursor =
          c.getContentResolver().query(
              Definitions.NetBulbColumns.URI,
              projections,
              Definitions.NetBulbColumns.DEVICE_ID_COLUMN + " =? AND "
              + Definitions.NetBulbColumns.TYPE_COLUMN + " =?", selectionArgs, null
          );

      if (cursor.moveToFirst()) {
        netBulbDbIds.add(cursor.getLong(0));
      }
      cursor.close();
    }

    mNetworkBulbDatabaseIds = netBulbDbIds;
    mName = groupName;
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
