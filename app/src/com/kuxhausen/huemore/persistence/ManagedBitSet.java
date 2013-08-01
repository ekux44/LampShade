package com.kuxhausen.huemore.persistence;

import java.util.BitSet;

import android.util.Base64;

public class ManagedBitSet {
	private BitSet set;
	private int index;
	
	public ManagedBitSet(){
		set = new BitSet();
		index = 0;
	}
	
	public void incrementingSet(boolean value){
		set.set(index,value);
		index++;
	}
	public String getBase64Encoding(){
		byte[] intermediaryResult = fromBitSet(set, index);
		return Base64.encodeToString(intermediaryResult, Base64.URL_SAFE);
	}
	
	private static byte[] fromBitSet(BitSet bits, int length) {
		while (length % 8 != 0) {
			length++;
		}

		byte[] bytes = new byte[length / 8];
		for (int i = 0; i < bytes.length; i++) {
			byte mask = 1;
			byte temp = 0;
			for (int j = 0; j < 8; j++) {
				if (bits.get(8 * i + j))
					temp |= mask;
				mask = (byte) (mask << 1);
			}
			bytes[i] = (temp);
		}

		return bytes;
	}

}
