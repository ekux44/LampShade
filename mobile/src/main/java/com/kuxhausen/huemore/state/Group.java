package com.kuxhausen.huemore.state;

import java.util.List;

public abstract class Group {

  public abstract String getName();

  public abstract List<Long> getNetworkBulbDatabaseIds();

  public boolean conflictsWith(Group other) {
    for (Long mBulbId : getNetworkBulbDatabaseIds()) {
      for (Long oBulbId : other.getNetworkBulbDatabaseIds()) {
        if (mBulbId.equals(oBulbId)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object g) {
    if (g instanceof Group) {
      if (this.getNetworkBulbDatabaseIds().equals(((Group) g).getNetworkBulbDatabaseIds())) {
        //can't use this.mName.equals(((Group) g).getName())) { because NFC
        return true;
      }
    }
    return false;
  }
}
