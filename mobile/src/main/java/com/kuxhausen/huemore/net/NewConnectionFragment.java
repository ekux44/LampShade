package com.kuxhausen.huemore.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kuxhausen.huemore.NetworkManagedActivity;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.net.hue.ui.DiscoverHubDialogFragment;
import com.kuxhausen.huemore.net.lifx.LifxRegistrationDialog;
import com.kuxhausen.huemore.persistence.Definitions.InternalArguments;

public class NewConnectionFragment extends DialogFragment implements OnItemClickListener {

  private NetworkManagedActivity mParent;
  private String[] deviceTypes;
  private ListView mTypeList;

  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    String selectedText = ((TextView) v).getText().toString();

    if (selectedText.equals(mParent.getString(R.string.device_hue))) {
      DiscoverHubDialogFragment dialogFrag = new DiscoverHubDialogFragment();
      dialogFrag.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);

    } else if (selectedText.equals(mParent.getString(R.string.device_lifx))) {
      LifxRegistrationDialog dialogFrag = new LifxRegistrationDialog();
      dialogFrag.show(getFragmentManager(), InternalArguments.FRAG_MANAGER_DIALOG_TAG);
    } else if (selectedText.equals(mParent.getString(R.string.device_lightpack))) {

    }
    this.dismiss();
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception.
    try {
      mParent = (NetworkManagedActivity) activity;
    } catch (ClassCastException e) {
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();

    View myView = inflater.inflate(R.layout.new_connection_list_fragment, null);

    mTypeList = (ListView) myView.findViewById(android.R.id.list);
    mTypeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    int layout =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        ? android.R.layout.simple_list_item_activated_1
        : android.R.layout.simple_list_item_1;
    deviceTypes = mParent.getResources().getStringArray(R.array.add_devices_list);
    ArrayAdapter<String> aa = new ArrayAdapter<String>(mParent, layout, deviceTypes);
    mTypeList.setAdapter(aa);

    mTypeList.setOnItemClickListener(this);

    builder.setView(myView);
    builder.setTitle(R.string.prompt_new_connection_type);

    return builder.create();
  }
}
