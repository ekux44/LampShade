package com.kuxhausen.huemore.state.api;

public class BulbState {

	/**
	 * On/Off state of the light. On=true, Off=false
	 */
	public boolean on;
	/**
	 * The brightness value to set the light to. Brightness is a scale from 0
	 * (the minimum the light is capable of) to 255 (the maximum). Note: a
	 * brightness of 0 is not off.
	 */
	public Integer bri;
	/**
	 * The hue value to set light to. The hue value is a wrapping value between
	 * 0 and 65535. Both 0 and 65535 are red, 25500 is green and 46920 is blue.
	 * e.g. “hue”: 50000 will set the light to a specific hue.
	 */
	public Integer hue;
	/**
	 * Saturation of the light. 255 is the most saturated (colored) and 0 is the
	 * least saturated (white).
	 */
	public Short sat;
	/**
	 * The x and y coordinates of a color in CIE color space. The first entry is
	 * the x coordinate and the second entry is the y coordinate. Both x and y
	 * must be between 0 and 1. If the specified coordinates are not in the CIE
	 * color space, the closest color to the coordinates will be chosen.
	 */
	public Double[] xy;
	/**
	 * The Mired Color temperature of the light. 2012 connected lights are
	 * capable of 153 (6500K) to 500 (2000K).
	 */
	public Integer ct;

	/**
	 * The alert effect, is a temporary change to the bulb’s state, and has one
	 * of the following values: “none” – The light is not performing an alert
	 * effect. “select” – The light is performing one breathe cycle. “lselect” –
	 * The light is performing breathe cycles for 30 seconds or until an
	 * "alert": "none" command is received.
	 */
	public String alert;

	/**
	 * The dynamic effect of the light, can either be "none" or "colorloop"
	 */
	public String effect;

	/**
	 * The duration of the transition from the light’s current state to the new
	 * state. This is given as a multiple of 100ms and defaults to 4 (400ms).
	 * For example, setting transistiontime:10 will make the transition last 1
	 * second.
	 */
	public Integer transitiontime;

	public BulbState() {
	}

	/**
	 * Must ensure uniqueness for HueUrlEncoder
	 */
	@Override
	public String toString() {
		String result = "";
		result += "on:" + (on ? "true" : "false") + " ";
		if (bri != null)
			result += "bri:" + bri + " ";
		if (hue != null)
			result += "hue:" + hue + " ";
		if (sat != null)
			result += "sat:" + sat + " ";
		if (xy != null)
			result += "xy:" + xy[0] + " " + xy[1] + " ";
		if (ct != null)
			result += "ct:" + ct + " ";
		if (alert != null)
			result += "alert:" + alert + " ";
		if (effect != null)
			result += "effect:" + effect + " ";
		if (transitiontime != null)
			result += "transitiontime:" + transitiontime + " ";
		return result;
	}
}
