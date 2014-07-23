package com.kuxhausen.huemore.net;

import android.content.Context;

import java.util.ArrayList;

public interface Connection {

  public abstract void initializeConnection(Context c);

  public abstract void onDestroy();

  public abstract ArrayList<NetworkBulb> getBulbs();

  public abstract String mainDescription();

  public abstract String subDescription();

  /**
   * return true if device should be kept awake for this connection to continue *
   */
  public abstract boolean hasPendingWork();

  /**
   * eliminate database presence and shut down any network activities *
   */
  public abstract void delete();
}
