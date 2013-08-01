package com.kuxhausen.huemore.testing;

import java.util.BitSet;

import android.util.Log;
import android.util.Pair;

import com.kuxhausen.huemore.persistence.HueUrlEncoder;
import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class Tests {
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
		
		Event[] eRay = {e1,e2,e1,e1,e1,e3};
		Mood m = new Mood();
		m.events = eRay;
		m.infiniteLooping=false;
		m.numChannels=3;
		m.numLoops=0;
		m.timeAddressingRepeatPolicy=false;
		m.usesTiming=false;
		
		
		
		Log.e("test1",HueUrlEncoder.encode(m));
		
		
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
