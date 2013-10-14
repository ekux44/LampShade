package com.kuxhausen.huemore.editmood;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.kuxhausen.huemore.R;
import com.kuxhausen.huemore.editmood.EditStatePagerDialogFragment.OnCreateColorListener;
import com.kuxhausen.huemore.persistence.Utils;
import com.kuxhausen.huemore.state.api.BulbState;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SaturationBar;

public class EditColorWheelFragment extends SherlockFragment implements
		OnCreateColorListener, com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener {

	ColorPicker picker;
	SaturationBar saturationBar;
	EditStatePagerDialogFragment statePager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View myView = inflater.inflate(R.layout.edit_hue_color, null);

		picker = (ColorPicker) myView.findViewById(R.id.picker);
		saturationBar = (SaturationBar) myView.findViewById(R.id.saturationbar);
		picker.addSaturationBar(saturationBar);
		picker.setOnColorChangedListener(this);
		
		myView.findViewById(R.id.colorLoopLayout).setVisibility(View.GONE);
		
		return myView;
	}

	@Override
	public void onColorChanged(int rgb) {
		picker.setOldCenterColor(rgb);
		
		float[] newHSV = new float[3];
		int red = ((rgb>>>16)&0xFF);
		int green = ((rgb>>>8)&0xFF);
		int blue = ((rgb)&0xFF);
		Color.RGBToHSV(red, green, blue, newHSV);
		Float[] newHueSat = {newHSV[0]/360f, newHSV[1]};
		Float[] newXY = Utils.hsTOxy(newHueSat);
		
		BulbState state = statePager.getState();
		
		state.xy = newXY;
		state.hue = null;
		state.sat = null;
		state.ct = null;
		if(EditStatePagerDialogFragment.currentPage == EditStatePagerDialogFragment.WHEEL_PAGE)
			statePager.setState(state, this, "wheel");
	}

	@Override
	public boolean stateChanged() {
		BulbState state = statePager.getState();
		if (state.hue != null && state.sat!=null){
			
			float[] hsv = { (state.hue * 360) / 65535, state.sat / 255f, 1 };
			Float[] input = {hsv[0]/360f, hsv[1]};
			state.xy = Utils.hsTOxy(input);
			
			int rgb = Color.HSVToColor(hsv);
			if(picker!=null&&saturationBar!=null){
				picker.setOnColorChangedListener(null);
				picker.setColor(rgb);
				picker.setOldCenterColor(rgb);
				saturationBar.setSaturation(hsv[1]);
				picker.setOnColorChangedListener(this);
				picker.invalidate();
			}
			
			state.hue = null;
			state.sat = null;
			return true;
		}
		if(state.xy!=null){
			Float[] hueSat = Utils.xyTOhs(state.xy);
			float[] hsv = {hueSat[0]*360, hueSat[1], 1f};
			
			int rgb = Color.HSVToColor(hsv);
			
			if(picker!=null&&saturationBar!=null){
				picker.setOnColorChangedListener(null);
				picker.setColor(rgb);
				picker.setOldCenterColor(rgb);
				saturationBar.setSaturation(hsv[1]);
				picker.setOnColorChangedListener(this);
				picker.invalidate();
			}
			return true;
		}
		return false;
	}
	@Override
	public void setStatePager(EditStatePagerDialogFragment statePage) {
		statePager = statePage;		
	}
}
