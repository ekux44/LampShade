package com.kuxhausen.huemore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class CommunityDialogFragment extends SherlockDialogFragment implements
OnClickListener{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.community_dialog_fragment, container, false);
		
		Button facebookButton = (Button) myView.findViewById(R.id.facebookButton);
		facebookButton.setOnClickListener(this);
		
		Button googleButton = (Button) myView.findViewById(R.id.googleButton);
		googleButton.setOnClickListener(this);
		
		Button twitterButton = (Button) myView.findViewById(R.id.twitterButton);
		twitterButton.setOnClickListener(this);
		
		this.getDialog().setTitle(R.string.action_communities);
		
		return myView;
	}
	
	@Override
	public void onClick(View v) {
		String url; 
		Intent i = new Intent(Intent.ACTION_VIEW);
		
		switch (v.getId()) {
		case R.id.facebookButton:
			url = "https://www.facebook.com/LampShade.io";
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		case R.id.googleButton:
			url = "https://plus.google.com/communities/117876087643455039742";
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		case R.id.twitterButton:
			url = "https://twitter.com/LampShadeIO";
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		}
		
	}

}
