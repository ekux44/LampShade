package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class NewGroupDialogFragment extends DialogFragment {
	
	ArrayList mSelectedItems;
	public static String[] dummyArrayItems = {"Bulb 1", "Bulb 2", "Bulb3"};
	ListView bulbsListView;
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View groupDialogView = inflater.inflate(R.layout.edit_group_dialog, null);
        bulbsListView = ((ListView)groupDialogView.findViewById(R.id.listView1));
        bulbsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        bulbsListView.setAdapter(new ArrayAdapter(this.getActivity(),android.R.layout.simple_list_item_multiple_choice,dummyArrayItems));
        builder.setView(groupDialogView);
        
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //bulbsListView.getCheckedItemPositions();
                	   //bulbsListView.getCheckedItemIds();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
