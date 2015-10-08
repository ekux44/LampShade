package com.kuxhausen.huemore.net.lifx;

import com.google.gson.Gson;

import android.content.Context;
import android.database.Cursor;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.Connection;
import com.kuxhausen.huemore.net.DeviceManager;
import com.kuxhausen.huemore.net.NetworkBulb;
import com.kuxhausen.huemore.persistence.Definitions;

import java.util.ArrayList;

import lifx.java.android.light.LFXLight;

public class LifxConnection implements Connection {

  private static final String[] bulbColumns = {Definitions.NetBulbColumns._ID,
                                               Definitions.NetBulbColumns.CONNECTION_DATABASE_ID,
                                               Definitions.NetBulbColumns.TYPE_COLUMN,
                                               Definitions.NetBulbColumns.NAME_COLUMN,
                                               Definitions.NetBulbColumns.DEVICE_ID_COLUMN,
                                               Definitions.NetBulbColumns.JSON_COLUMN,
                                               Definitions.NetBulbColumns.CURRENT_MAX_BRIGHTNESS};
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
        Definitions.NetBulbColumns.TYPE_COLUMN + " = ?  AND "
        + Definitions.NetBulbColumns.CONNECTION_DATABASE_ID + " = ?";
    String[]
        selectionArgs =
        {"" + Definitions.NetBulbColumns.NetBulbType.LIFX, "" + mBaseId};
    Cursor cursor =
        c.getContentResolver()
            .query(Definitions.NetBulbColumns.URI, bulbColumns, selection, selectionArgs,
                   null);
    cursor.moveToFirst();
    Long bulbBaseId = cursor.getLong(0);
    String bulbName = cursor.getString(3);
    String bulbDeviceId = cursor.getString(4);
    LifxBulb.ExtraData bulbData = gson.fromJson(cursor.getString(5), LifxBulb.ExtraData.class);
    int currentMaxBri = cursor.getInt(6);
    cursor.close();

    mBulb = new LifxBulb(c, bulbBaseId, bulbName, bulbDeviceId, bulbData, this);
  }

  @Override
  public void initializeConnection(Context c) {
    mBulb.onInitialize();
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
    return this.mBulb.hasPendingWork();
  }

  @Override
  public void delete() {
    this.onDestroy();

    String selector = Definitions.NetConnectionColumns._ID + "=?";
    String[] selectionArgs = {"" + mBaseId};
    mContext.getContentResolver()
        .delete(Definitions.NetConnectionColumns.URI, selector, selectionArgs);
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

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }

  public static class ExtraData {

  }
}
