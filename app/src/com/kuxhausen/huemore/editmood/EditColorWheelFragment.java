package com.kuxhausen.huemore.editmood;

import android.graphics.Color;
import android.os.Bundle;
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

		View groupDialogView = inflater.inflate(R.layout.edit_hue_color, null);

		picker = (ColorPicker) groupDialogView.findViewById(R.id.picker);
		saturationBar = (SaturationBar) groupDialogView.findViewById(R.id.saturationbar);
		picker.addSaturationBar(saturationBar);

		groupDialogView.findViewById(R.id.colorLoopLayout).setVisibility(View.GONE);
		
		return groupDialogView;
	}
	
	public void onStart(){
		super.onStart();
		picker.setOnColorChangedListener(this);
	}

	@Override
	public void onColorChanged(int rgb) {
		picker.setOldCenterColor(rgb);
		
		float[] hsv = new float[3];
		int red = ((rgb>>>16)&0xFF);
		int green = ((rgb>>>8)&0xFF);
		int blue = ((rgb)&0xFF);
		Color.RGBToHSV(red, green, blue, hsv);
		
		statePager.getState().xy = Utils.hsTOxy(hsv[0]/360f, hsv[1]);
		statePager.getState().hue = null;
		statePager.getState().sat = null;
		statePager.getState().ct = null;
		statePager.stateChanged(this);
	}

	@Override
	public boolean stateChanged() {
		BulbState previous = statePager.getState();
		if (previous.hue != null && previous.sat!=null){
			
			float[] hsv = { (previous.hue * 360) / 65535, previous.sat / 255f, 1 };
			previous.xy = Utils.hsTOxy(hsv[0]/360f, hsv[1]);
			
			int rgb = Color.HSVToColor(hsv);
			if(picker!=null&&saturationBar!=null){
				picker.setColor(rgb);
				picker.setOldCenterColor(rgb);
				saturationBar.setSaturation(hsv[1]);
			}
			
			previous.hue = null;
			previous.sat = null;
			return true;
		}
		if(previous.xy!=null){
			Float[] hueSat = Utils.xyTOhs(previous.xy[0], previous.xy[1]);
			float[] hsv = {hueSat[0]*360, hueSat[1], 1f};
			
			int rgb = Color.HSVToColor(hsv);
			
			if(picker!=null&&saturationBar!=null){
				picker.setColor(rgb);
				picker.setOldCenterColor(rgb);
				saturationBar.setSaturation(hsv[1]);
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
