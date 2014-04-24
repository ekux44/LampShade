package com.kuxhausen.huemore.net.hue;

import com.kuxhausen.huemore.state.BulbState;

public class PendingStateChange implements Comparable<PendingStateChange>{
	public BulbState sentState;
	public HueBulb hubBulb;
	//System.nanoTime when sent
	public long timeInitiated;
	
	
	public PendingStateChange(BulbState bState, HueBulb hBulb, long tInitiated){
		sentState = bState;
		hubBulb = hBulb;
		timeInitiated = tInitiated;
	}
	
	@Override
	/**
	 * reverse natural ordering so hightset timeInitiated = lowest/first
	 */
	public int compareTo(PendingStateChange another) {
		return ((Long)another.timeInitiated).compareTo(timeInitiated);
	}
	
	
}
