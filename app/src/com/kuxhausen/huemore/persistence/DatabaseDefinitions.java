package com.kuxhausen.huemore.persistence;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for Database Hander and Preferences
 */
public final class DatabaseDefinitions {

	public static final String SLASH = "/";
	
	public static final class AlarmColumns implements BaseColumns {
		
		public static final String TABLE_NAME = "alarms";

		public static final String PATH_ALARMS = "alarms";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri ALARMS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_ALARMS);

		
		public static final String PATH_INDIVIDUAL_ALARM = "alarms/#";
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri INDIVIDUAL_ALARM_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_INDIVIDUAL_ALARM);

		
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
		public static final String TABLE_NAME = "groups";

		
		public static final String PATH_GROUPS = "groups";
		public static final Uri GROUPS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_GROUPS);

		public static final String PATH_GROUPBULBS = "groupbulbs";
		public static final Uri GROUPBULBS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH+ PATH_GROUPBULBS);
		
		/**
		 * which group this bulb row is part of
		 */
		public static final String GROUP = "Dgroup";

		/**
		 * order in which bulb configurations should be used when applying mood (lowest number = first)
		 */
		public static final String PRECEDENCE = "Dprecedence";

		/**
		 * Points to the NetBulb table entry for this bulb
		 */
		public static final String BULB_DATABASE_ID = "Dbulb_database_id";

		
		// This class cannot be instantiated
		private GroupColumns() {
		}
	}

	public static final class MoodColumns implements BaseColumns {

		public static final String TABLE_NAME = "moods";

		/**
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";

		public static final String PATH_MOODS = "moods";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri MOODS_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH_MOODS);

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
	
	public static final class NetBulbColumns implements BaseColumns {

		public static final String TABLE_NAME = "netbulbs";

		public static final String PATH = "netbulbs";

		public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

		public static final String NAME_COLUMN = "D_NAME_COLUMN";
		public static final String DEVICE_ID_COLUMN = "D_DEVICE_ID_COLUMN";
		public static final String CONNECTION_DEVICE_ID_COLUMN = "D_CONNECTION_DEVICE_ID_COLUMN";
		public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
		public static final String JSON_COLUMN = "D_JSON_COLUMN";
		
		public static final class NetBulbType{
			public static final int PHILIPS_HUE = 1;
			public static final int LIFX = 2;
		}
		
		// This class cannot be instantiated
		private NetBulbColumns() {
		}
	}

	public static final class NetConnectionColumns implements BaseColumns {

		public static final String TABLE_NAME = "netconnection";

		public static final String PATH = "netconnection";

		public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

		public static final String NAME_COLUMN = "D_NAME_COLUMN";
		public static final String DEVICE_ID_COLUMN = "D_DEVICE_ID_COLUMN";
		//uses NetBulbType
		public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
		public static final String JSON_COLUMN = "D_JSON_COLUMN";
		
		// This class cannot be instantiated
		private NetConnectionColumns() {
		}
	}

	
	public static final class InternalArguments {
		public static final String GROUP_NAME = "Group_Name";
		public static final String GROUP_VALUES = "Group_Values";
		public static final String BULB_NAME = "Bulb_Name";
		public static final String BULB_NUMBER = "Bulb_Number";
		public static final String MOOD_NAME = "Mood_Name";
		public static final String ENCODED_MOOD = "Encoded_Mood";
		public static final String BRIDGES = "Bridges";
		public static final String MD5 = "MD5";
		public static final String FRAG_MANAGER_DIALOG_TAG = "dialog";
		public static final String IPV4dot = ".";
		public static final String FALLBACK_USERNAME_HASH = "f01623452466afd4eba5c1ed0a0a9395";
		public final static String ALARM_DETAILS = "alarmDetailsBundle";
		public final static String HUE_STATE = "HueState";
		public final static String SHOW_EDIT_TEXT = "ShowEditText";
		public final static String PREVIOUS_STATE = "PreviousState";
		public final static String PROMPT_UPGRADE = "PromptUpgrade";
		public final static String TRANSIENT_NETWORK_REQUEST = "TransienteNetworkRequest";
		public final static String PERMANENT_NETWORK_REQUEST = "PermanentNetworkRequest";
		public final static String ALARM_ID = "AlarmId";
		public final static String ALARM_JSON = "AlarmJson";
		public final static String DECODER_ERROR_UPGRADE = "DecoderErrorUpgrade";
		public static final String DURATION_TIME = "DurationTime";
		public static final String BRIGHTNESS = "Brightness";
		public static final String HELP_PAGE = "HelpPage";
		public static final String ROW = "Row";
		public static final String COLUMN = "Column";
		public static final String FROM_NFC = "FromNfc";
		public static final String NET_BULB_DATABASE_ID = "NET_BULB_DATABASE_ID";
		public static final String MAX_BRIGHTNESS = "MAX_BRIGHTNESS";
	}

	public static final class PreferenceKeys {
		public static final String DEFAULT_TO_GROUPS = "default_to_groups";
		public static final String DEFAULT_TO_MOODS = "default_to_moods";
		public static final String FIRST_RUN = "First_Run";
		public static final String DONE_WITH_WELCOME_DIALOG = "DONE_WITH_WELCOME_DIALOG";
		public static final String HAS_SHOWN_COMMUNITY_DIALOG = "HAS_SHOWN_COMMUNITY_DIALOG";
		public static final String UPDATE_OPT_OUT = "Update_Opt_Out";
		public static final String BULBS_UNLOCKED = "Bulbs_Unlocked";
		public static final int ALWAYS_FREE_BULBS = 10;
		public static final String NUMBER_OF_CONNECTED_BULBS = "Number_Of_Connected_Bulbs";
		public static final String VERSION_NUMBER = "Version_Number";
		public static final String UNNAMED_GROUP_NUMBER = "UNNAMED_GROUP_NUMBER";
		public static final String UNNAMED_MOOD_NUMBER = "UNNAMED_MOOD_NUMBER";
		public static final String CACHED_EXECUTING_ENCODED_MOOD = "CACHED_EXECUTING_ENCODED_MOOD";
		public static final String SHOW_ACTIVITY_ON_NFC_READ = "SHOW_ACTIVITY_ON_NFC_READ";
		
			}
	
	/**
	 * These preference keys were used in previous versions and might still exist on users devices
	 */
	public static final class DeprecatedPreferenceKeys{
		public static final String BRIDGE_IP_ADDRESS = "Bridge_IP_Address";
		public static final String LOCAL_BRIDGE_IP_ADDRESS = "Local_Bridge_IP_Address";
		public static final String INTERNET_BRIDGE_IP_ADDRESS = "Internet_Bridge_IP_Address";
		public static final String HASHED_USERNAME = "Hashed_Username";
	}

	public static final class PlayItems {
		public static final String FIVE_BULB_UNLOCK_1 = "five_bulb_unlock_1";
		public static final String BUY_ME_A_BULB_DONATION_1 = "buy_me_a_bulb_donation_1";
	}

	public static final String AUTHORITY = "com.kuxhausen.provider.huemore.database";
	private static final String SCHEME = "content://";


	// This class cannot be instantiated
	private DatabaseDefinitions() {
	}
}