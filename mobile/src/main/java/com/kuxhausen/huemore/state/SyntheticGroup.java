package com.kuxhausen.huemore.state;

import java.util.List;

/**
 * A group object not backed by an entry in the Groups database. Uses include include encapsulating
 * single bulbs for the MoodPlayer
 */
public class SyntheticGroup extends Group {


  private String mName;
  private List<Long> mNetworkBulbDatabaseIds;

  public SyntheticGroup(List<Long> netBulbDatabaseIds, String name) {
    mName = name;
    mNetworkBulbDatabaseIds = netBulbDatabaseIds;
  }

  public String getName() {
    return mName;
  }

  public List<Long> getNetworkBulbDatabaseIds() {
    return mNetworkBulbDatabaseIds;
  }

  public static SyntheticGroup asSynthetic(Group group) {
    return new SyntheticGroup(group.getNetworkBulbDatabaseIds(), group.getName());
  }
}
