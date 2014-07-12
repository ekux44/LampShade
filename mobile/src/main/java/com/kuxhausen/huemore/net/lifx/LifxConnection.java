package com.kuxhausen.huemore.net.lifx;

import com.google.gson.Gson;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions;

import java.util.ArrayList;

import lifx.java.android.light.LFXLight;

public class LifxConnection implements Connection {

  private static final String[] bulbColumns = {DatabaseDefinitions.NetBulbColumns._ID,
                                               DatabaseDefinitions.NetBulbColumns.CONNECTION_DATABASE_ID,
                                               DatabaseDefinitions.NetBulbColumns.TYPE_COLUMN,
                                               DatabaseDefinitions.NetBulbColumns.NAME_COLUMN,
                                               DatabaseDefinitions.NetBulbColumns.DEVICE_ID_COLUMN,
                                               DatabaseDefinitions.NetBulbColumns.JSON_COLUMN,
                                               DatabaseDefinitions.NetBulbColumns.CURRENT_MAX_BRIGHTNESS};
  private static final Gson gson = new Gson();

  private Long mBaseId;
  private String mName, mDeviceId;
  ExtraData mData;
  private Context mContext;
  private LifxBulb mBulb;
  DeviceManager mDeviceManager;
  private LFXLight mLight;

  public LifxConnection(Context c, Long baseId, String name, String deviceId,
                        ExtraData data, DeviceManager dm) {
    mBaseId = baseId;
    mName = name;
    mDeviceId = deviceId;
    mData = data;

    mContext = c;
    mDeviceManager = dm;

    String selection =
        DatabaseDefinitions.NetBulbColumns.TYPE_COLUMN + " = ?  AND "
        + DatabaseDefinitions.NetBulbColumns.CONNECTION_DATABASE_ID + " = ?";
    String[]
        selectionArgs =
        {"" + DatabaseDefinitions.NetBulbColumns.NetBulbType.LIFX, "" + mBaseId};
    Cursor cursor =
        c.getContentResolver()
            .query(DatabaseDefinitions.NetBulbColumns.URI, bulbColumns, selection, selectionArgs,
                   null);
    cursor.moveToFirst();
    Long bulbBaseId = cursor.getLong(0);
    String bulbName = cursor.getString(3);
    String bulbDeviceId = cursor.getString(4);
    LifxBulb.ExtraData bulbData = gson.fromJson(cursor.getString(5), LifxBulb.ExtraData.class);
    int currentMaxBri = cursor.getInt(6);
    mBulb = new LifxBulb(c, bulbBaseId, bulbName, bulbDeviceId, bulbData, this, currentMaxBri);
  }

  @Override
  public void initializeConnection(Context c) {

  }

  @Override
  public void onDestroy() {
    lightDisconnected();
  }

  @Override
  public ArrayList<NetworkBulb> getBulbs() {
    ArrayList<NetworkBulb> result = new ArrayList<NetworkBulb>(1);
    result.add(mBulb);
    return result;
  }

  @Override
  public String mainDescription() {
    return mName;
  }

  @Override
  public String subDescription() {
    return mContext.getString(R.string.device_lifx);
  }

  @Override
  public boolean hasPendingWork() {
    return false;
  }

  @Override
  public void delete() {

  }

  public String getDeviceId() {
    return mDeviceId;
  }

  public void lightConnected(LFXLight light) {
    mLight = light;
    this.mBulb.lightConnected(light);
  }

  public void lightDisconnected() {
    this.mBulb.lightDisconnected();
    mLight = null;
  }

  public static class ExtraData {

  }
}
