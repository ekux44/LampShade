package com.kuxhausen.huemore.testing;

import java.util.BitSet;

import android.util.Log;
import android.util.Pair;

import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class Tests {
	
	public static Boolean test(int tNum, Mood m1, Integer[] b1){
		
		Mood m2 = null;
		Integer[] b2 = null;
		if(b1!=null){
			m2 = HueUrlEncoder.decode(HueUrlEncoder.encode(m1, b1)).second;
			b2 = HueUrlEncoder.decode(HueUrlEncoder.encode(m1, b1)).first;
		}
		else
			m2 = HueUrlEncoder.decode(HueUrlEncoder.encode(m1)).second;
		
		if(b1!=null){
			if(b1.length!=b2.length){
				Log.e("tests",tNum+"bulbLengthNotEqual");
			}
			for(int i = 0; i<b1.length; i++)
				if(b1[i]!=b2[i]){
					Log.e("tests",tNum+"bulb@spot"+i+" FlagNotEqual");
					return false;
				}
		}
		if(m1.isInfiniteLooping()!=m2.isInfiniteLooping()){
			Log.e("tests",tNum+"infiniteLoopingNotEqual");
			return false;
		}
		if(m1.timeAddressingRepeatPolicy!=m2.timeAddressingRepeatPolicy){
			Log.e("tests",tNum+"timeAddressingRepeatPolicyNotEqual");
			return false;
		}
		if(m1.usesTiming!=m2.usesTiming){
			Log.e("tests",tNum+"usesTimingNotEqual");
			return false;
		}
		if(m1.numChannels!=m2.numChannels){
			Log.e("tests",tNum+"numChannelsNotEqual");
			return false;
		}
		if(m1.getNumLoops()!=m2.getNumLoops()){
			Log.e("tests",tNum+"numLoopsNotEqual");
			return false;
		}
		if(m1.events.length!=m2.events.length){
			Log.e("tests",tNum+"numEventsNotEqual");
			return false;
		}
		for(int i = 0; i<m1.events.length; i++){
			if(m1.events[i].channel!=m2.events[i].channel){
				Log.e("tests",tNum+"event"+i+"ChannelNotEqual");
				return false;
			}
			if(m1.events[i].time!=m2.events[i].time){
				Log.e("tests",tNum+"event"+i+"TimeNotEqual");
				return false;
			}
			if(!m1.events[i].state.toString().equals(m2.events[i].state.toString())){
				Log.e("tests",m1.events[i].state.toString());
				Log.e("tests",m2.events[i].state.toString());
				Log.e("tests",tNum+"event"+i+"StateNotEqual");
				return false;
			}
		}
		
		return true;
	}
	
	public static void tests() {
		
		BulbState bs = new BulbState();
		bs.on=true;
		bs.bri=80;
		
		Event e1 = new Event();
		e1.state = bs;
		e1.channel = 0;
		e1.time=0;
		
		Event e2 = new Event();
		e2.state = bs;
		e2.channel = 1;
		e2.time=0;
		
		Event e3 = new Event();
		e3.state = bs;
		e3.channel = 2;
		e3.time=0;
		
		Event[] eRay = {e1,e2,e3,e1,e1,e3, e2};
		Mood m = new Mood();
		Log.e("tests","1"+test(1,m,null));
		m.numChannels=3;
		m.events = eRay;
		Log.e("tests","2"+test(2,m,null));
		e3.time = 2;
		m.usesTiming=true;
		Log.e("tests","3"+test(3,m,null));
		m.setNumLoops(98);
		Log.e("tests","4"+test(4,m,null));
		m.setInfiniteLooping(true);
		Log.e("tests","5"+test(5,m,null));
		m.timeAddressingRepeatPolicy=true;
		Log.e("tests","6"+test(6,m,null));
		Integer[] bulbs = {1, 3, 14};
		Log.e("tests","7"+test(7,m,bulbs));
		
		/** bitSet to encoding test **/
		/*{
			BitSet b = new BitSet();
			for (int i = 0; i < 10000; i++) {
				if (Math.random() < .5)
					b.set(i);
			}
			byte[] intermediate = com.kuxhausen.huemore.persistence.HueUrlEncoder
					.fromBitSet(b, 10000);
			BitSet b2 = com.kuxhausen.huemore.persistence.HueUrlEncoder
					.toBitSet(intermediate);
			for (int i = 0; i < 10000; i++)
				if (b.get(i) != b2.get(i))
					System.out.println(i + " " + b.get(i) + " " + b2.get(i)
							+ " " + intermediate[i / 8]);
			System.out.println("bitSet-Byte[] testComplete");
		}
		{
			Integer[] bulbs = { 1, 4, 8, 3, 43 };
			BulbState[] bsRay = new BulbState[4];
			BulbState one = new BulbState();
			one.on = true;
			one.bri = 1;
			one.ct = 2;
			one.effect = "none";
			one.hue = 4;
			one.sat = 5;
			one.transitiontime = 6;
			one.alert = "none";
			bsRay[0] = one;

			BulbState two = new BulbState();
			two.on = false;
			two.effect = "none";
			bsRay[1] = two;

			BulbState three = new BulbState();
			three.on = true;
			three.sat = 255;
			three.bri = 255;
			three.hue = 0;
			bsRay[2] = three;

			BulbState four = new BulbState();
			four.on = false;
			four.bri = 1;
			four.alert = "select";
			four.transitiontime = 10;
			four.effect = "colorloop";
			// four.transitiontime =0;
			bsRay[3] = four;

			String interm = com.kuxhausen.huemore.persistence.HueUrlEncoder.legacyEncode(
					bulbs, bsRay);
			System.out.println(interm);
			Pair<Integer[], BulbState[]> results = com.kuxhausen.huemore.persistence.HueUrlEncoder
					.legacyDecode(interm);
			System.out.println("resultSize" + results.first.length + "  "
					+ results.second.length);

			for (int i : results.first) {
				System.out.print(i + "  ");
			}
			System.out.println();
			for (BulbState j : results.second) {
				if (j != null)
					System.out.println(j);
				else
					System.out.println("wtf-null ");
			}
		}*/
	}
}
