package com.kuxhausen.huemore;

import android.provider.BaseColumns;

/**
 * Convenience definitions for Database Hander
 */
public final class DatabaseDefinitions {
	
	// This class cannot be instantiated
	private DatabaseDefinitions() {
	}

	/**
	 * Notes table
	 */
	public static final class MoodColumns implements BaseColumns {
		// This class cannot be instantiated
		private MoodColumns() {
		}

		/**
		 * which mood this state row is part of
		 */
		public static final String MOOD = "mood";

		/**
		 * order in which bulb configurations should be used when applying mood
		 */
		public static final String PRECEDENCE = "precedence";

		/**
		 * true if the light is on, false if off
		 */
		public static final String ON = "on";

		/**
		 * brightness between 0-254 (NB 0 is not off!)
		 */
		public static final String BRI = "bri";

		/**
		 * hs mode: the hue (expressed in ~deg*182)
		 */
		public static final String HUE = "hue";

		/**
		 * hs mode: saturation between 0-254
		 */
		public static final String SAT = "sat";

		/**
		 * xy mode: CIE 1931 colour co-ordinates
		 */
		public static final String X = "x";

		/**
		 * xy mode: CIE 1931 colour co-ordinates
		 */
		public static final String Y = "y";

		/**
		 * ct mode: colour temp (expressed in mireds range 154-500)
		 */
		public static final String CT = "ct";

		/**
		 * 'select' flash the lamp once, 'lselect' repeat flash
		 */
		public static final String ALERT = "alert";

		/**
		 * not sure what this does yet
		 */
		public static final String EFFECT = "effect";

	}
}