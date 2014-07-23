package com.kuxhausen.huemore.net.lifx;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.persistence.Definitions;

import java.util.ArrayList;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes;
import lifx.java.android.light.LFXLight;
import lifx.java.android.light.LFXLightCollection;
import lifx.java.android.network_context.LFXNetworkContext;

public class LifxRegistrationDialog extends DialogFragment implements
                                                           LFXLightCollection.LFXLightCollectionListener,
                                                           LoaderManager.LoaderCallbacks<Cursor> {

  private static final int LIFX_BULBS_LOADER = 0;
  private static final String[] columns = {Definitions.NetBulbColumns.DEVICE_ID_COLUMN};
  private static final String[]
      selectionArgs =
      {"" + Definitions.NetBulbColumns.NetBulbType.LIFX};

  ListView bulbsListView;

  private Context mContext;
  private LFXNetworkContext networkContext;
  private WifiManager.MulticastLock ml = null;
  private ArrayAdapter<String> candidateBulbsAdapter;
  private ArrayList<String> candidateBulbNames;
  private ArrayList<String> candidateBulbDeviceIds;
  private ArrayList<String> existingBulbDeviceIds;
  private Gson gson = new Gson();

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.mContext = activity;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    ml = wifi.createMulticastLock("lifx_samples_tag");
    ml.setReferenceCounted(true);
    ml.acquire();

    networkContext = LFXClient.getSharedInstance(mContext).getLocalNetworkContext();
    networkContext.getAllLightsCollection().addLightCollectionListener(this);
    networkContext.connect();

    // Use the Builder class for convenient dialog construction
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = getActivity().getLayoutInflater();
    View dialogMainView = inflater.inflate(R.layout.lifx_add_connections, null);
    bulbsListView = ((ListView) dialogMainView.findViewById(R.id.listView1));
    bulbsListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

    candidateBulbNames = new ArrayList<String>();
    candidateBulbDeviceIds = new ArrayList<String>();
    existingBulbDeviceIds = new ArrayList<String>();
    updateCandidateList();
    candidateBulbsAdapter =
        new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_multiple_choice,
                                 android.R.id.text1, candidateBulbNames);

    getLoaderManager().initLoader(LIFX_BULBS_LOADER, null, this);

    bulbsListView.setAdapter(candidateBulbsAdapter);

    builder.setTitle(R.string.dialog_title_searching_for_lights);

    builder.setView(dialogMainView);

    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        SparseBooleanArray set = bulbsListView.getCheckedItemPositions();
        for (int i = 0; i < candidateBulbNames.size(); i++) {
          //if that candidateLight is checked
          if (set.get(i)) {
            LFXLight
                selectedLight =
                networkContext.getAllLightsCollection()
                    .getLightWithDeviceID(candidateBulbDeviceIds.get(i));

            ContentValues netConnectionValues = new ContentValues();
            netConnectionValues.put(Definitions.NetConnectionColumns.TYPE_COLUMN,
                                    Definitions.NetBulbColumns.NetBulbType.LIFX);
            netConnectionValues.put(Definitions.NetConnectionColumns.JSON_COLUMN,
                                    gson.toJson(new LifxConnection.ExtraData()));
            netConnectionValues.put(Definitions.NetConnectionColumns.NAME_COLUMN,
                                    selectedLight.getLabel());
            netConnectionValues.put(Definitions.NetConnectionColumns.DEVICE_ID_COLUMN,
                                    selectedLight.getDeviceID());
            long connectionBaseId = Long.parseLong(mContext.getContentResolver().insert(
                Definitions.NetConnectionColumns.URI, netConnectionValues)
                                                       .getLastPathSegment());
            ;

            ContentValues netBulbValues = new ContentValues();
            netBulbValues
                .put(Definitions.NetBulbColumns.NAME_COLUMN, selectedLight.getLabel());
            netBulbValues.put(Definitions.NetBulbColumns.DEVICE_ID_COLUMN,
                              selectedLight.getDeviceID());
            netBulbValues
                .put(Definitions.NetBulbColumns.CONNECTION_DATABASE_ID, connectionBaseId);
            netBulbValues.put(Definitions.NetBulbColumns.JSON_COLUMN,
                              gson.toJson(new LifxBulb.ExtraData()));
            netBulbValues.put(Definitions.NetBulbColumns.TYPE_COLUMN,
                              Definitions.NetBulbColumns.NetBulbType.LIFX);
            netBulbValues.put(Definitions.NetBulbColumns.CURRENT_MAX_BRIGHTNESS, 100);
            long
                bulbBaseId =
                Long.parseLong(mContext.getContentResolver()
                                   .insert(Definitions.NetBulbColumns.URI, netBulbValues)
                                   .getLastPathSegment());


          }
        }

      }
    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
        // User cancelled the dialog
      }
    });
    // Create the AlertDialog object and return it
    return builder.create();
  }

  @Override
  public void onDestroy() {
    networkContext.disconnect();
    if (ml != null) {
      ml.release();
    }
    super.onDestroy();
  }

  private void updateCandidateList() {
    candidateBulbNames.clear();
    candidateBulbDeviceIds.clear();
    for (LFXLight light : networkContext.getAllLightsCollection().getLights()) {
      if (!existingBulbDeviceIds.contains(light.getDeviceID())) {
        candidateBulbNames.add(light.getLabel());
        candidateBulbDeviceIds.add(light.getDeviceID());
      }
    }
    bulbsListView.setAdapter(candidateBulbsAdapter);
  }

  @Override
  public void lightCollectionDidAddLight(LFXLightCollection lightCollection, LFXLight light) {
    updateCandidateList();
  }

  @Override
  public void lightCollectionDidRemoveLight(LFXLightCollection lightCollection, LFXLight light) {
    updateCandidateList();
  }

  @Override
  public void lightCollectionDidChangeLabel(LFXLightCollection lightCollection, String label) {
    updateCandidateList();
  }

  @Override
  public void lightCollectionDidChangeColor(LFXLightCollection lightCollection,
                                            LFXHSBKColor color) {
  }

  @Override
  public void lightCollectionDidChangeFuzzyPowerState(LFXLightCollection lightCollection,
                                                      LFXTypes.LFXFuzzyPowerState fuzzyPowerState) {
  }


  @Override
  public Loader<Cursor> onCreateLoader(int loaderID, Bundle arg1) {
    switch (loaderID) {
      case LIFX_BULBS_LOADER:
        // Returns a new CursorLoader
        return new CursorLoader(mContext,
                                Definitions.NetConnectionColumns.URI,
                                columns,
                                Definitions.NetConnectionColumns.TYPE_COLUMN + " = ?",
                                selectionArgs,
                                null
        );
      default:
        // An invalid id was passed in
        return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
    existingBulbDeviceIds.clear();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      cursor.moveToNext();
      existingBulbDeviceIds.add(cursor.getString(0));
    }
    updateCandidateList();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    if (existingBulbDeviceIds != null) {
      existingBulbDeviceIds.clear();
    }
  }
}
