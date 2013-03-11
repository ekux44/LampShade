package com.kuxhausen.huemore;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Database Hander and Preferences
 */
public final class DatabaseDefinitions {
	public static final class GroupColumns implements BaseColumns {
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "groups";

		/**
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";

		public static final String PATH_GROUPS = "/groups";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri GROUPS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_GROUPS);

		public static final String PATH_GROUPBULBS = "/groupbulbs";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri GROUPBULBS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_GROUPBULBS);
		/**
		 * which group this bulb row is part of
		 */
		public static final String GROUP = "Dgroup";

		/**
		 * which bulb. currently using bulb number until better method found
		 */
		public static final String BULB = "Dbulb";

		/**
		 * order in which bulb configurations should be used when applying mood
		 */
		public static final String PRECEDENCE = "Dprecedence";

		// This class cannot be instantiated
		private GroupColumns() {
		}
	}

	/**
	 * Notes table
	 */
	public static final class MoodColumns implements BaseColumns {

		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "moods";

		/**
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";

		public static final String PATH_MOODS = "/moods";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri MOODS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_MOODS);

		public static final String PATH_MOODSTATES = "/moodstates";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri MOODSTATES_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_MOODSTATES);

		/**
		 * which mood this state row is part of
		 */
		public static final String MOOD = "Dmood";

		/**
		 * order in which bulb configurations should be used when applying mood
		 */
		public static final String PRECEDENCE = "Dprecedence";

		/**
		 * JSon'd HueState object
		 */
		public static final String STATE = "Dstate";

		// This class cannot be instantiated
		private MoodColumns() {
		}
	}

	public static final class PreferencesKeys {
		public static final String Bridge_IP_Address = "Bridge_IP_Address";
		public static final String Hashed_Username = "Hashed_Username";
		public static final String First_Run = "First_Run";
	}

	public static final String AUTHORITY = "com.kuxhausen.provider.huemore";

	// This class cannot be instantiated
	private DatabaseDefinitions() {
	}
}