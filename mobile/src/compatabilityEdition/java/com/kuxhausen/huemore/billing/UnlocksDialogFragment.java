package com.kuxhausen.huemore.billing;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kuxhausen.huemore.R;

public class UnlocksDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.unlocks_fragment, container, false);
        this.getDialog().setTitle(R.string.action_unlocks);

        return myView;
    }

}
