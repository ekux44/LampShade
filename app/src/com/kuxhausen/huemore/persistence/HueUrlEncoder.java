package com.kuxhausen.huemore.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Base64;
import android.util.Pair;

import com.kuxhausen.huemore.state.Event;
import com.kuxhausen.huemore.state.Mood;
import com.kuxhausen.huemore.state.api.BulbState;

public class HueUrlEncoder {

	public final static Integer PROTOCOL_VERSION_NUMBER = 2;
	
	public static String encode(Mood mood){
		if (mood == null)
			return "";
		
		ManagedBitSet mBitSet = new ManagedBitSet();
		
		// Set 4 bit protocol version
		addNumber(mBitSet,PROTOCOL_VERSION_NUMBER,4);
		
		// Set 6 bit number of channels
		addNumber(mBitSet,mood.numChannels,6);
		
		addTimingRepeatPolicy(mBitSet, mood);
		
		ArrayList<Integer> timeArray = generateTimesArray(mood);
		// Set 6 bit number of timestamps
		addNumber(mBitSet,timeArray.size(),6);
		// Set variable size list of 20 bit timestamps
		for(Integer i : timeArray)
			addNumber(mBitSet,i,20);

		BulbState[] stateArray = generateStatesArray(mood);
		// Set 6 bit number of states
		addNumber(mBitSet,stateArray.length,6);
		
		for(BulbState state : stateArray)
			addState(mBitSet, state);
		
		// Set 8 bit number of events
		addNumber(mBitSet,mood.events.length,8);
		
		addListOfEvents(mBitSet, mood, timeArray, stateArray);		
		return mBitSet.getBase64Encoding();
	}
	
	/** Set 8 bit timing repeat policy **/
	private static void addTimingRepeatPolicy(ManagedBitSet mBitSet, Mood mood){
		//1 bit timing addressing reference mode
		mBitSet.incrementingSet(mood.timeAddressingRepeatPolicy);
		
		//7 bit timing repeat number (max value specialcased to infinity)
		if(mood.infiniteLooping){
			//if infinite looping, write max value
			addNumber(mBitSet,127,7);
		}else
			addNumber(mBitSet,mood.numLoops,6);
	}
	
	
	/** Set variable length state **/
	private static void addState(ManagedBitSet mBitSet, BulbState bs){
		/** Put 9 bit properties flags **/
		{
			// On/OFF flag always include in v1 implementation 1
			mBitSet.incrementingSet(true);
			
			// Put bri flag
			mBitSet.incrementingSet(bs.bri != null);
			
			// Put hue flag
			mBitSet.incrementingSet(bs.hue != null);
			
			// Put sat flag
			mBitSet.incrementingSet(bs.sat != null);

			// Put xy flag
			mBitSet.incrementingSet(bs.xy != null);

			// Put ct flag
			mBitSet.incrementingSet(bs.ct != null);

			// Put alert flag
			mBitSet.incrementingSet(bs.alert != null);

			// Put effect flag
			mBitSet.incrementingSet(bs.effect != null);

			// Put transitiontime flag
			mBitSet.incrementingSet(bs.transitiontime != null);
		}
		/** Put on bit **/
		// On/OFF flag always include in v1 implementation 1
		mBitSet.incrementingSet(bs.on);
		
		/** Put 8 bit bri **/	
		if (bs.bri != null) {
			addNumber(mBitSet,bs.bri,8);
		}
		
		/** Put 16 bit hue **/
		if (bs.hue != null) {
			addNumber(mBitSet,bs.hue,16);
		}

		/** Put 8 bit sat **/
		if (bs.sat != null) {
			addNumber(mBitSet,bs.sat,8);
		}
		
		/** Put 64 bit xy **/
		if (bs.xy != null) {
			int x = Float
					.floatToIntBits((float) ((double) bs.xy[0]));
			addNumber(mBitSet,x,32);

			int y = Float
					.floatToIntBits((float) ((double) bs.xy[1]));
			addNumber(mBitSet,y,32);
		}
		
		/** Put 9 bit ct **/
		if (bs.ct != null) {
			addNumber(mBitSet,bs.ct,9);
		}
		
		/** Put 2 bit alert **/
		if (bs.alert != null) {
			int value = 0;
			if (bs.alert.equals("none"))
				value = 0;
			else if (bs.alert.equals("select"))
				value = 1;
			else if (bs.alert.equals("lselect"))
				value = 2;

			addNumber(mBitSet,value,2);
		}
		
		/** Put 4 bit effect **/
		// three more bits than needed, reserved for future API
		// functionality
		if (bs.effect != null) {
			int value = 0;
			if (bs.effect.equals("none"))
				value = 0;
			else if (bs.effect.equals("colorloop"))
				value = 1;
			
			addNumber(mBitSet,value,4);
		}
		
		/** Put 16 bit transitiontime **/
		if (bs.transitiontime != null) {
			addNumber(mBitSet,bs.transitiontime,16);
		}
	}
	
	/** Set variable length list of variable length events **/
	private static void addListOfEvents(ManagedBitSet mBitSet, Mood mood, ArrayList<Integer> timeArray, BulbState[] stateArray){
		String[] bulbStateToStringArray = new String[stateArray.length];
		for(int i = 0; i< stateArray.length; i++){
			bulbStateToStringArray[i] = stateArray[i].toString();
		}
		ArrayList<String> bulbStateToStringList = new ArrayList<String>(Arrays.asList(bulbStateToStringArray));
		for(Event e: mood.events){
			
			// add channel number
			addNumber(mBitSet, e.channel, getBitLength(mood.numChannels));
			
			//add timestamp lookup number
			addNumber(mBitSet, timeArray.indexOf(e.time), getBitLength(timeArray.size()));
			
			//add mood lookup number
			addNumber(mBitSet, bulbStateToStringList.indexOf(e.state.toString()), getBitLength(stateArray.length));
		}
	}
	
	/** calulate number of bits needed to address this many addresses **/
	private static int getBitLength(int addresses){
		int length=0;
		while(addresses!=0){
			addresses = addresses>>>1;
			length++;
		}
		return length;
	}
	
	private static ArrayList<Integer> generateTimesArray(Mood mood){
		HashSet<Integer> timeset = new HashSet<Integer>();
		for(Event e : mood.events){
			timeset.add(e.time);
		}
		return new ArrayList<Integer>(Arrays.asList((Integer[])timeset.toArray()));
	}
	
	private static BulbState[] generateStatesArray(Mood mood){
		HashMap<String, BulbState> statemap = new HashMap<String, BulbState>();
		for(Event e : mood.events){
			statemap.put(e.state.toString(), e.state);
		}
		return (BulbState[])statemap.values().toArray();
	}
	
	/**
	 * @param set
	 * @param index
	 * @param value
	 * @param length
	 */
	private static void addNumber(ManagedBitSet mBitSet, int value, int length){
		int bitMask = (int)Math.pow(2, length-1);
		for (int i = length-1; i >= 0; i--) {
			mBitSet.incrementingSet(((value & bitMask) > 0));
			bitMask /= 2;
		}
	}
	
	public static Pair<Integer[], Mood> decode(String code){
		//TODO		
		return null;
	}
	
	/**
	 * 4 bit version header.
	 * <p>
	 * 50 bit bulbs included flags.
	 * <p>
	 * 7 bit number of states.
	 * <p>
	 * STATE
	 * <p>
	 * Each state contains:
	 * <p>
	 * 9 bit properties flagging inclusion of this order of properties:
	 * <p>
	 * 1 bit on.
	 * <p>
	 * 8 bit bri.
	 * <p>
	 * 16 bit hue.
	 * <p>
	 * 8 bit sat.
	 * <p>
	 * 64 bit xy.
	 * <p>
	 * 9 bit ct.
	 * <p>
	 * 2 bit alert.
	 * <p>
	 * 4 bit effect //three more bits than needed, reserved for future.
	 * <p>
	 * 16 bit transitiontime
	 * <p>
	 */
	public static String legacyEncode(Integer[] bulbS, BulbState[] bsRay) {
		//TODO replace
		return "";
	}

	public static Pair<Integer[], BulbState[]> legacyDecode(String encoded) {
		ArrayList<Integer> bList = null;
		BulbState[] bsRay = null;
		try {
			byte[] intermediaryReverse = Base64
					.decode(encoded, Base64.URL_SAFE);
			BitSet set = toBitSet(intermediaryReverse);
			bList = new ArrayList<Integer>();

			int index = 0;// points to the next spot
			/** Get protocol version **/
			{
				// Make sure the protocol is version 1 (0001)
				if (set.get(0) || set.get(1) || set.get(2) || !set.get(3)) {
					// Unsupported protocol version
					return new Pair(null, null);
				}
				index += 4;
			}
			/** Set bulbs flags version **/
			{

				for (int i = 0; i < 50; i++)
					if (set.get(index + i))
						bList.add(i + 1);
				index += 50;
			}
			/** Set num states **/
			{
				int numStates = 0;
				int bitMask = 1;
				for (int i = 0; i < 7; i++) {
					if (set.get(index))
						numStates |= bitMask;
					index++;
					bitMask *= 2;
				}
				bsRay = new BulbState[numStates];
			}

			/** Decode each state **/
			{
				for (int i = 0; i < bsRay.length; i++) {

					/**
					 * On, Bri, Hue, Sat, XY, CT, Alert, Effect, Transitiontime
					 */
					boolean[] propertiesFlags = new boolean[9];
					BulbState bs = new BulbState();
					/** Get 9 bit properties flags **/
					{
						for (int j = 0; j < 9; j++) {
							propertiesFlags[j] = set.get(index);
							index++;
						}

					}
					/** Get on bit **/
					{
						bs.on = set.get(index);
						index++;
					}
					/** Get 8 bit bri **/
					{
						if (propertiesFlags[1]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 8; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							bs.bri = value;
						}
					}
					/** Get 16 bit hue **/
					{
						if (propertiesFlags[2]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 16; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							bs.hue = value;
						}
					}

					/** Get 8 bit sat **/
					{
						if (propertiesFlags[3]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 8; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							bs.sat = (short) value;
						}
					}
					/** Get 64 bit xy **/
					{
						if (propertiesFlags[4]) {

							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 32; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							Double x = (double) Float.intBitsToFloat(value);
							value = 0;
							bitMask = 1;
							for (int j = 0; j < 32; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							Double y = (double) Float.intBitsToFloat(value);
							bs.xy = new Double[] { x, y };
						}
					}
					/** Get 9 bit ct **/
					{
						if (propertiesFlags[5]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 9; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							bs.ct = value;
						}
					}
					/** Get 2 bit alert **/
					{
						if (propertiesFlags[6]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 2; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							switch (value) {
							case 0:
								bs.alert = "none";
								break;
							case 1:
								bs.alert = "select";
								break;
							case 2:
								bs.alert = "lselect";
								break;
							}
						}
					}
					/** Get 4 bit effect **/
					{
						// three more bits than needed, reserved for future API
						// functionality
						if (propertiesFlags[7]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 4; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							switch (value) {
							case 0:
								bs.effect = "none";
								break;
							case 1:
								bs.effect = "colorloop";
								break;
							}
						}
					}
					/** Get 16 bit transitiontime **/
					{
						if (propertiesFlags[8]) {
							int value = 0;
							int bitMask = 1;
							for (int j = 0; j < 16; j++) {
								if (set.get(index))
									value |= bitMask;
								index++;
								bitMask *= 2;
							}
							bs.transitiontime = value;
						}
					}
					bsRay[i] = bs;
				}
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new Pair<Integer[], BulbState[]>(bList.toArray(new Integer[bList
				.size()]), bsRay);
	}

	public static BitSet toBitSet(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length; i++) {
			byte mask = 1;
			byte temp = bytes[i];
			for (int j = 0; j < 8; j++) {
				if ((temp & mask) != 0)
					bits.set(8 * i + j, true);
				mask = (byte) (mask << 1);
			}
		}
		return bits;
	}
}
