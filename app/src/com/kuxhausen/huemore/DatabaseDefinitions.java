package com.kuxhausen.huemore;

import android.provider.BaseColumns;

/**
 * Convenience definitions for Database Hander
 */
public final class DatabaseDefinitions {

	// This class cannot be instantiated
	private DatabaseDefinitions() {
	}

	public static final class GroupColumns implements BaseColumns {
		// This class cannot be instantiated
		private GroupColumns() {
		}

		/**
		 * which group this bulb row is part of
		 */
		public static final String GROUP = "Dgroup";

		/**
		 * which bulb. currently using bulb name string until better method
		 * found
		 */
		public static final String BULB = "Dbulb";
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
		public static final String MOOD = "Dmood";

		/**
		 * order in which bulb configurations should be used when applying mood
		 */
		public static final String PRECEDENCE = "Dprecedence";

		/**
		 * true if the light is on, false if off
		 */
		public static final String ON = "Don";

		/**
		 * brightness between 0-254 (NB 0 is not off!)
		 */
		public static final String BRI = "Dbri";

		/**
		 * hs mode: the hue (expressed in ~deg*182)
		 */
		public static final String HUE = "Dhue";

		/**
		 * hs mode: saturation between 0-254
		 */
		public static final String SAT = "Dsat";

		/**
		 * xy mode: CIE 1931 colour co-ordinates
		 */
		public static final String X = "Dx";

		/**
		 * xy mode: CIE 1931 colour co-ordinates
		 */
		public static final String Y = "Dy";

		/**
		 * ct mode: colour temp (expressed in mireds range 154-500)
		 */
		public static final String CT = "Dct";

		/**
		 * 'select' flash the lamp once, 'lselect' repeat flash
		 */
		public static final String ALERT = "Dalert";

		/**
		 * not sure what this does yet
		 */
		public static final String EFFECT = "effect";

	}
}