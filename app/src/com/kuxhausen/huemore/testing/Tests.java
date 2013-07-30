package com.kuxhausen.huemore.testing;

import java.util.BitSet;

import android.util.Pair;

import com.kuxhausen.huemore.state.api.BulbState;

public class Tests {
	public void tests() {
		/** bitSet to encoding test **/
		{
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

			String interm = com.kuxhausen.huemore.persistence.HueUrlEncoder.encode(
					bulbs, bsRay);
			System.out.println(interm);
			Pair<Integer[], BulbState[]> results = com.kuxhausen.huemore.persistence.HueUrlEncoder
					.decode(interm);
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
		}
	}
}
