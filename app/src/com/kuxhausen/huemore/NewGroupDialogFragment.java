package com.kuxhausen.huemore;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class NewGroupDialogFragment extends DialogFragment {
	
	ArrayList mSelectedItems;
	public static String[] dummyArrayItems = {"Bulb 1", "Bulb 2", "Bulb3"};
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        mSelectedItems = new ArrayList();  // Where we track the selected items
        builder.setMultiChoiceItems(dummyArrayItems, null,
                new DialogInterface.OnMultiChoiceClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which,
                 boolean isChecked) {
             if (isChecked) {
                 // If the user checked the item, add it to the selected items
                 mSelectedItems.add(which);
             } else if (mSelectedItems.contains(which)) {
                 // Else, if the item is already in the array, remove it 
                 mSelectedItems.remove(Integer.valueOf(which));
             }
         }
     });
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
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
