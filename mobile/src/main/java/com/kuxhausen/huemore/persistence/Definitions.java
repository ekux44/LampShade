package com.kuxhausen.huemore.persistence;

import android.net.Uri;
import android.provider.BaseColumns;

import com.kuxhausen.huemore.BuildConfig;

/**
 * Convenience definitions for Database Hander and Preferences
 */
public final class Definitions {

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
    public static final Uri INDIVIDUAL_ALARM_URI = Uri.parse(SCHEME + AUTHORITY + SLASH
                                                             + PATH_INDIVIDUAL_ALARM);


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
    public static final Uri GROUPBULBS_URI = Uri
        .parse(SCHEME + AUTHORITY + SLASH + PATH_GROUPBULBS);

    /**
     * which group this bulb row is part of
     */
    public static final String GROUP = "Dgroup";
    public static final String COL_GROUP_LOWERCASE_NAME = "D_COL_GROUP_LOWERCASE_NAME";

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

    public static final String COL_MOOD_VALUE = "Dstate";
    public static final String COL_MOOD_NAME = "Dmood";
    public static final String COL_MOOD_LOWERCASE_NAME = "D_COL_MOOD_LOWERCASE_NAME";
    public static final String COL_MOOD_PRIORITY = "D_COL_MOOD_PRIORITY";

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
    public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
    public static final String JSON_COLUMN = "D_JSON_COLUMN";
    /**
     * holds a values 0-100 *
     */
    public static final String CURRENT_MAX_BRIGHTNESS = "D_CURRENT_MAX_BRIGHTNESS";
    /**
     * Points to the NetConnection table entry for this bulb
     */
    public static final String CONNECTION_DATABASE_ID = "Dconnection_database_id";

    public static final class NetBulbType {

      public static final int DEBUG = 0;
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
    // uses NetBulbType
    public static final String TYPE_COLUMN = "D_TYPE_COLUMN";
    public static final String JSON_COLUMN = "D_JSON_COLUMN";

    // This class cannot be instantiated
    private NetConnectionColumns() {
    }
  }

  /*
   * This table is used to store ongoing/playing moods details while the app power naps
   */
  public static final class PlayingMood implements BaseColumns {

    public static final String TABLE_NAME = "playingmood";

    public static final String PATH = "playingmood";

    public static final Uri URI = Uri.parse(SCHEME + AUTHORITY + SLASH + PATH);

    /**
     * json encoded group object
     */
    public static final String COL_GROUP_VALUE = "D_GROUP_VALUE_COLUMN";
    /**
     * the mood name, may not exist in the mood table
     */
    public static final String COL_MOOD_NAME = "D_MOOD_NAME_COLUMN";
    /**
     * the URL-ENCODE mood value
     */
    public static final String COL_MOOD_VALUE = "D_MOOD_VALUE_COLUMN";

    public static final String COL_MOOD_BRI = "D_COL_INITIAL_MAX_BRI";

    public static final String COL_INTERNAL_PROGRESS = "D_COL_INTERNAL_PROGRESS";

    /**
     * the original mood start time measured in miliseconds using SystemClock elapsedRealTime()
     */
    public static final String COL_MILI_TIME_STARTED = "D_MILI_TIME_START_COLUMN";

    // This class cannot be instantiated
    private PlayingMood() {
    }
  }

  public static final class InternalArguments {

    public static final String GROUP_NAME = "Group_Name";
    public static final String MOOD_NAME = "Mood_Name";
    public static final String ENCODED_MOOD = "Encoded_Mood";
    public static final String BRIDGES = "Bridges";
    public static final String MD5 = "MD5";
    public static final String FRAG_MANAGER_DIALOG_TAG = "dialog";
    public static final String FALLBACK_USERNAME_HASH = "f01623452466afd4eba5c1ed0a0a9395";
    public final static String ALARM_DETAILS = "alarmDetailsBundle";
    public final static String HUE_STATE = "HueState";
    public final static String PREVIOUS_STATE = "PreviousState";
    public final static String ALARM_ID = "AlarmId";
    public final static String ALARM_JSON = "AlarmJson";
    public final static String DECODER_ERROR_UPGRADE = "DecoderErrorUpgrade";
    public static final String DURATION_TIME = "DurationTime";
    public static final String HELP_PAGE = "HelpPage";
    public static final String ROW = "Row";
    public static final String COLUMN = "Column";
    public static final String NET_BULB_DATABASE_ID = "NET_BULB_DATABASE_ID";
    public static final String MAX_BRIGHTNESS = "MAX_BRIGHTNESS";
    public static final String NAV_DRAWER_PAGE = "NAV_DRAWER_PAGE";
    public static final String GROUPBULB_TAB = "GROUPBULB_TAB";
    public static final String FLAG_SHOW_NAV_DRAWER = "FLAG_SHOW_NAV_DRAWER";
    public static final String FLAG_CANCEL_PLAYING = "FLAG_CANCEL_PLAYING";
    public static final String VOICE_INPUT = "VOICE_INPUT";
    public static final String VOICE_INPUT_LIST = "VOICE_INPUT_LIST";
    public static final String VOICE_INPUT_CONFIDENCE_ARRAY = "VOICE_INPUT_CONFIDENCE_ARRAY";
    public static final String CLICK_ACTION = "com.kuxhausen.huemore.CLICK_ACTION";
  }

  public static final class PreferenceKeys {

    public static final String DEFAULT_TO_GROUPS = "default_to_groups";
    public static final String DEFAULT_TO_MOODS = "default_to_moods";
    public static final String FIRST_RUN = "First_Run";
    public static final String DONE_WITH_WELCOME_DIALOG = "DONE_WITH_WELCOME_DIALOG";
    public static final String HAS_SHOWN_COMMUNITY_DIALOG = "HAS_SHOWN_COMMUNITY_DIALOG";
    public static final String UPDATE_OPT_OUT = "Update_Opt_Out";
    public static final String NUMBER_OF_CONNECTED_BULBS = "Number_Of_Connected_Bulbs";
    public static final String VERSION_NUMBER = "Version_Number";
    public static final String UNNAMED_GROUP_NUMBER = "UNNAMED_GROUP_NUMBER";
    public static final String UNNAMED_MOOD_NUMBER = "UNNAMED_MOOD_NUMBER";
    public static final String CACHED_EXECUTING_ENCODED_MOOD = "CACHED_EXECUTING_ENCODED_MOOD";
    public static final String SHOW_ACTIVITY_ON_NFC_READ = "SHOW_ACTIVITY_ON_NFC_READ";
    public static final String USER_SELECTED_LOCALE_LANG = "USER_SELECTED_LOCALE_LANG";
  }

  /**
   * These preference keys were used in previous versions and might still exist on users devices
   */
  public static final class DeprecatedPreferenceKeys {

    public static final String BRIDGE_IP_ADDRESS = "Bridge_IP_Address";
    public static final String LOCAL_BRIDGE_IP_ADDRESS = "Local_Bridge_IP_Address";
    public static final String INTERNET_BRIDGE_IP_ADDRESS = "Internet_Bridge_IP_Address";
    public static final String HASHED_USERNAME = "Hashed_Username";
  }

  public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.database";
  private static final String SCHEME = "content://";


  // This class cannot be instantiated
  private Definitions() {
  }
}
