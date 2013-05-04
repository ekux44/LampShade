package com.kuxhausen.huemore.persistence;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Database Hander and Preferences
 */
public final class DatabaseDefinitions {

	public static final class AlarmColumns implements BaseColumns {
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "alarms";

		/**
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";

		public static final String PATH_ALARMS = "/alarms";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri ALARMS_URI = Uri.parse(SCHEME + AUTHORITY
				+ PATH_ALARMS);

		/**
		 * JSon'd HueState object of other state data to override mood
		 */
		public static final String STATE = "Dstate";

		/**
		 * other data needed to delete and alarm
		 */
		public static final String INTENT_REQUEST_CODE = "Dintent_request_code";

		// This class cannot be instantiated
		private AlarmColumns() {
		}
	}

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

	public static final class InternalArguments{
		public static final String GROUP_NAME = "Group_Name";
	}
	
	public static final class PreferencesKeys {
		public static final String BRIDGE_IP_ADDRESS = "Bridge_IP_Address";
		public static final String HASHED_USERNAME = "Hashed_Username";
		public static final String DEFAULT_TO_GROUPS = "default_to_groups";
		public static final String DEFAULT_TO_MOODS = "default_to_moods";
		public static final String FIRST_RUN = "First_Run";
		public static final String THIRD_UPDATE = "Third_Update";
		public static final String TWO_POINT_OH_UPDATE = "Two_Point_Oh";
		public static final String TWO_POINT_ONE_UPDATE = "Two_Point_One";
		public static final String BULBS_UNLOCKED = "Bulbs_Unlocked";
		public static final int ALWAYS_FREE_BULBS = 10;
		public static final String ALL = ((char) 8) + "ALL";
		public static final String OFF = ((char) 8) + "OFF";
		public static final String ON = ((char) 8) + "ON";
	}

	public static final class PlayItems {
		public static final String FIVE_BULB_UNLOCK_1 = "five_bulb_unlock_1";
		public static final String BUY_ME_A_BULB_DONATION_1 = "buy_me_a_bulb_donation_1";
	}

	public static final String AUTHORITY = "com.kuxhausen.provider.huemore.database";

	// This class cannot be instantiated
	private DatabaseDefinitions() {
	}
}