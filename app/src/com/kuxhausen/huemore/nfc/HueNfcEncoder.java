package com.kuxhausen.huemore.nfc;

import java.util.ArrayList;
import java.util.BitSet;

import android.util.Base64;
import android.util.Pair;

import com.kuxhausen.huemore.state.BulbState;

public class HueNfcEncoder {

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
	public HueNfcEncoder() {

	}

	public static String encode(Integer[] bulbS, BulbState[] bsRay) {
		if (bulbS != null && bsRay != null) {
			BitSet set = new BitSet();
			int index = 0;//points to the next spot
			/** Set 4 bit protocol version **/
			{
				set.set(index, false);
				index++;
				set.set(index, false);
				index++;
				set.set(index, false);
				index++;
				set.set(index, true);
				index++;
			}
			/** Set 50 bit bulbs flags **/
			{
				set.set(index, index+50, false);
				for(int bulb : bulbS)
					set.set(index+(bulb-1), true);
				index+=50;
			}
			/** Set num states **/
			{
				int stateLength = 0; // number of non null bulb states
				for (BulbState bs : bsRay) {
					if (bs != null)
						stateLength++;
				}

				int bitMask = 1;
				for (int i = 0; i < 7; i++) {
					if ((stateLength & bitMask) > 0) {
						set.set(index, true);
						index++;
					} else {
						set.set(index, false);
						index++;
					}
					bitMask *= 2;
				}
			}
			/** Encode each state **/
			{
				for(BulbState bs : bsRay){
					/** Put 9 bit properties flags **/{
						// On/OFF flag always include in v1 implementation 1
						set.set(index, true);
						index++;
						
						//Put bri flag
						if(bs.bri!=null)
							set.set(index, true);
						index++;
						
						//Put hue flag
						if(bs.hue!=null)
							set.set(index, true);
						index++;
						
						//Put sat flag
						if(bs.sat!=null)
							set.set(index, true);
						index++;
						
						//Put xy flag
						if(bs.xy!=null)
							set.set(index, true);
						index++;
						
						//Put ct flag
						if(bs.ct!=null)
							set.set(index, true);
						index++;
						
						//Put alert flag
						if(bs.alert!=null)
							set.set(index, true);
						index++;
						
						//Put effect flag
						if(bs.effect!=null)
							set.set(index, true);
						index++;
						
						//Put transitiontime flag
						if(bs.transitiontime!=null)
							set.set(index, true);
						index++;
						
					}
					/** Put on bit **/{
						
					}
					/** Put 8 bit bri **/{
						
					}
					/** Put 16 bit hue **/{
						
					}
					
					/** Put 8 bit sat **/{
						
					}
					/** Put 64 bit xy **/{
						//TODO implement xy mode
					}
					/**	Put 9 bit ct **/{
						
					}
					/** Put 2 bit alert **/{
						
					}
					/** Put 4 bit effect **/{
						//three more bits than needed, reserved for future API functionality
					}
					/** Put 16 bit transitiontime **/{
						
					}
				}
			}

			byte[] intermediaryResult = fromBitSet(set,index);
			return Base64.encodeToString(intermediaryResult,Base64.URL_SAFE);
		}

		return "";
	}

	public static Pair<Integer[], BulbState[]> decode(String encoded) {
		ArrayList<Integer> bList = null;
		BulbState[] bsRay = null;
		try {
			byte[] intermediaryReverse = Base64.decode(encoded, Base64.URL_SAFE);
			BitSet set = toBitSet(intermediaryReverse);
			bList = new ArrayList<Integer>();
			
			int index = 0;//points to the next spot
			/** Get protocol version **/
			{
				//Make sure the protocol is version 1 (0001)
				if(set.get(0)||set.get(1)||set.get(2)||!set.get(3)){
					//Unsupported protocol version
					return new Pair(null,null);				
				}
				index+=4;
			}
			/** Set bulbs flags version **/
			{
				
				for(int i = 0; i<50; i++)
					if(set.get(index+i))
						bList.add(i+1);
				index+=50;
			}
			/** Set num states **/
			{
				int numStates = 0;
				int bitMask = 1;
				for (int i = 0; i < 7; i++) {
					if(set.get(index))
						numStates |= bitMask;
					index++;
					bitMask *= 2;
				}
				bsRay = new BulbState[numStates];
			}
			
			/** Encode each state **/
			{
				for(BulbState bs : bsRay){
					/** Put 9 bit properties flags **/{
						// On/OFF flag always include in v1 implementation 1
						set.set(index, true);
						index++;
						
						//Put bri flag
						if(bs.bri!=null)
							set.set(index, true);
						index++;
						
						//Put hue flag
						if(bs.hue!=null)
							set.set(index, true);
						index++;
						
						//Put sat flag
						if(bs.sat!=null)
							set.set(index, true);
						index++;
						
						//Put xy flag
						if(bs.xy!=null)
							set.set(index, true);
						index++;
						
						//Put ct flag
						if(bs.ct!=null)
							set.set(index, true);
						index++;
						
						//Put alert flag
						if(bs.alert!=null)
							set.set(index, true);
						index++;
						
						//Put effect flag
						if(bs.effect!=null)
							set.set(index, true);
						index++;
						
						//Put transitiontime flag
						if(bs.transitiontime!=null)
							set.set(index, true);
						index++;
						
					}
					/** Get on bit **/{
						
					}
					/** Get 8 bit bri **/{
						
					}
					/** Get 16 bit hue **/{
						
					}
					
					/** Get 8 bit sat **/{
						
					}
					/** Get 64 bit xy **/{
						//TODO implement xy mode
					}
					/**	Get 9 bit ct **/{
						
					}
					/** Get 2 bit alert **/{
						
					}
					/** Get 4 bit effect **/{
						//three more bits than needed, reserved for future API functionality
					}
					/** Get 16 bit transitiontime **/{
						
					}
				}
			}
			
			
			
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new Pair<Integer[],BulbState[]>((Integer[]) bList.toArray(), bsRay);
	}

	
	public static byte[] fromBitSet(BitSet bits, int length) {
		while(length%8!=0){
			length++;
		}
		
		byte[] bytes = new byte[length/ 8];
		for(int i = 0; i<bytes.length; i++){
			byte mask = 1;
			byte temp = 0;
			for(int j =0; j< 8; j++){
				if(bits.get(8*i+j))
					temp|=mask;
				mask= (byte) (mask << 1);
			}
			bytes[i] = (byte)(temp);
		}
		
		return bytes;
	}	
	
	public static BitSet toBitSet(byte[] bytes) {
		BitSet bits = new BitSet();
		for(int i = 0; i<bytes.length; i++){
			byte mask =1;
			byte temp = bytes[i];
			for(int j = 0; j<8; j++){
				if((temp & mask) !=0)
					bits.set(8*i+j, true);
				mask= (byte) (mask << 1);
			}
		}
		return bits;
	}
}
