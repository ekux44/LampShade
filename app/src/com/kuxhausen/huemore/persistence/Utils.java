package com.kuxhausen.huemore.persistence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kuxhausen.huemore.MoodExecuterService;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.InternalArguments;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.MoodColumns;
import com.kuxhausen.huemore.persistence.DatabaseDefinitions.PreferenceKeys;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class Utils {

	public static Mood getMoodFromDatabase(String moodName, Context ctx){
		String[] moodColumns = { MoodColumns.STATE };
		String[] mWhereClause = { moodName };
		Cursor moodCursor = ctx.getContentResolver().query(
				DatabaseDefinitions.MoodColumns.MOODS_URI, 
				moodColumns,
				MoodColumns.MOOD + "=?",
				mWhereClause,
				null
				);
		moodCursor.moveToFirst();
		try {
			return HueUrlEncoder.decode(moodCursor.getString(0)).second;
		} catch (InvalidEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (FutureEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static Mood generateSimpleMood(BulbState bs){
		//boilerplate
		Event e = new Event();
		e.channel=0;
		e.time=0;
		e.state=bs;
		Event[] eRay = {e};
		//more boilerplate
		Mood m = new Mood();
		m.numChannels=1;
		m.usesTiming = false;
		m.events = eRay;
		
		return m;
	}
	
	public static void transmit(Context context, String priority, Mood m, Integer[] bulbS, String optionalMoodName){
		Intent intent = new Intent(context, MoodExecuterService.class);
		intent.putExtra(priority, HueUrlEncoder.encode(m,bulbS));
		intent.putExtra(InternalArguments.MOOD_NAME, optionalMoodName);
        context.startService(intent);
	}
	
	public static boolean hasProVersion(Context c){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
		return settings.getInt(PreferenceKeys.BULBS_UNLOCKED,0) > PreferenceKeys.ALWAYS_FREE_BULBS;
	}
	
	public static Float[] xyFromRGB(int rgb){
		//convert rgb to separate r,g&b channels normalized to the range 0 through 1.0
		float red = ((rgb>>>16)&0xFF)/255f;
		float green = ((rgb>>>8)&0xFF)/255f;
		float blue = ((rgb)&0xFF)/255f;
		Log.e("colorspace","r"+red+" g"+green+" b"+blue);
		
		
		//perform arbitrary gamma correction against the sRRB we started with
		/*Apply a gamma correction to the RGB values, which makes the color more vivid
		 *  and more the like the color displayed on the screen of your device. This gamma correction 
		 *  is also applied to the screen of your computer or phone, thus we need this to create the 
		 *  same color on the light as on screen. This is done by the following formulas:*/
		red = (float) ((red > 0.04045f) ? Math.pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f));
		green = (float) ((green > 0.04045f) ? Math.pow((green + 0.055f) / (1.0f + 0.055f), 2.4f) : (green / 12.92f));
		blue = (float) ((blue > 0.04045f) ? Math.pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f));
		Log.e("colorspace","after gamma correction r"+red+" g"+green+" b"+blue);
		
		//Convert the RGB values to XYZ using the Wide RGB D65 conversion formula
		float X = red * 0.649926f + green * 0.103455f + blue * 0.197109f; 
		float Y = red * 0.234327f + green * 0.743075f + blue * 0.022598f;
		float Z = red * 0.0000000f + green * 0.053077f + blue * 1.035763f;
		Log.e("colorspace","X"+X+" Y"+Y+" Z"+Z);
		
		//Calculate the xy values from the XYZ values
		float x = X / (X + Y + Z); 
		float y = Y / (X + Y + Z);
		Log.e("colorspace", "x"+x+" y"+y);
		
		//TODO consider clipping to range of producable light
		
		Float[] result = {x, y};
		Log.e("colorspace", "x"+result[0]+" y"+result[1]);
		return result;
	}
	
	public static int rgbFromXY(Float[] xy){
		
		Log.e("colorspace", "x"+xy[0]+" y"+xy[1]);
		
		xy[0] = Math.min(.9999f, Math.max(xy[0],.0001f));
		xy[1] = Math.min(.9999f, Math.max(xy[1],.0001f));
		
		Log.e("colorspace", "x"+xy[0]+" y"+xy[1]);
		
		float brightness = .1f; //todo consider supporting other greylevels
		
		float x = xy[0]; // the given x value
		float y = xy[1]; // the given y value
		float z = 1.0f - x - y; 
		float Y = brightness; // The given brightness value normalized on 0 to 1.0
		float X = (Y / y) * x;  
		float Z = (Y / y) * z;
		
		Log.e("revColorspace","X"+X+" Y"+Y+" Z"+Z);
		
		//Convert to RGB using Wide RGB D65 conversion
		float r = X * 1.612f - Y * 0.203f - Z * 0.302f;
		float g = -X * 0.509f + Y * 1.412f + Z * 0.066f;
		float b = X * 0.026f - Y * 0.072f + Z * 0.962f;
		
		Log.e("colorspace","pre revgamma correction r"+r+" g"+g+" b"+b);
		
		//Apply reverse sRGB gamma correction
		r = (float) (r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * Math.pow(r, (1.0f / 2.4f)) - 0.055f);
		g = (float) (g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * Math.pow(g, (1.0f / 2.4f)) - 0.055f);
		b = (float) (b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * Math.pow(b, (1.0f / 2.4f)) - 0.055f);
		
		Log.e("revColorspace","r"+r+" g"+g+" b"+b);
		
		//convert scale back to 0-255
		r = Math.max(0, Math.min(r*0xFF, 0xFF));
		g = Math.max(0, Math.min(g*0xFF, 0xFF));
		b = Math.max(0, Math.min(b*0xFF, 0xFF));
		
		//reconstruct rgb int
		int rgb = ((int)(r)<<16)+((int)(g)<<8)+((int)(b));
		
		return rgb;
	}
	
}
