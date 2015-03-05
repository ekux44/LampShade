package com.kuxhausen.huemore.persistence.migrations;

import android.provider.BaseColumns;

public class DeprecatedDefinitions {

  @Deprecated
  public static final class DeprecatedAlarmColumns implements BaseColumns {

    public static final String TABLE_NAME = "alarms";

    /**
     * JSon'd HueState object of other state data to override mood
     */
    public static final String STATE = "Dstate";

    /**
     * other data needed to delete and alarm
     */
    public static final String INTENT_REQUEST_CODE = "Dintent_request_code";

    private DeprecatedAlarmColumns() {
    }
  }

  @Deprecated
  public static final class DeprecatedGroupColumns implements BaseColumns {

    public static final String TABLE_NAME = "groups";

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

    private DeprecatedGroupColumns() {
    }
  }

}
