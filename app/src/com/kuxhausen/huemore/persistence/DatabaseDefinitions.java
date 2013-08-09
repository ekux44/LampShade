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
		 * JSon'd HueState object
		 */
		public static final String STATE = "Dstate";

		// This class cannot be instantiated
		private MoodColumns() {
		}
	}

	public static final class InternalArguments {
		public static final String GROUP_NAME = "Group_Name";
		public static final String BULB_NAME = "Bulb_Name";
		public static final String BULB_NUMBER = "Bulb_Number";
		public static final String MOOD_NAME = "Mood_Name";
		public static final String ENCODED_MOOD = "Encoded_Mood";
		public static final String BULB_STATE = "Bulb_State";
		public static final String BRIDGES = "Bridges";
		public static final String MD5 = "MD5";
		public static final String FRAG_MANAGER_DIALOG_TAG = "dialog";
		public static final String IPV4dot = ".";
		public static final String FALLBACK_USERNAME_HASH = "f01623452466afd4eba5c1ed0a0a9395";
		public final static String ALARM_DETAILS = "alarmDetailsBundle";
		public final static String HUE_STATE = "HueState";
		public final static String COLOR = "Color";
		public final static String SHOW_EDIT_TEXT = "ShowEditText";
		public final static String PREVIOUS_STATE = "PreviousState";
		public final static String PROMPT_UPGRADE = "PromptUpgrade";
		public final static String SERIALIZED_GOD_OBJECT = "SerializedGodObject";
		public final static String TRANSIENT_NETWORK_REQUEST = "TransienteNetworkRequest";
		public final static String PERMANENT_NETWORK_REQUEST = "PermanentNetworkRequest";
		public final static String ALARM_ID = "AlarmId";
		public final static String ALARM_JSON = "AlarmJson";
	}

	public static final class ExternalArguments {
		public static final String NA = "N/A";
	}

	public static final class PreferencesKeys {
		public static final String BRIDGE_IP_ADDRESS = "Bridge_IP_Address";
		public static final String HASHED_USERNAME = "Hashed_Username";
		public static final String DEFAULT_TO_GROUPS = "default_to_groups";
		public static final String DEFAULT_TO_MOODS = "default_to_moods";
		public static final String FIRST_RUN = "First_Run";
		public static final String UPDATE_OPT_OUT = "Update_Opt_Out";
		public static final String BULBS_UNLOCKED = "Bulbs_Unlocked";
		public static final int ALWAYS_FREE_BULBS = 10;
		public static final String NUMBER_OF_CONNECTED_BULBS = "Number_Of_Connected_Bulbs";
		public static final String VERSION_NUMBER = "Version_Number";
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