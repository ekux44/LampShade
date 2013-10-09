package com.kuxhausen.huemore.editmood;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.kuxhausen.huemore.GodObject;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class EditColorWheelFragment extends SherlockFragment implements
		OnCreateColorListener, com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener {

	public interface OnColorChangedListener {
		void colorChanged(int color, int hue);

		float getSaturation();
	}

	ColorPicker picker;
	SaturationBar saturationBar;
	private BulbState hs = new BulbState();
	{
		hs.on = true;
		hs.effect = "none";
		Float[] lol = {.5f, .5f};
		hs.xy = lol;//TODO change

	}
	Gson gson = new Gson();

	CompoundButton colorLoop;
	
	LinearLayout colorLoopLayout, transitionLayout;
	boolean colorLoopLayoutVisible = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color, null);

		picker = (ColorPicker) groupDialogView.findViewById(R.id.picker);
		saturationBar = (SaturationBar) groupDialogView.findViewById(R.id.saturationbar);
		picker.addSaturationBar(saturationBar);


		Bundle args = getArguments();
		if (args != null && args.containsKey(InternalArguments.PREVIOUS_STATE)) {
			BulbState bs = gson.fromJson(
					args.getString(InternalArguments.PREVIOUS_STATE),
					BulbState.class);
			loadPrevious(bs);
		}
		loadPrevious(hs);
		
		if (colorLoopLayoutVisible) {
			colorLoop = (CompoundButton) groupDialogView
					.findViewById(R.id.colorLoopCompoundButton);
			colorLoopLayout = (LinearLayout) groupDialogView
					.findViewById(R.id.colorLoopLayout);
		} else {
			groupDialogView.findViewById(R.id.colorLoopLayout).setVisibility(
					View.GONE);
		}
		
		return groupDialogView;
	}
	
	public void loadPrevious(BulbState bs){
		if (bs.hue != null && bs.sat!=null){
			
			float[] hsv = { (bs.hue * 360) / 65535, bs.sat / 255f, 1 };
			hs.xy = Utils.hsTOxy(hsv[0]/360f, hsv[1]);
			
			picker.setColor(Color.HSVToColor(hsv));
			picker.setOldCenterColor(Color.HSVToColor(hsv));
			saturationBar.setSaturation(hsv[1]);
		}
		if(bs.xy!=null){
			hs.xy = bs.xy;
			
			Float[] hueSat = Utils.xyTOhs(bs.xy[0], bs.xy[1]);
			float[] hsv = {hueSat[0]*360, hueSat[1], 1f};
			
			int rgb = Color.HSVToColor(hsv);
			
			picker.setColor(rgb);
			picker.setOldCenterColor(rgb);
			
			saturationBar.setSaturation(hsv[1]);
		}
	}
	public void onStart(){
		super.onStart();
		picker.setOnColorChangedListener(this);
	}
	
	public void hideColorLoop() {
		colorLoopLayoutVisible = false;
		colorLoop = null;
		if (colorLoopLayout != null)
			colorLoopLayout.setVisibility(View.GONE);
	}

	public void preview() {
		if(isAdded()){
			
			Mood m = Utils.generateSimpleMood(hs);
			Utils.transmit(this.getActivity(), InternalArguments.ENCODED_TRANSIENT_MOOD, m, ((GodObject)this.getActivity()).getBulbs(), null);
		}
	}

	@Override
	public Intent onCreateColor(Integer transitionTime) {
		this.onColorChanged(this.picker.getColor());
		
		hs.transitiontime = transitionTime;
		Intent i = new Intent();
		i.putExtra(InternalArguments.HUE_STATE, gson.toJson(hs));
		return i;
	}

	@Override
	public void onColorChanged(int rgb) {
		picker.setOldCenterColor(rgb);
		
		float[] hsv = new float[3];
		int red = ((rgb>>>16)&0xFF);
		int green = ((rgb>>>8)&0xFF);
		int blue = ((rgb)&0xFF);
		Color.RGBToHSV(red, green, blue, hsv);
		
		hs.xy = Utils.hsTOxy(hsv[0]/360f, hsv[1]);
		hs.hue = null;
		hs.sat = null;
		preview();
	}
}
